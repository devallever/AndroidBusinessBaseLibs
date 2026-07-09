package app.allever.android.sample.im.websocket.client

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    /**
     * 连接服务端
     * @param url 服务端地址，如 ws://192.168.1.100:8887
     */
    fun connect(url: String) {
        if (client != null && client?.isOpen == true) {
            log("客户端已经连接，请勿重复连接")
            return
        }
        this.currentUrl = url
        this.isManualClose = false
        reconnectJob?.cancel() // 取消可能存在的重连任务
        scope.launch {
            createAndConnect(url)
        }
    }

    /**
     * 创建连接并阻塞直到成功或失败
     */
    private fun createAndConnect(url: String) {
        val uri = URI(url)
        client = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                log("连接服务端成功: $url")
                notifyOpen()
            }

            override fun onMessage(message: String?) {
                notifyMessage(message ?: "")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                log("连接已关闭: code=$code, reason=$reason")
                notifyClose(code, reason, remote)
                // 非主动断开时，触发自动重连
                if (!isManualClose) {
                    tryReconnect()
                }
            }

            override fun onError(ex: Exception?) {
                // 优化：如果 ex 为空，提示未知异常
                logE("连接发生错误: ${ex?.message ?: "未知异常"}")
                notifyError(ex)
                // 发生错误通常意味着连接中断，尝试重连
                if (!isManualClose && client?.isOpen != true) {
                    tryReconnect()
                }
            }
        }
        // 设置心跳保活：15秒内没有发送消息，自动发送一个ping帧
        client?.connectionLostTimeout = 15
        try {
            client?.connectBlocking() // 阻塞直到连接成功或失败
        } catch (e: Exception) {
            logE("连接失败: ${e.message}")
            tryReconnect()
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage(message: String): Boolean {
        if (client != null && client?.isOpen == true) {
            client?.send(message)
            log("发送消息: $message")
            return true
        }
        logE("发送失败：连接未开启")
        return false
    }

    /**
     * 主动断开连接
     */
    fun disconnect() {
        if (!isConnected()) return
        isManualClose = true
        reconnectJob?.cancel() // 取消正在等待的重连任务
        scope.launch {
            // 增加协程内部的二次校验，防止并发空指针
            val currentClient = client ?: return@launch
            currentClient.closeBlocking()
            client = null
            log("客户端已主动断开")
        }
    }

    /**
     * 断线重连机制
     */
    private fun tryReconnect() {
        if (reconnectJob?.isActive == true) return // 已经在重连中，跳过
        reconnectJob = scope.launch {
            delay(3000) // 延迟3秒重连
            if (!isManualClose && currentUrl != null) {
                log("尝试重新连接...")
                currentUrl?.let { url ->
                    createAndConnect(url)
                }
            }
        }
    }

    fun isConnected(): Boolean {
        return client != null && client?.isOpen == true
    }

    fun registerClientListener(listener: ClientListener?) {
        this.clientListener = listener
    }

    fun unregisterClientListener() {
        this.clientListener = null
    }

    // ==================== 回调通知与日志 ====================
    private fun notifyOpen() {
        scope.launch(Dispatchers.Main) { clientListener?.onOpen() }
    }

    private fun notifyMessage(message: String) {
        scope.launch(Dispatchers.Main) { clientListener?.onMessage(message) }
    }

    private fun notifyClose(code: Int, reason: String?, remote: Boolean) {
        scope.launch(Dispatchers.Main) { clientListener?.onClose(code, reason, remote) }
    }

    private fun notifyError(ex: Exception?) {
        scope.launch(Dispatchers.Main) { clientListener?.onError(ex) }
    }

    private fun log(message: String) {
        Log.d(TAG, message)
        scope.launch(Dispatchers.Main) { clientListener?.onLog(message) }
    }

    private fun logE(message: String) {
        Log.e(TAG, message)
        scope.launch(Dispatchers.Main) { clientListener?.onLog(message) }
    }

    /**
     * 客户端事件监听器
     * 所有回调方法已在主线程执行，可直接更新 UI
     */
    interface ClientListener {
        fun onLog(log: String)
        fun onOpen()
        fun onMessage(message: String)
        fun onClose(code: Int, reason: String?, remote: Boolean)
        fun onError(ex: Exception?)
    }
}