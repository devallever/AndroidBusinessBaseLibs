package app.allever.android.sample.im.http

import android.util.Log
import app.allever.android.sample.im.http.request.UserInfoRequest
import app.allever.android.sample.im.http.response.EchoData
import app.allever.android.sample.im.http.response.MessageData
import app.allever.android.sample.im.http.response.StatusData
import app.allever.android.sample.im.http.response.UserInfoData
import app.allever.android.sample.im.websocket.server.IMWebSocketServer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Android 本地 HTTP 服务端 - Gson 版
 * 彻底解决 org.json 整数转浮点数问题，类型严格可控
 */
object LocalHttpServer {
    private val TAG = LocalHttpServer::class.java.simpleName
    private var server: NanoHTTPD? = null

    @Volatile
    private var port: Int = 8080
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var listener: WeakReference<HttpServerListener>? = null

    // 全局 Gson 实例：开启 null 序列化，保证响应结构永远统一
    private val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

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
                        buildJsonResponse<Any?>(
                            httpStatus = Response.Status.INTERNAL_ERROR,
                            bizCode = BizCode.SERVER_ERROR,
                            msg = "服务器内部错误",
                            data = null
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

    // ====================== 路由分发 ======================
    private fun handleRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        // 跨域预检：显式指定泛型
        if (session.method == NanoHTTPD.Method.OPTIONS) {
            return buildSuccessResponse<Any?>(null)
        }

        return when (session.uri) {
            "/" -> buildSuccessResponse(MessageData("Android Local HTTP Server is running."))
            "/api/status" -> handleStatus()
            "/api/echo" -> handleEcho(session)
            "/api/user" -> handleUserInfo(session)
            else -> buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.NOT_FOUND,
                bizCode = BizCode.NOT_FOUND,
                msg = "接口不存在",
                data = null
            )
        }
    }

    // ====================== 业务接口 ======================
    private fun handleStatus(): NanoHTTPD.Response {
        val data = StatusData(
            port = port,
            online_client = IMWebSocketServer.getOnlineCount(),
            timestamp = System.currentTimeMillis()
        )
        return buildSuccessResponse(data)
    }

    private fun handleEcho(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val text = session.parms["text"] ?: "空内容"
        return buildSuccessResponse(EchoData("你发送了: $text"))
    }

    private fun handleUserInfo(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        if (session.method != NanoHTTPD.Method.POST) {
            return buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                bizCode = BizCode.METHOD_NOT_ALLOWED,
                msg = "仅支持 POST 请求",
                data = null
            )
        }

        val request = parseJsonBody<UserInfoRequest>(session)
            ?: return buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.BAD_REQUEST,
                bizCode = BizCode.BAD_REQUEST,
                msg = "请求体必须为合法 JSON",
                data = null
            )

        val userId = request.userId
        val result = UserInfoData(
            userId = userId,
            nickname = "用户_$userId",
            level = 1
        )
        return buildSuccessResponse(result)
    }

    // ====================== 统一响应构建 ======================
    /**
     * 成功响应：携带数据对象
     */
//    private fun <T> buildSuccessResponse(data: T): NanoHTTPD.Response {
//        return buildJsonResponse(bizCode = BizCode.SUCCESS, msg = "success", data = data)
//    }

    /**
     * 成功响应：无数据，显式指定泛型
     */
    private fun <T> buildSuccessResponse(data: T? = null): NanoHTTPD.Response {
        return buildJsonResponse(bizCode = BizCode.SUCCESS, msg = "success", data = data)
    }

    /**
     * 统一 JSON 响应底层方法
     * 基于 BaseResponse 结构 + Gson 序列化，编码与长度完全可控
     */
    private fun <T> buildJsonResponse(
        httpStatus: NanoHTTPD.Response.Status = NanoHTTPD.Response.Status.OK,
        bizCode: Int,
        msg: String,
        data: T? = null
    ): NanoHTTPD.Response {
        // 1. 构造标准响应结构，Gson 序列化为 JSON 字符串
        val response = ServerResponse(bizCode, msg, data)
        val jsonBody = gson.toJson(response)

        // 2. 手动转 UTF-8 字节数组，编码 100% 可控
        val bytes = jsonBody.toByteArray(Charsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        // 3. 流式返回，指定准确长度
        return NanoHTTPD.newFixedLengthResponse(
            httpStatus,
            "application/json",
            inputStream,
            bytes.size.toLong()
        ).apply {
            // 只设置一次 Content-Type，避免重复头
            addHeader("Content-Type", "application/json; charset=utf-8")
            // 跨域头
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
        }
    }

    // ====================== 请求解析工具 ======================
    /**
     * 通用解析 POST JSON 请求体
     * @param T 目标数据类型
     * @return 解析成功返回实体，失败返回 null
     */
    private inline fun <reified T> parseJsonBody(session: NanoHTTPD.IHTTPSession): T? {
        return try {
            val body = session.inputStream.bufferedReader(Charsets.UTF_8).readText()
            if (body.isBlank()) null
            else gson.fromJson(body, object : TypeToken<T>() {}.type)
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