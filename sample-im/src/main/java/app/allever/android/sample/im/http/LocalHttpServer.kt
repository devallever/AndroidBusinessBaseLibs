package app.allever.android.sample.im.http

import android.util.Log
import app.allever.android.sample.im.websocket.server.IMWebSocketServer
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Android 本地 HTTP 服务端
 * 设计风格与 IMWebSocketServer 保持一致
 */
object LocalHttpServer {
    private val TAG = LocalHttpServer::class.java.simpleName
    private var server: NanoHTTPD? = null

    @Volatile
    private var port: Int = 8080
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var listener: WeakReference<HttpServerListener>? = null

    fun startServer(port: Int = 8080) {
        if (server != null) {
            log("HTTP 服务已在运行，请勿重复启动")
            return
        }
        synchronized(this) {
            if (server != null) return
            this.port = port

            val newServer = object : NanoHTTPD(port) {
                override fun serve(session: IHTTPSession): Response {
                    log("收到请求: ${session.method} ${session.uri}")
                    return try {
                        handleRequest(session)
                    } catch (e: Exception) {
                        logE("请求处理异常: ${e.message}")
                        buildErrorResponse(500, "服务器内部错误")
                    }
                }
            }

            server = newServer
            scope.launch {
                try {
                    newServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                    val url = getServerUrl()
                    log("HTTP 服务启动成功: $url")
                    scope.launch(Dispatchers.Main) {
                        listener?.get()?.onStarted(url)
                    }
                } catch (e: Exception) {
                    synchronized(this@LocalHttpServer) { server = null }
                    logE("服务启动失败: ${e.message}")
                    scope.launch(Dispatchers.Main) {
                        listener?.get()?.onError(e.message ?: "启动失败")
                    }
                }
            }
        }
    }

    /**
     * 统一请求分发
     */
    private fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        // 处理跨域预检
        if (session.method == NanoHTTPD.Method.OPTIONS) {
            return buildSuccessResponse("")
        }

        return when (session.uri) {
            "/" -> buildSuccessResponse("Android Local HTTP Server is running.")
            "/api/status" -> handleStatus()
            "/api/echo" -> handleEcho(session)
            "/api/user" -> handleUserInfo(session)
            else -> buildErrorResponse(404, "接口不存在")
        }
    }

    private fun handleStatus(): NanoHTTPD.Response {
        val data = JSONObject().apply {
            put("port", port)
            put("online_client", IMWebSocketServer.getOnlineCount())
            put("timestamp", System.currentTimeMillis())
        }
        return buildSuccessResponse(data.toString())
    }

    private fun handleEcho(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val text = session.parms["text"] ?: "空内容"
        return buildSuccessResponse("你发送了: $text")
    }

    private fun handleUserInfo(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.POST) {
            return buildErrorResponse(405, "仅支持 POST 请求")
        }
        val body = session.inputStream.bufferedReader().readText()
        val json = JSONObject(body)
        val userId = json.optString("userId")

        val result = JSONObject().apply {
            put("userId", userId)
            put("nickname", "用户_$userId")
            put("level", 1)
        }
        return buildSuccessResponse(result.toString())
    }

    fun stopServer() {
        if (server == null) return
        scope.launch {
            try {
                server?.stop()
            } catch (e: Exception) {
                logE("停止服务异常: ${e.message}")
            } finally {
                server = null
                log("HTTP 服务已停止")
                scope.launch(Dispatchers.Main) {
                    listener?.get()?.onStopped()
                }
            }
        }
    }

    fun isRunning(): Boolean = server != null

    fun getServerUrl(): String {
        return if (server != null) "http://${getLocalIp()}:$port" else ""
    }

    private fun getLocalIp(): String {
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
        return "127.0.0.1"
    }

    // ========== 响应工具 ==========
    private fun buildSuccessResponse(data: String): NanoHTTPD.Response {
        return NanoHTTPD.newFixedLengthResponse(
            NanoHTTPD.Response.Status.OK,
            "application/json; charset=utf-8",
            JSONObject().apply {
                put("code", 200)
                put("msg", "success")
                put("data", data)
            }.toString()
        ).apply { addCorsHeaders(this) }
    }

    private fun buildErrorResponse(code: Int, msg: String): NanoHTTPD.Response {
        val status = when (code) {
            404 -> NanoHTTPD.Response.Status.NOT_FOUND
            405 -> NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED
            else -> NanoHTTPD.Response.Status.INTERNAL_ERROR
        }
        return NanoHTTPD.newFixedLengthResponse(
            status,
            "application/json; charset=utf-8",
            JSONObject().apply {
                put("code", code)
                put("msg", msg)
            }.toString()
        ).apply { addCorsHeaders(this) }
    }

    private fun addCorsHeaders(response: NanoHTTPD.Response) {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
    }

    // ========== 日志与监听 ==========
    private fun log(msg: String) {
        Log.d(TAG, msg)
        scope.launch(Dispatchers.Main) { listener?.get()?.onLog(msg) }
    }

    private fun logE(msg: String) {
        Log.e(TAG, msg)
        scope.launch(Dispatchers.Main) { listener?.get()?.onLog(msg) }
    }

    fun registerListener(l: HttpServerListener?) {
        listener = l?.let { WeakReference(it) }
    }

    fun unregisterListener() {
        listener = null
    }

    interface HttpServerListener {
        fun onLog(log: String)
        fun onStarted(url: String)
        fun onStopped()
        fun onError(msg: String)
    }
}