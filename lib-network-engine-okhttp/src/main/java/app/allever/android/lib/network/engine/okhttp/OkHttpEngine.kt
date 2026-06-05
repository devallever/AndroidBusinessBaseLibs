package app.allever.android.lib.network.engine.okhttp

import app.allever.android.lib.network.core.engine.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * OkHttp 引擎实现
 *
 * 基于 Square OkHttp 实现 HttpEngine 接口。
 * 利用 OkHttp 的连接池、HTTP/2、拦截器等原生能力。
 *
 * 自动注册到 EngineRegistry，名称为 "okhttp"
 *
 * 使用方式：
 * ```kotlin
 * Network.init {
 *     baseUrl("https://api.example.com")
 *     engine("okhttp") {
 *         connectTimeout(10_000)
 *         readTimeout(15_000)
 *         (this as? OkHttpConfig)?.apply {
 *             retryOnConnectionFailure(true)
 *         }
 *     }
 * }
 * ```
 */
class OkHttpEngine(private val config: OkHttpConfig) : HttpEngine {

    override val engineName: String = ENGINE_NAME

    /** OkHttp 客户端实例（懒加载，支持配置热更新） */
    @Volatile
    private var _client: okhttp3.OkHttpClient? = null

    private val client: okhttp3.OkHttpClient
        get() {
            if (_client == null) {
                synchronized(this) {
                    if (_client == null) {
                        _client = buildClient()
                    }
                }
            }
            return _client!!
        }

    override fun execute(request: NetRequest): NetResponse {
        val startTime = System.currentTimeMillis()

        try {
            val okRequest = buildOkHttpRequest(request)
            val okResponse = client.newCall(okRequest).execute()

            return convertResponse(okResponse, request, startTime)

        } catch (e: Exception) {
            throw mapException(e)
        }
    }

    override fun newCall(request: NetRequest): NetCall {
        return OkHttpCall(client.newCall(buildOkHttpRequest(request)), request)
    }

    override fun shutdown() {
        _client?.dispatcher?.executorService?.shutdown()
        _client?.connectionPool?.evictAll()
        _client = null
    }

    // ==================== 内部实现 ====================

    /**
     * 构建 OkHttpClient 实例
     */
    private fun buildClient(): okhttp3.OkHttpClient {
        return okhttp3.OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMs, TimeUnit.MILLISECONDS)
            .writeTimeout(config.writeTimeoutMs, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(config.retryOnConnectionFailure)
            .also { builder ->
                config.connectionPool?.let { builder.connectionPool(it) }
                config.protocols?.let { builder.protocols(it) }
            }
            .build()
    }

    /**
     * 将 NetRequest 转换为 okhttp3.Request
     */
    private fun buildOkHttpRequest(request: NetRequest): Request {
        val builder = Request.Builder()
            .url(request.url)

        // 请求方法 + 请求体
        when (request.method) {
            HttpMethod.GET -> builder.get()
            HttpMethod.HEAD -> builder.head()
            HttpMethod.DELETE -> {
                val body = request.body?.toOkHttpRequestBody()
                if (body != null) builder.delete(body) else builder.delete()
            }
            else -> {
                // POST / PUT / PATCH
                builder.method(
                    request.method.name,
                    request.body?.toOkHttpRequestBody()
                )
            }
        }

        // 请求头
        for ((key, value) in request.headers) {
            builder.addHeader(key, value)
        }

        // tag（用于取消等）
        request.tag?.let { builder.tag(it) }

        return builder.build()
    }

    /**
     * 将 NetBody 转换为 okhttp3.RequestBody
     */
    private fun NetBody.toOkHttpRequestBody(): okhttp3.RequestBody? {
        return try {
            val outputStream = ByteArrayOutputStream()
            this.writeTo(outputStream)
            val bytes = outputStream.toByteArray()

            val mediaType = this.contentType?.toMediaTypeOrNull()
            bytes.toRequestBody(mediaType)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 将 okhttp3.Response 转换为 NetResponse
     */
    private fun convertResponse(
        okResponse: okhttp3.Response,
        originalRequest: NetRequest,
        startTime: Long
    ): NetResponse {
        val body = okResponse.body?.bytes()
        val headers = mutableMapOf<String, String>()

        okResponse.headers.names().forEach { name ->
            okResponse.headers.values(name).firstOrNull()?.let { value ->
                headers[name] = value
            }
        }

        return NetResponse(
            code = okResponse.code,
            message = okResponse.message,
            headers = headers,
            body = body,
            request = originalRequest,
            contentLength = okResponse.body?.contentLength() ?: -1L,
            elapsedMs = System.currentTimeMillis() - startTime
        )
    }

    /**
     * 将原始异常映射为更有意义的异常类型
     */
    private fun mapException(e: Exception): Exception {
        return when (e) {
            is java.net.SocketTimeoutException -> e
            is java.net.ConnectException -> e
            is java.net.UnknownHostException -> e
            is javax.net.ssl.SSLException -> e
            is java.io.IOException -> e
            else -> java.io.IOException("网络请求失败: ${e.message}", e)
        }
    }

    companion object {
        /** 引擎名称（非 const，避免编译期内联导致类不被加载） */
        val ENGINE_NAME = "okhttp"

        /**
         * 模块被引用时自动注册到 EngineRegistry
         */
        init {
            EngineRegistry.register(ENGINE_NAME) { rawConfig ->
                when (rawConfig) {
                    is OkHttpConfig -> rawConfig
                    else -> OkHttpConfig().also {
                        it.connectTimeoutMs = rawConfig.connectTimeoutMs
                        it.readTimeoutMs = rawConfig.readTimeoutMs
                        it.writeTimeoutMs = rawConfig.writeTimeoutMs
                    }
                }.let { OkHttpEngine(it) }
            }
        }
    }
}
