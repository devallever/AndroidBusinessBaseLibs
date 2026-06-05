package app.allever.android.lib.network.engine.huc

import app.allever.android.lib.network.core.engine.*
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * HttpURLConnection 引擎实现
 *
 * 基于 JDK 内置的 HttpURLConnection 实现 HttpEngine 接口。
 * 无需任何第三方依赖，适合对包体积敏感的场景。
 *
 * 自动注册到 EngineRegistry，名称为 "url_connection"
 *
 * 使用方式：
 * ```kotlin
 * Network.init {
 *     baseUrl("https://api.example.com")
 *     engine("url_connection") {
 *         connectTimeout(10_000)
 *         readTimeout(15_000)
 *         // UrlConnectionConfig 专属配置
 *         (this as? UrlConnectionConfig)?.apply {
 *             followRedirects(true)
 *         }
 *     }
 * }
 * ```
 */
class UrlConnectionEngine(private val config: UrlConnectionConfig) : HttpEngine {

    override val engineName: String = ENGINE_NAME

    override fun execute(request: NetRequest): NetResponse {
        val startTime = System.currentTimeMillis()
        var connection: HttpURLConnection? = null

        try {
            connection = createConnection(request)

            // 写入请求体
            if (request.body != null && request.method in listOf(
                    HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE
                )
            ) {
                writeBody(connection, request.body!!)
            }

            // 读取响应
            val code = connection.responseCode
            val message = connection.responseMessage ?: ""
            val headers = readHeaders(connection)
            val body = readBody(connection)
            val contentLength = connection.contentLength.toLong()

            return NetResponse(
                code = code,
                message = message,
                headers = headers,
                body = body,
                request = request,
                contentLength = contentLength,
                elapsedMs = System.currentTimeMillis() - startTime
            )

        } catch (e: Exception) {
            throw mapException(e)
        } finally {
            connection?.disconnect()
        }
    }

    override fun newCall(request: NetRequest): NetCall {
        return UrlConnectionCall(this, request)
    }

    override fun shutdown() {
        // HttpURLConnection 无需显式释放资源
        // 如果使用了 CookieManager 等可以在这里清理
    }

    // ==================== 内部实现 ====================

    /**
     * 创建并配置 HttpURLConnection
     */
    private fun createConnection(request: NetRequest): HttpURLConnection {
        val url = URL(request.url)
        val connection = url.openConnection() as HttpURLConnection

        // 基础配置
        connection.connectTimeout = config.connectTimeoutMs.toInt()
        connection.readTimeout = config.readTimeoutMs.toInt()
        connection.instanceFollowRedirects = config.followRedirects
        connection.useCaches = config.useCaches

        // 请求方法
        connection.requestMethod = when (request.method) {
            HttpMethod.GET -> "GET"
            HttpMethod.POST -> "POST"
            HttpMethod.PUT -> "PUT"
            HttpMethod.DELETE -> "DELETE"
            HttpMethod.PATCH -> "PATCH"
            HttpMethod.HEAD -> "HEAD"
            HttpMethod.OPTIONS -> "OPTIONS"
        }

        // 输入输出设置
        connection.doInput = true
        if (request.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) {
            connection.doOutput = true
        }

        // Keep-Alive
        if (config.keepAlive) {
            connection.setRequestProperty("Connection", "keep-alive")
        } else {
            connection.setRequestProperty("Connection", "close")
        }

        // 请求头
        for ((key, value) in request.headers) {
            connection.setRequestProperty(key, value)
        }

        return connection
    }

    /**
     * 写入请求体
     */
    private fun writeBody(connection: HttpURLConnection, body: NetBody) {
        body.contentType?.let {
            connection.setRequestProperty("Content-Type", it)
        }

        val contentLength = body.contentLength()
        if (contentLength >= 0) {
            connection.setFixedLengthStreamingMode(contentLength.toInt())
        } else {
            connection.setChunkedStreamingMode(8192)
        }

        connection.outputStream.use { output ->
            body.writeTo(output)
            output.flush()
        }
    }

    /**
     * 读取响应头
     */
    private fun readHeaders(connection: HttpURLConnection): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        for (i in 0 until connection.headerFields.size) {
            val key = connection.getHeaderFieldKey(i) ?: ""
            val value = connection.getHeaderField(i) ?: ""
            if (key.isNotEmpty()) {
                headers[key] = value
            }
        }
        return headers
    }

    /**
     * 读取响应体
     */
    private fun readBody(connection: HttpURLConnection): ByteArray? {
        return try {
            val inputStream = if (connection.responseCode < 400) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            if (inputStream == null) null
            else ByteArrayOutputStream().use { output ->
                inputStream.copyTo(output)
                output.toByteArray()
            }
        } catch (_: Exception) {
            null
        }
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
        val ENGINE_NAME = "url_connection"

        /**
         * 模块被引用时自动注册到 EngineRegistry
         */
        init {
            EngineRegistry.register(ENGINE_NAME) { rawConfig ->
                when (rawConfig) {
                    is UrlConnectionConfig -> rawConfig
                    else -> UrlConnectionConfig().also {
                        it.connectTimeoutMs = rawConfig.connectTimeoutMs
                        it.readTimeoutMs = rawConfig.readTimeoutMs
                        it.writeTimeoutMs = rawConfig.writeTimeoutMs
                    }
                }.let { UrlConnectionEngine(it) }
            }

        }
    }
}
