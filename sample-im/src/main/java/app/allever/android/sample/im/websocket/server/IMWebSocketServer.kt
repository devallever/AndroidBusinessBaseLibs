package app.allever.android.sample.im.websocket.server

import android.util.Log
import kotlinx.coroutines.*
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
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
    private val clientsMap = ConcurrentHashMap<String, WebSocket>()

    // 新增：记录客户端最后活跃时间
    private val heartbeatMap = ConcurrentHashMap<String, Long>()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 增加 @Volatile 保证多线程下的可见性
    @Volatile
    private var serverListener: ServerListener? = null

    // 新增：心跳常量
    private const val HEARTBEAT_PING = "HEARTBEAT_PING"
    private const val HEARTBEAT_PONG = "HEARTBEAT_PONG"
    private const val HEARTBEAT_TIMEOUT = 45 * 1000L // 45秒未收到消息视为超时
    fun startServer(port: Int) {
        if (server != null) {
            log("服务端已经在运行中，请勿重复启动")
            return
        }
        this.port = port
        scope.launch {
            server = object : WebSocketServer(InetSocketAddress(port)) {
                override fun onStart() {
                    log("IM 服务端已启动，监听端口: $port")
                    val url = getConnectUrl()
                    log("IM 服务端已启动，监听地址: $url")
                    scope.launch(Dispatchers.Main) {
                        serverListener?.onStarted(url)
                    }
                    // 启动心跳检测定时器
                    startHeartbeatCheck()
                }

                override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                    val clientId = getClientId(conn)
                    clientsMap[clientId] = conn
                    heartbeatMap[clientId] = System.currentTimeMillis() // 初始化活跃时间
                    log("客户端连接成功: $clientId, 当前在线人数: ${clientsMap.size}")
                    sendMessageToClient(clientId, "系统消息：欢迎加入聊天室！")
                    broadcastToOthers("系统消息：$clientId 上线了", conn)
                }

                override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                    val clientId = getClientId(conn)
                    clientsMap.remove(clientId)
                    heartbeatMap.remove(clientId)
                    log("客户端断开连接: $clientId, 剩余在线人数: ${clientsMap.size}")
                    broadcastToOthers("系统消息：$clientId 离线了", conn)
                }

                override fun onMessage(conn: WebSocket, message: String) {
                    val clientId = getClientId(conn)
                    // 更新活跃时间
                    heartbeatMap[clientId] = System.currentTimeMillis()
                    // 处理心跳请求
                    if (message == HEARTBEAT_PING) {
                        log("收到 $clientId 的心跳包")
                        sendMessageToClient(clientId, HEARTBEAT_PONG)
                        return // 心跳包不进行业务广播
                    }
                    log("收到 $clientId 的消息: $message")
                    scope.launch {
                        processMessage(clientId, message, conn)
                    }
                }

                override fun onError(conn: WebSocket?, ex: Exception) {
                    val clientId = conn?.let { getClientId(it) } ?: "全局"
                    if (ex is java.net.BindException) {
                        logE("连接异常 ($clientId): 端口 $port 被占用")
                    } else {
                        logE("连接异常 ($clientId): ${ex.message}")
                    }
                }
            }
            server?.isReuseAddr = true
            server?.start()
        }
    }

    /**
     * 新增：定时清理僵尸连接
     */
    private fun startHeartbeatCheck() {
        scope.launch {
            while (isActive && server != null) {
                delay(15 * 1000) // 每 15 秒检查一次
                val currentTime = System.currentTimeMillis()
                val iterator = heartbeatMap.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (currentTime - entry.value > HEARTBEAT_TIMEOUT) {
                        val clientId = entry.key
                        logE("心跳超时，主动断开僵尸连接: $clientId")
                        clientsMap[clientId]?.close() // 主动断开
                        // 注意：close() 会触发 onClose，在那里会清理 map
                    }
                }
            }
        }
    }

    private suspend fun processMessage(clientId: String, message: String, senderConn: WebSocket) {
        if (message.startsWith("@")) {
            val targetEndIndex = message.indexOf(":")
            if (targetEndIndex > 0) {
                val targetId = message.substring(1, targetEndIndex)
                val realMsg = message.substring(targetEndIndex + 1).trim()
                sendPrivateMessage(targetId, "$clientId 私聊你: $realMsg")
            }
        } else {
            broadcastToOthers("$clientId: $message", senderConn)
        }
    }

    private fun getClientId(conn: WebSocket): String {
        return conn.remoteSocketAddress.address.hostAddress + ":" + conn.remoteSocketAddress.port
    }

    fun sendMessageToClient(clientId: String, message: String) {
        clientsMap[clientId]?.let { conn ->
            if (conn.isOpen) conn.send(message)
        }
    }

    fun sendPrivateMessage(targetClientId: String, message: String) =
        sendMessageToClient(targetClientId, message)

    fun getOnlineCount(): Int = clientsMap.size
    private fun broadcastToOthers(message: String, senderConn: WebSocket) {
        val targetClients = clientsMap.values.filter { it != senderConn && it.isOpen }
        if (targetClients.isNotEmpty()) server?.broadcast(message, targetClients)
    }

    fun stopServer() {
        if (!isStarted()) return
        scope.launch {
            // 增加协程内部的二次校验
            val currentServer = server ?: return@launch
            clientsMap.values.forEach { it.close() }
            clientsMap.clear()
            heartbeatMap.clear()
            currentServer.stop(0)
            server = null
            log("IM 服务端已关闭")
            scope.launch(Dispatchers.Main) { serverListener?.onStopped() }
        }
    }

    fun isStarted(): Boolean = server != null
    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "未知IP"
    }

    fun getConnectUrl(): String = if (server != null) "ws://${getLocalIpAddress()}:$port" else ""
    private fun log(message: String) {
        Log.d(TAG, message)
        scope.launch(Dispatchers.Main) { serverListener?.onLog(message) }
    }

    private fun logE(message: String) {
        Log.e(TAG, message)
        scope.launch(Dispatchers.Main) { serverListener?.onLog(message) }
    }

    fun registerServerListener(serverListener: ServerListener?) {
        this.serverListener = serverListener
    }

    /**
     * 服务端事件监听器
     * 建议将日志和业务消息分开，方便 UI 层做不同处理
     */
    fun unregisterServerListener() {
        this.serverListener = null
    }

    interface ServerListener {
        fun onLog(log: String)
        fun onStarted(url: String)
        fun onStopped()
    }
}