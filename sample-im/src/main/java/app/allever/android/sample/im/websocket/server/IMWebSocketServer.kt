package app.allever.android.sample.im.websocket.server

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    // 增加 @Volatile 保证多线程下的可见性
    @Volatile
    private var port: Int = 5400
    private val clientsMap = ConcurrentHashMap<String, WebSocket>()

    // 创建单例专属的 CoroutineScope
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 增加 @Volatile 保证多线程下的可见性
    @Volatile
    private var serverListener: ServerListener? = null

    /**
     * 初始化并启动服务端
     * @param port 传入的监听端口号
     */
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
//                    server?.address?.address?.hostAddress ?: "未知" // ws://:::5400
                    val url = getConnectUrl()
                    log("IM 服务端已启动，监听地址: $url")
                    scope.launch(Dispatchers.Main) {
                        serverListener?.onStarted(url)
                    }
                }

                override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
                    val clientId = getClientId(conn)
                    clientsMap[clientId] = conn
                    log("客户端连接成功: $clientId, 当前在线人数: ${clientsMap.size}")
                    sendMessageToClient(clientId, "系统消息：欢迎加入聊天室！")
                    broadcastToOthers("系统消息：$clientId 上线了", conn)
                }

                override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
                    val clientId = getClientId(conn)
                    clientsMap.remove(clientId)
                    log("客户端断开连接: $clientId, 剩余在线人数: ${clientsMap.size}")
                    broadcastToOthers("系统消息：$clientId 离线了", conn)
                }

                override fun onMessage(conn: WebSocket, message: String) {
                    val clientId = getClientId(conn)
                    log("收到 $clientId 的消息: $message")
                    scope.launch {
                        processMessage(clientId, message, conn)
                    }
                }

                override fun onError(conn: WebSocket?, ex: Exception) {
                    // 优化：如果 conn 为空，提示“全局异常”
                    val clientId = conn?.let { getClientId(it) } ?: "全局"
                    logE("连接异常 ($clientId): ${ex.message}")
                }
            }
            server?.start()
        }
    }

    fun registerServerListener(serverListener: ServerListener?) {
        this.serverListener = serverListener
    }

    fun unregisterServerListener() {
        this.serverListener = null
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

    // ==================== 核心连接管理方法 ====================

    private fun getClientId(conn: WebSocket): String {
        return conn.remoteSocketAddress.address.hostAddress + ":" + conn.remoteSocketAddress.port
    }

    fun sendMessageToClient(clientId: String, message: String) {
        clientsMap[clientId]?.let { conn ->
            if (conn.isOpen) {
                conn.send(message)
            }
        }
    }

    fun sendPrivateMessage(targetClientId: String, message: String) {
        sendMessageToClient(targetClientId, message)
    }

    fun getOnlineCount(): Int = clientsMap.size

    private fun broadcastToOthers(message: String, senderConn: WebSocket) {
        val targetClients = clientsMap.values.filter { it != senderConn && it.isOpen }
        if (targetClients.isNotEmpty()) {
            server?.broadcast(message, targetClients)
        }
    }

    fun stopServer() {
        if (!isStarted()) return
        scope.launch {
            // 增加协程内部的二次校验
            val currentServer = server ?: return@launch

            clientsMap.values.forEach { it.close() }
            clientsMap.clear()
            currentServer.stop(0)
            server = null

            log("IM 服务端已关闭")
            scope.launch(Dispatchers.Main) {
                serverListener?.onStopped()
            }
        }
    }

    fun isStarted(): Boolean {
        return server != null
    }

    private fun getLocalIpAddress(): String {
        try {
            // 遍历所有网络接口
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                // 排除回环接口和未启用的接口
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val inetAddress = addresses.nextElement()
                    // 只要 IPv4 地址，且不是回环地址
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

    fun getConnectUrl(): String {
        if (server != null) {
            val url = "ws://${getLocalIpAddress()}:$port"
            return url
        } else {
            return ""
        }
    }

    // ==================== 日志与回调 ====================

    private fun log(message: String) {
        Log.d(TAG, message)
        // 切换到主线程回调，防止调用者直接更新 UI 导致崩溃
        scope.launch(Dispatchers.Main) {
            serverListener?.onLog(message)
        }
    }

    private fun logE(message: String) {
        Log.e(TAG, message)
        scope.launch(Dispatchers.Main) {
            serverListener?.onLog(message)
        }
    }

    /**
     * 服务端事件监听器
     * 建议将日志和业务消息分开，方便 UI 层做不同处理
     */
    interface ServerListener {
        /**
         * 接收服务端运行日志（已在主线程回调）
         */
        fun onLog(log: String)
        fun onStarted(url: String)
        fun onStopped()
    }
}