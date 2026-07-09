package app.allever.android.sample.im.websocket.client

import android.util.Log
import kotlinx.coroutines.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * WebSocket 客户端单例
 */
object IMWebSocketClient {
    private val TAG = IMWebSocketClient::class.java.simpleName
    private var client: WebSocketClient? = null

    // 创建单例专属的 CoroutineScope
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 增加 @Volatile 保证多线程下的可见性
    @Volatile
    private var clientListener: ClientListener? = null

    // 记录当前连接的目标地址，用于断线重连
    @Volatile
    private var currentUrl: String? = null

    // 标记是否为主动断开（主动断开则不触发自动重连）
    @Volatile
    private var isManualClose = false

    // 重连任务Job，用于取消重连
    private var reconnectJob: Job? = null
    // 新增：心跳相关
    private var heartbeatJob: Job? = null
    @Volatile
    private var lastPongTime: Long = 0L
    private const val HEARTBEAT_INTERVAL = 30 * 1000L  // 30秒发一次心跳
    private const val HEARTBEAT_TIMEOUT = 90 * 1000L   // 90秒没收到回复视为断线
    private const val HEARTBEAT_PING = "HEARTBEAT_PING"
    private const val HEARTBEAT_PONG = "HEARTBEAT_PONG"
    fun connect(url: String) {
        if (client != null && client?.isOpen == true) {
            log("客户端已经连接，请勿重复连接")
            return
        }
        this.currentUrl = url
        this.isManualClose = false
        reconnectJob?.cancel()
        scope.launch { createAndConnect(url) }
    }
    private fun createAndConnect(url: String) {
        val uri = URI(url)
        client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                log("连接服务端成功: $url")
                lastPongTime = System.currentTimeMillis() // 初始化时间
                startHeartbeat() // 开启心跳
                notifyOpen()
            }
            override fun onMessage(message: String?) {
                if (message == HEARTBEAT_PONG) {
                    lastPongTime = System.currentTimeMillis()
                    return // 心跳回复不抛给 UI 层
                }
                log("收到服务端消息: $message")
                notifyMessage(message ?: "")
            }
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                log("连接已关闭: code=$code, reason=$reason")
                heartbeatJob?.cancel() // 停止心跳
                notifyClose(code, reason, remote)
                if (!isManualClose) tryReconnect()
            }
            override fun onError(ex: Exception?) {
                logE("连接发生错误: ${ex?.message ?: "未知异常"}")
                notifyError(ex)
                if (!isManualClose && client?.isOpen != true) tryReconnect()
            }
        }
        try {
            client?.connectBlocking()
        } catch (e: Exception) {
            logE("连接失败: ${e.message}")
            tryReconnect()
        }
    }
    /**
     * 新增：客户端发送心跳并检测超时
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive && !isManualClose) {
                delay(HEARTBEAT_INTERVAL)
                if (client != null && client?.isOpen == true) {
                    client?.send(HEARTBEAT_PING)
                    Log.d(TAG, "发送心跳: $HEARTBEAT_PING")
                    // 检查是否超时
                    if (System.currentTimeMillis() - lastPongTime > HEARTBEAT_TIMEOUT) {
                        logE("心跳超时，服务端无响应，触发重连")
                        client?.close() // 主动关闭会触发 onClose -> tryReconnect
                        break
                    }
                }
            }
        }
    }
    fun sendMessage(message: String): Boolean {
        if (client != null && client?.isOpen == true) {
            client?.send(message)
            log("发送消息: $message")
            return true
        }
        logE("发送失败：连接未开启")
        return false
    }
    fun disconnect() {
        if (!isConnected()) return
        isManualClose = true
        reconnectJob?.cancel()
        heartbeatJob?.cancel() // 停止心跳
        scope.launch {
            val currentClient = client ?: return@launch
            currentClient.closeBlocking()
            client = null
            log("客户端已主动断开")
        }
    }
    private fun tryReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(3000)
            if (!isManualClose && currentUrl != null) {
                log("尝试重新连接...")
                currentUrl?.let { createAndConnect(it) }
            }
        }
    }
    fun isConnected(): Boolean = client != null && client?.isOpen == true
    fun registerClientListener(listener: ClientListener?) { this.clientListener = listener }
    fun unregisterClientListener() { this.clientListener = null }
    private fun notifyOpen() { scope.launch(Dispatchers.Main) { clientListener?.onOpen() } }
    private fun notifyMessage(message: String) { scope.launch(Dispatchers.Main) { clientListener?.onMessage(message) } }
    private fun notifyClose(code: Int, reason: String?, remote: Boolean) { scope.launch(Dispatchers.Main) { clientListener?.onClose(code, reason, remote) } }
    private fun notifyError(ex: Exception?) { scope.launch(Dispatchers.Main) { clientListener?.onError(ex) } }
    private fun log(message: String) { Log.d(TAG, message); scope.launch(Dispatchers.Main) { clientListener?.onLog(message) } }
    private fun logE(message: String) { Log.e(TAG, message); scope.launch(Dispatchers.Main) { clientListener?.onLog(message) } }
    interface ClientListener {
        fun onLog(log: String)
        fun onOpen()
        fun onMessage(message: String)
        fun onClose(code: Int, reason: String?, remote: Boolean)
        fun onError(ex: Exception?)
    }
}