package app.allever.android.lib.network.core.engine

/**
 * 引擎无关的 HTTP 响应模型
 *
 * 对应 HTTP 协议层面的 status line + headers + body
 *
 * @param code HTTP 状态码 (200, 404, 500 ...)
 * @param message HTTP 状态消息 ("OK", "Not Found" ...)
 * @param headers 响应头（不区分大小写的 key-value）
 * @param body 响应体原始字节（可能为空）
 * @param request 关联的原始请求
 * @param contentLength Content-Length（用于下载进度计算）
 * @param elapsedMs 请求耗时（毫秒）
 */
data class HttpResponse(
    val code: Int,
    val message: String = "",
    val headers: Map<String, String> = emptyMap(),
    val body: ByteArray? = null,
    val request: HttpRequest? = null,
    val contentLength: Long = -1L,
    val elapsedMs: Long = 0L
) {
    /** HTTP 层是否成功 (200~299) */
    val isSuccessful: Boolean get() = code in 200..299

    /** 是否为重定向 (300~399) */
    val isRedirect: Boolean get() = code in 300..399

    /** 是否为客户端错误 (400~499) */
    val isClientError: Boolean get() = code in 400..499

    /** 是否为服务端错误 (500~599) */
    val isServerError: Boolean get() = code in 500..599

    /** body 转字符串 */
    val bodyString: String? get() = body?.toString(Charsets.UTF_8)

    /** 获取指定 header（不区分大小写） */
    fun header(name: String): String? {
        return headers.entries.find { it.key.equals(name, ignoreCase = true) }?.value
    }

    /** 是否包含该 header */
    fun hasHeader(name: String): Boolean = header(name) != null

    /**
     * 使用 ResponseConverter 将 body 反序列化为目标类型
     */
    fun <T> toObject(converter: ResponseConverter, clazz: Class<T>): T? {
        return body?.let { converter.convert(it, clazz) }
    }
}
