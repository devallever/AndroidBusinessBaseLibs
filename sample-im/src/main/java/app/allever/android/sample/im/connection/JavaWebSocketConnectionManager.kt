package app.allever.android.sample.im.connection

import android.util.Log
import app.allever.android.lib.core.ext.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlin.math.pow

object JavaWebSocketConnectionManager {

    private var url: String? = null
    private var connectionCallback: ConnectionCallback? = null


    private var webSocketClient: WebSocketClient? = null

    // 使用协程管理重连逻辑
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isManualClose = false
    private var retryCount = 0

    // 初始化并配置WebSocketClient
    private fun initClient() {
    }

    // 建立连接
    fun connect(url: String) {
        isManualClose = false
        if (webSocketClient == null) initClient()

        // 避免重复连接
        if (webSocketClient?.isOpen == true || webSocketClient?.connectBlocking() == true) return

        try {

            val uri = URI(url)

            webSocketClient = object : WebSocketClient(uri) {
                // 连接成功回调
                override fun onOpen(handshakedata: ServerHandshake?) {
                    log("WebSocket", "连接成功")
                    retryCount = 0
                    connectionCallback?.onConnectionStateChanged(true)
                }

                // 接收到文本消息回调（IM消息、信令等）
                override fun onMessage(message: String?) {
                    message?.let { connectionCallback?.onMessageReceived(it) }
                    //log
                    log("WebSocket", "接收到消息：$message")
                }

                // 接收到二进制消息回调（如语音流数据片段，若使用WebSocket传输）
                override fun onMessage(bytes: java.nio.ByteBuffer?) {
                    // 可处理二进制数据
                    bytes?.let { connectionCallback?.onMessageReceived(it.array()) }
                    log("WebSocket", "接收到二进制消息")
                }

                // 连接关闭回调
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d("WebSocket", "连接关闭: $reason")
                    connectionCallback?.onConnectionStateChanged(false)
                    if (!isManualClose) retryConnect()
                    //log
                    log("WebSocket", "连接关闭: $reason")
                }

                // 发生异常回调
                override fun onError(ex: Exception?) {
                    //log
                    log("WebSocket", "发生异常: ${ex?.message}")
                    // 注意：框架的onError触发后，通常也会触发onClose，重连逻辑放在onClose处理即可
                }
            }

            // ===== 关键配置：心跳保活 =====
            // 该框架自带心跳机制，会自动发送Ping/Pong控制帧维持长连接
            webSocketClient?.connectionLostTimeout = 30 // 30秒发送一次心跳

            webSocketClient?.connectBlocking() // 阻塞式连接，因为我们在IO协程或线程中执行
        } catch (e: Exception) {
            e.printStackTrace()
            retryConnect()
        }
    }

    // 发送消息
    fun send(msg: String) {
        if (webSocketClient?.isOpen == true) {
            webSocketClient?.send(msg) ?: false
        }
    }

    // 断线重连（指数退避算法）
    private fun retryConnect() {
        scope.launch {
            val delayTime = (3000L * (2.0.pow(retryCount.coerceAtMost(5).toDouble()))).toLong()
            delay(delayTime)
            retryCount++
            Log.d("WebSocket", "尝试第 $retryCount 次重连...")
            url?.let { connect(it) }
        }
    }

    // 主动断开连接
    fun disconnect() {
        isManualClose = true
        scope.cancel() // 取消所有协程任务
        try {
            webSocketClient?.closeBlocking()
            webSocketClient = null
            retryCount = 0
            url = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}