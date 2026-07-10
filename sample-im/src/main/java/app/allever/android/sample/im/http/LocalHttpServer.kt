package app.allever.android.sample.im.http

import android.util.Log
import app.allever.android.sample.im.websocket.server.IMWebSocketServer
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Android 本地 HTTP 服务端
 * 修复：编码问题、重复Content-Type、JSON结构统一
 */
object LocalHttpServer {
    private val TAG = LocalHttpServer::class.java.simpleName
    private var server: NanoHTTPD? = null

    @Volatile
    private var port: Int = 8080
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var listener: WeakReference<HttpServerListener>? = null

    private object BizCode {
        const val SUCCESS = 0
        const val BAD_REQUEST = 400
        const val NOT_FOUND = 404
        const val METHOD_NOT_ALLOWED = 405
        const val SERVER_ERROR = 500
    }

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
                        buildJsonResponse(
                            httpStatus = Response.Status.INTERNAL_ERROR,
                            bizCode = BizCode.SERVER_ERROR,
                            msg = "服务器内部错误"
                        )
                    }
                }
            }

            server = newServer
            scope.launch {
                try {
                    newServer.start(SOCKET_READ_TIMEOUT, false)
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

    private fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method == NanoHTTPD.Method.OPTIONS) {
            return buildSuccessResponse()
        }

        return when (session.uri) {
            "/" -> buildSuccessResponse(JSONObject().put("message", "Android Local HTTP Server is running."))
            "/api/status" -> handleStatus()
            "/api/echo" -> handleEcho(session)
            "/api/user" -> handleUserInfo(session)
            else -> buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.NOT_FOUND,
                bizCode = BizCode.NOT_FOUND,
                msg = "接口不存在"
            )
        }
    }

    private fun handleStatus(): NanoHTTPD.Response {
        val data = JSONObject().apply {
            put("port", port)
            put("online_client", IMWebSocketServer.getOnlineCount())
            put("timestamp", System.currentTimeMillis())
        }
        return buildSuccessResponse(data)
    }

    private fun handleEcho(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val text = session.parms["text"] ?: "空内容"
        return buildSuccessResponse(JSONObject().put("text", "你发送了: $text"))
    }

    private fun handleUserInfo(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.POST) {
            return buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = BizCode.METHOD_NOT_ALLOWED,
                msg = "仅支持 POST 请求"
            )
        }

        val body = parseJsonBody(session)
            ?: return buildJsonResponse(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = BizCode.BAD_REQUEST,
                msg = "请求体必须为合法 JSON"
            )

        val userId = body.optString("userId", "")
        val result = JSONObject().apply {
            put("userId", userId)
            put("nickname", "用户_$userId")
            put("level", 1)
        }
        return buildSuccessResponse(result)
    }

    // ====================== 响应构建（核心修复） ======================
    private fun buildSuccessResponse(): NanoHTTPD.Response {
        return buildJsonResponse(bizCode = BizCode.SUCCESS, msg = "success")
    }

    private fun buildSuccessResponse(data: JSONObject): NanoHTTPD.Response {
        return buildJsonResponse(bizCode = BizCode.SUCCESS, msg = "success", data = data)
    }

    private fun buildSuccessResponse(data: JSONArray): NanoHTTPD.Response {
        return buildJsonResponse(bizCode = BizCode.SUCCESS, msg = "success", data = data)
    }

    /**
     * 统一 JSON 响应底层方法
     * 关键：手动转 UTF-8 字节数组，用 InputStream 返回，编码和长度 100% 可控
     */
    private fun buildJsonResponse(
        httpStatus: NanoHTTPD.Response.Status = NanoHTTPD.Response.Status.OK,
        bizCode: Int,
        msg: String,
        data: Any? = null
    ): NanoHTTPD.Response {
        // 1. 构造完整 JSON
        val jsonBody = JSONObject().apply {
            put("code", bizCode)
            put("msg", msg)
            put("data", data ?: JSONObject.NULL)
        }.toString()

        // 2. 手动转 UTF-8 字节数组，编码完全可控
        val bytes = jsonBody.toByteArray(Charsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        // 3. 用 InputStream 重载返回，指定准确长度
        return NanoHTTPD.newFixedLengthResponse(
            httpStatus,
            "application/json",
            inputStream,
            bytes.size.toLong()
        ).apply {
            // 只在这里设置一次 charset，不再重复 addHeader
            addHeader("Content-Type", "application/json; charset=utf-8")
            // 跨域头
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        }
    }

    // ====================== 请求解析工具 ======================
    private fun parseJsonBody(session: NanoHTTPD.IHTTPSession): JSONObject? {
        return try {
            val body = session.inputStream.bufferedReader(Charsets.UTF_8).readText()
            if (body.isBlank()) null else JSONObject(body)
        } catch (e: Exception) {
            null
        }
    }

    // ====================== 生命周期 ======================
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

    // ====================== 日志与监听 ======================
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