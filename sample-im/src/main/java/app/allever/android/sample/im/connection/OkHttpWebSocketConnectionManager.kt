package app.allever.android.sample.im.connection

import app.allever.android.lib.core.ext.log
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

object OkHttpWebSocketConnectionManager {

    private val connectionCallback: ConnectionCallback? = null
    private var url: String? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, java.util.concurrent.TimeUnit.SECONDS) // OkHttp底层心跳保活
        .build()

    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isManualClose = false
    private var retryCount = 0

    // 连接WebSocket
    fun connect(url: String) {
        this.url = url
        isManualClose = false
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                retryCount = 0
                connectionCallback?.onConnectionStateChanged(true)
                log("OkHttpWebSocketConnectionManager", "连接成功")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // 接收到文本消息（如IM JSON协议、信令）
                connectionCallback?.onMessageReceived(text)
                //log
                log("OkHttpWebSocketConnectionManager", "接收到文本消息：$text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // 接收到二进制消息（如语音流数据片段）
                bytes.toByteArray().let {
                    // 处理二进制数据
                    connectionCallback?.onMessageReceived(it)
                }
                log("OkHttpWebSocketConnectionManager", "接收到二进制消息：${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                log("OkHttpWebSocketConnectionManager", "连接关闭")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connectionCallback?.onConnectionStateChanged(false)
                if (!isManualClose) retryConnect()
                log("OkHttpWebSocketConnectionManager", "连接关闭")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connectionCallback?.onConnectionStateChanged(false)
                if (!isManualClose) retryConnect()
                log("OkHttpWebSocketConnectionManager", "连接失败")
            }
        })
    }

    // 发送消息
    fun send(msg: String): Boolean {
        return webSocket?.send(msg) ?: false
    }

    // 断线重连机制（指数退避）
    private fun retryConnect() {
        scope.launch {
            val delayTime = (3000L * (Math.pow(2.0, retryCount.coerceAtMost(5).toDouble()))).toLong()
            delay(delayTime)
            retryCount++
            url?.let { connect(it) }
        }
    }

    // 主动关闭连接
    fun disconnect() {
        isManualClose = true
        webSocket?.close(1000, "Client closed")
        scope.cancel()
        webSocket = null
        url = null
    }
}