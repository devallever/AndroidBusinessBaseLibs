package app.allever.android.sample.im.websocket.server

import android.util.Log
import app.allever.android.sample.im.database.UserRepository
import app.allever.android.sample.im.protocol.ContentType
import app.allever.android.sample.im.protocol.Message
import app.allever.android.sample.im.protocol.MessageBuilder
import app.allever.android.sample.im.protocol.MessageProtocol
import app.allever.android.sample.im.protocol.MessageStatus
import app.allever.android.sample.im.protocol.MessageType
import kotlinx.coroutines.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket 服务端
 */
object IMWebSocketServer {
    private val TAG = IMWebSocketServer::class.java.simpleName
    private var server: WebSocketServer? = null

    @Volatile
    private var port: Int = 5400

    // key 改为用户名
    private val clientsMap = ConcurrentHashMap<String, WebSocket>()

    // 新增：记录客户端最后活跃时间
    private val heartbeatMap = ConcurrentHashMap<String, Long>()

    // 反向映射：WebSocket -> 用户名，用于 O(1) 查找
    private val connUsernameMap = ConcurrentHashMap<WebSocket, String>()

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 增加 @Volatile 保证多线程下的可见性
    @Volatile
    private var serverListener: WeakReference<ServerListener>? = null

    private const val HEARTBEAT_PING = "HEARTBEAT_PING"
    private const val HEARTBEAT_PONG = "HEARTBEAT_PONG"
    private const val HEARTBEAT_TIMEOUT = 45 * 1000L
    private var heartbeatCheckJob: Job? = null

    fun startServer(port: Int) {

        if (server != null) {
            log("服务端已经在运行中，请勿重复启动")
            return
        }
        synchronized(this) {
            // 双重校验，防止排队进入同步块时服务已被其他线程启动
            if (server != null) return
            this.port = port

            val newServer = object : WebSocketServer(InetSocketAddress(port)) {
                override fun onStart() {
                    log("IM 服务端已启动，监听端口: $port")
                    val url = getConnectUrl()
                    log("IM 服务端已启动，监听地址: $url")

                    // 服务启动：重置所有用户在线状态，避免脏数据
                    UserRepository.resetAllOnlineStatus()

                    scope.launch(Dispatchers.Main) {
                        serverListener?.get()?.onStarted(url)
                    }
                    startHeartbeatCheck()
                }

                override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                    // 从 URL 参数获取用户名，格式：ws://ip:port?username=xxx
                    val username = getUsernameFromHandshake(handshake)
                    if (username.isBlank() || !UserRepository.isUserExists(username)) {
                        conn.close(1008, "用户未登录或不存在")
                        return
                    }

                    // 如果该用户已在线，踢掉旧连接
                    clientsMap[username]?.let { oldConn ->
                        connUsernameMap.remove(oldConn)
                        oldConn.close(1008, "账号在其他地方登录")
                    }

                    clientsMap[username] = conn
                    connUsernameMap[conn] = username
                    heartbeatMap[username] = System.currentTimeMillis()

                    // 更新数据库：用户上线
                    UserRepository.updateOnlineStatus(username, true)

                    log("用户上线: $username, 当前在线人数: ${clientsMap.size}")

                    val welcomeMsg = MessageBuilder()
                        .type(MessageType.SYSTEM)
                        .fromUser("system")
                        .toUser(username)
                        .content("欢迎回来，$username！")
                        .status(MessageStatus.SENT)
                        .buildText()
                    sendMessageToClient(username, welcomeMsg)

                    val onlineMsg = MessageBuilder()
                        .type(MessageType.SYSTEM)
                        .fromUser("system")
                        .content("$username 上线了")
                        .status(MessageStatus.SENT)
                        .buildText()
                    broadcastToOthers(onlineMsg, username)
                }

                override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                    val username = getUsernameByConn(conn) ?: return
                    clientsMap.remove(username)
                    connUsernameMap.remove(conn)
                    heartbeatMap.remove(username)

                    // 更新数据库：用户离线
                    UserRepository.updateOnlineStatus(username, false)

                    log("用户离线: $username, 剩余在线人数: ${clientsMap.size}")
                    val offlineMsg = MessageBuilder()
                        .type(MessageType.SYSTEM)
                        .fromUser("system")
                        .content("$username 离线了")
                        .status(MessageStatus.SENT)
                        .buildText()
                    broadcastToOthers(offlineMsg, username)
                }

                override fun onMessage(conn: WebSocket, message: String) {
                    val username = getUsernameByConn(conn) ?: return
                    heartbeatMap[username] = System.currentTimeMillis()

                    if (message == HEARTBEAT_PING) {
                        sendMessageToClient(username, HEARTBEAT_PONG)
                        return
                    }

                    log("收到 $username 的消息: $message")
                    scope.launch {
                        processMessage(username, message, conn)
                    }
                }

                override fun onError(conn: WebSocket?, ex: Exception) {
                    val clientId = conn?.let { getUsernameByConn(it) } ?: "全局"
                    if (ex is java.net.BindException) {
                        logE("连接异常 ($clientId): 端口 $port 被占用")
                        server = null
                    } else {
                        logE("连接异常 ($clientId): ${ex.message}")
                    }
                }
            }
            newServer.isReuseAddr = true
            server = newServer

            scope.launch {
                try {
                    newServer.start()
                } catch (e: Exception) {
                    // 启动失败立即释放引用
                    synchronized(this@IMWebSocketServer) {
                        if (server === newServer) server = null
                    }
                    logE("服务启动失败: ${e.message}")
                }
            }
        }
    }

    // ====================== 身份工具 ======================
    /**
     * 从握手参数中解析用户名
     */
    private fun getUsernameFromHandshake(handshake: ClientHandshake): String {
        val resource = handshake.resourceDescriptor
        val queryStart = resource.indexOf("?")
        if (queryStart == -1) return ""

        val query = resource.substring(queryStart + 1)
        val params = query.split("&")
        for (param in params) {
            val pair = param.split("=")
            if (pair.size == 2 && pair[0] == "username") {
                return pair[1]
            }
        }
        return ""
    }

    /**
     * 根据连接反查用户名（O(1) 查找）
     */
    private fun getUsernameByConn(conn: WebSocket): String? {
        return connUsernameMap[conn]
    }

    /**
     * 断开指定用户连接（供 HTTP 退出接口调用）
     */
    fun disconnectUser(username: String) {
        clientsMap[username]?.close(1000, "主动退出登录")
    }

    // ====================== 心跳检测 ======================
    private fun startHeartbeatCheck() {
        heartbeatCheckJob?.cancel()
        heartbeatCheckJob = scope.launch {
            while (isActive && server != null) {
                delay(15 * 1000) // 每 15 秒检查一次
                val currentTime = System.currentTimeMillis()
                val timeoutUsers = mutableListOf<String>()
                //原因：两个线程同时修改同一个 ConcurrentHashMap，虽然不会抛 ConcurrentModificationException，但可能导致迭代遗漏、状态不一致。
                // 第一步：收集所有超时客户端
                heartbeatMap.forEach { (username, lastActiveTime) ->
                    if (currentTime - lastActiveTime > HEARTBEAT_TIMEOUT) {
                        timeoutUsers.add(username)
                    }
                }
                // 第二步：统一关闭
                timeoutUsers.forEach { username ->
                    logE("心跳超时，断开用户: $username")
                    clientsMap[username]?.close()
                }
            }
        }
    }

    // ====================== 消息处理 ======================
    private suspend fun processMessage(username: String, message: String, senderConn: WebSocket) {
        if (message.startsWith("@")) {
            val targetEndIndex = message.indexOf("#")
            if (targetEndIndex > 1) {
                val targetUser = message.substring(1, targetEndIndex)
                val realMsg = message.substring(targetEndIndex + 1).trim()
                val msg = MessageBuilder()
                    .type(MessageType.PRIVATE)
                    .fromUser(username)
                    .toUser(targetUser)
                    .content(realMsg)
                    .status(MessageStatus.SENT)
                    .buildText()
                sendPrivateMessage(msg)
            }
            return
        }

        if (message.startsWith("{")) {
            try {
                val msg = Message.fromJson(message)
                msg.fromUser = username
                msg.status = MessageStatus.SENT
                dispatchMessage(msg)
            } catch (e: Exception) {
                logE("解析 JSON 消息失败: ${e.message}")
                val textMsg = MessageBuilder()
                    .type(MessageType.BROADCAST)
                    .fromUser(username)
                    .content(message)
                    .status(MessageStatus.SENT)
                    .buildText()
                broadcastToOthers(textMsg, username)
            }
        } else {
            val textMsg = MessageBuilder()
                .type(MessageType.BROADCAST)
                .fromUser(username)
                .content(message)
                .status(MessageStatus.SENT)
                .buildText()
            broadcastToOthers(textMsg, username)
        }
    }

    private fun dispatchMessage(msg: Message) {
        when (msg.type) {
            MessageType.PRIVATE -> sendPrivateMessage(msg)
            MessageType.GROUP -> sendGroupMessage(msg)
            MessageType.SYSTEM -> sendSystemMessage(msg)
            MessageType.BROADCAST -> broadcastToOthers(msg, msg.fromUser)
        }
    }

    private fun sendPrivateMessage(msg: Message) {
        sendMessageToClient(msg.toUser, msg)
    }

    private fun sendGroupMessage(msg: Message) {
        broadcastToOthers(msg, msg.fromUser)
    }

    private fun sendSystemMessage(msg: Message) {
        broadcastToOthers(msg, "")
    }

    // ====================== 消息发送 ======================
    fun sendMessageToClient(username: String, message: String) {
        clientsMap[username]?.let { conn ->
            if (conn.isOpen) {
                try {
                    conn.send(message)
                } catch (e: Exception) {
                    logE("发送消息失败 $username: ${e.message}")
                }
            }
        }
    }

    fun sendMessageToClient(username: String, message: Message) {
        clientsMap[username]?.let { conn ->
            if (conn.isOpen) {
                try {
                    conn.send(message.toJson())
                } catch (e: Exception) {
                    logE("发送消息失败 $username: ${e.message}")
                }
            }
        }
    }

    fun sendPrivateMessage(targetUser: String, message: String) =
        sendMessageToClient(targetUser, message)

    fun getOnlineCount(): Int = clientsMap.size

    private fun broadcastToOthers(message: String, excludeUser: String) {
        val targetClients = clientsMap.entries
            .filter { it.key != excludeUser && it.value.isOpen }
            .map { it.value }

        if (targetClients.isNotEmpty()) {
            try {
                server?.broadcast(message, targetClients)
            } catch (e: Exception) {
                logE("广播异常: ${e.message}")
            }
        }
    }

    private fun broadcastToOthers(message: Message, excludeUser: String) {
        val targetClients = clientsMap.entries
            .filter { it.key != excludeUser && it.value.isOpen }
            .map { it.value }

        if (targetClients.isNotEmpty()) {
            try {
                server?.broadcast(message.toJson(), targetClients)
            } catch (e: Exception) {
                logE("广播异常: ${e.message}")
            }
        }
    }

    // ====================== 生命周期 ======================
    fun stopServer() {
        if (!isStarted()) return
        scope.launch {
            // 增加协程内部的二次校验
            val currentServer = server ?: return@launch
            heartbeatCheckJob?.cancel()
            heartbeatCheckJob = null

            clientsMap.values.forEach {
                try { it.close() } catch (_: Exception) {}
            }
            clientsMap.clear()
            connUsernameMap.clear()
            heartbeatMap.clear()

            try {
                currentServer.stop(1000)
            } catch (e: Exception) {
                logE("停止服务异常: ${e.message}")
            } finally {
                server = null
            }

            log("IM 服务端已关闭")
            scope.launch(Dispatchers.Main) { serverListener?.get()?.onStopped() }
        }
    }

    fun isStarted(): Boolean = server != null

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (ni.isLoopback || !ni.isUp) continue
                val addresses = ni.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return "未知IP"
    }

    fun getConnectUrl(): String = if (server != null) "ws://${getLocalIpAddress()}:$port" else ""

    // ====================== 日志 ======================
    private fun log(message: String) {
        Log.d(TAG, message)
        scope.launch(Dispatchers.Main) { serverListener?.get()?.onLog(message) }
    }

    private fun logE(message: String) {
        Log.e(TAG, message)
        scope.launch(Dispatchers.Main) { serverListener?.get()?.onLog(message) }
    }

    fun registerServerListener(serverListener: ServerListener?) {
        this.serverListener = WeakReference(serverListener)
    }

    fun unregisterServerListener() {
        this.serverListener = null
    }

    interface ServerListener {
        fun onLog(log: String)
        fun onStarted(url: String)
        fun onStopped()
    }
}