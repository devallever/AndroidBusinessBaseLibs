package app.allever.android.lib.network.core.engine

import app.allever.android.lib.network.core.engine.body.NetBody

/**
 * 引擎无关的 HTTP 请求模型
 *
 * 使用示例：
 * ```kotlin
 * val request = NetRequest.Builder()
 *     .url("https://api.example.com/user")
 *     .method(HttpMethod.GET)
 *     .header("Authorization", "Bearer token")
 *     .build()
 * ```
 */
class NetRequest private constructor(
    val url: String,
    val method: HttpMethod,
    val headers: Map<String, String>,
    val params: Map<String, String>,
    val body: NetBody?,
    val tag: Any?,
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val writeTimeoutMs: Long
) {
    /** 获取指定 header 值（不区分大小写） */
    fun header(name: String): String? {
        return headers.entries.find { it.key.equals(name, ignoreCase = true) }?.value
    }

    /** 是否包含该 header */
    fun hasHeader(name: String): Boolean = header(name) != null

    /**
     * Builder - DSL 风格构建请求
     */
    class Builder {
        var url: String = ""
        var method: HttpMethod = HttpMethod.GET
        private val headers = mutableMapOf<String, String>()
        private val params = mutableMapOf<String, String>()
        var body: NetBody? = null
        var tag: Any? = null
        var connectTimeoutMs: Long = 10_000L
        var readTimeoutMs: Long = 15_000L
        var writeTimeoutMs: Long = 30_000L

        fun url(url: String) = apply { this.url = url }
        fun method(method: HttpMethod) = apply { this.method = method }

        /** 添加请求头（不区分大小写，后者覆盖前者） */
        fun header(name: String, value: String) = apply {
            // 移除同名 header（不区分大小写）
            headers.keys.removeAll { it.equals(name, ignoreCase = true) }
            headers[name] = value
        }

        /** 批量添加请求头 */
        fun headers(map: Map<String, String>) = apply {
            map.forEach { (k, v) -> header(k, v) }
        }

        /** 添加 query 参数 */
        fun param(key: String, value: String) = apply { params[key] = value }

        /** 批量添加 query 参数 */
        fun params(map: Map<String, String>) = apply { params.putAll(map) }

        fun body(body: NetBody?) = apply { this.body = body }
        fun tag(tag: Any?) = apply { this.tag = tag }
        fun connectTimeout(ms: Long) = apply { connectTimeoutMs = ms }
        fun readTimeout(ms: Long) = apply { readTimeoutMs = ms }
        fun writeTimeout(ms: Long) = apply { writeTimeoutMs = ms }

        /** 构建完整 URL（拼接 query 参数） */
        fun buildUrl(): String {
            if (params.isEmpty()) return url
            val separator = if (url.contains("?")) "&" else "?"
            val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            return "$url$separator$queryString"
        }

        fun build(): NetRequest {
            require(url.isNotBlank()) { "NetRequest url 不能为空" }
            return NetRequest(
                url = buildUrl(),
                method = method,
                headers = headers.toMap(),
                params = params.toMap(),
                body = body,
                tag = tag,
                connectTimeoutMs = connectTimeoutMs,
                readTimeoutMs = readTimeoutMs,
                writeTimeoutMs = writeTimeoutMs
            )
        }
    }

    /** DSL 创建请求的便捷方法 */
    companion object {
        inline fun request(block: Builder.() -> Unit): NetRequest =
            Builder().apply(block).build()
    }
}

/** 旧名兼容别名（后续版本移除） */
@Deprecated("请使用 NetRequest 替代", ReplaceWith("NetRequest"))
typealias HttpRequest = NetRequest
