package app.allever.android.sample.im.http

import android.util.Log
import app.allever.android.sample.im.http.handler.EchoHandler
import app.allever.android.sample.im.http.handler.LoginHandler
import app.allever.android.sample.im.http.handler.LogoutHandler
import app.allever.android.sample.im.http.handler.OnlineUserListHandler
import app.allever.android.sample.im.http.handler.RegisterHandler
import app.allever.android.sample.im.http.handler.RootHandler
import app.allever.android.sample.im.http.handler.StatusHandler
import app.allever.android.sample.im.http.handler.UserInfoHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Android 本地 HTTP 服务端 - Handler 解耦版
 * 新增：统一请求/响应日志打印，回调 onLog
 */
object LocalHttpServer {
    private val TAG = LocalHttpServer::class.java.simpleName
    private var server: NanoHTTPD? = null

    @Volatile
    var port: Int = 8080
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var listener: WeakReference<HttpServerListener>? = null

    internal val gson: Gson = GsonBuilder()
        .serializeNulls()
        .create()

    private val routeMap = mutableMapOf<String, HttpRequestHandler>()

    private object BizCode {
        const val SUCCESS = 0
        const val BAD_REQUEST = 400
        const val NOT_FOUND = 404
        const val SERVER_ERROR = 500
    }

    init {
        registerHandler(RootHandler())
        registerHandler(StatusHandler())
        registerHandler(EchoHandler())
        registerHandler(UserInfoHandler())
        registerHandler(RegisterHandler())
        registerHandler(LoginHandler())
        registerHandler(LogoutHandler())
        registerHandler(OnlineUserListHandler())
    }

    fun registerHandler(handler: HttpRequestHandler) {
        routeMap[handler.path] = handler
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
                    return try {
                        dispatchRequest(session)
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

    /**
     * 请求分发 + 统一请求日志
     */
    private fun dispatchRequest(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val method = session.method
        val fullUri = session.uri
        // 纯路径部分，用于路由匹配
        val path = fullUri.substringBefore("?")
        // 查询参数部分，用于日志打印
        val query = fullUri.substringAfter("?", missingDelimiterValue = "")

        if (query.isNotEmpty()) {
            log("[请求] $method $fullUri")
        } else {
            log("[请求] $method $fullUri")
        }

        if (method == NanoHTTPD.Method.OPTIONS) {
            return buildSuccessResponse<Any?>(null)
        }

        // 用纯路径匹配路由
        val handler = routeMap[path]
        return handler?.handle(session)
            ?: buildJsonResponse<Any?>(
                httpStatus = NanoHTTPD.Response.Status.NOT_FOUND,
                bizCode = BizCode.NOT_FOUND,
                msg = "接口不存在",
                data = null
            )
    }

    // ====================== 统一响应构建 ======================
    internal fun <T> buildSuccessResponse(data: T? = null): NanoHTTPD.Response {
        return buildJsonResponse(bizCode = BizCode.SUCCESS, msg = "success", data = data)
    }

    /**
     * 统一响应出口 + 响应日志打印
     */
    internal fun <T> buildJsonResponse(
        httpStatus: NanoHTTPD.Response.Status = NanoHTTPD.Response.Status.OK,
        bizCode: Int,
        msg: String,
        data: T? = null
    ): NanoHTTPD.Response {
        val response = ServerResponse(bizCode, msg, data)
        val jsonBody = gson.toJson(response)

        // 2. 打印响应内容
        log("[响应] $httpStatus $jsonBody")

        val bytes = jsonBody.toByteArray(Charsets.UTF_8)
        val inputStream = ByteArrayInputStream(bytes)

        return NanoHTTPD.newFixedLengthResponse(
            httpStatus,
            "application/json",
            inputStream,
            bytes.size.toLong()
        ).apply {
            addHeader("Content-Type", "application/json; charset=utf-8")
            addHeader("Access-Control-Allow-Origin", "*")
            addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
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

    // ====================== 日志（改为 internal，供同包扩展函数调用） ======================
    internal fun log(msg: String) {
        Log.d(TAG, msg)
        scope.launch(Dispatchers.Main) { listener?.get()?.onLog(msg) }
    }

    internal fun logE(msg: String) {
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