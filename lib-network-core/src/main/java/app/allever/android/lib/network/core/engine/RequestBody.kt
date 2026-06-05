package app.allever.android.lib.network.core.engine

/**
 * 抽象请求体
 *
 * 各引擎需将此抽象转换为自身实现：
 * - OkHttp → okhttp3.RequestBody
 * - HttpURLConnection → OutputStream 写入
 */
abstract class RequestBody {

    /** Content-Type (如 "application/json; charset=utf-8") */
    abstract val contentType: String?

    /** 请求体内容长度，未知返回 -1 */
    abstract fun contentLength(): Long

    /** 将请求体写入输出流 */
    abstract fun writeTo(output: java.io.OutputStream)

    // ==================== 工厂方法 ====================

    /** 创建 JSON 请求体 */
    companion object {
        /**
         * 从字符串创建请求体
         * @param content 字符串内容
         * @param contentType MIME 类型，默认 application/json
         */
        fun create(content: String, contentType: String = "application/json; charset=utf-8"): RequestBody {
            return StringRequestBody(content, contentType)
        }

        /**
         * 从字节数组创建请求体
         */
        fun create(bytes: ByteArray, contentType: String = "application/octet-stream"): RequestBody {
            return BytesRequestBody(bytes, contentType)
        }

        /**
         * 从 File 创建请求体（用于上传文件）
         * @param file 文件
         * @param contentType MIME 类型
         */
        fun create(file: java.io.File, contentType: String? = null): RequestBody {
            return FileRequestBody(file, contentType)
        }

        /**
         * 空请求体
         */
        fun empty(): RequestBody = EmptyRequestBody()
    }
}

/** 字符串请求体 */
private class StringRequestBody(
    private val content: String,
    override val contentType: String?
) : RequestBody() {
    override fun contentLength(): Long = content.toByteArray(Charsets.UTF_8).size.toLong()

    override fun writeTo(output: java.io.OutputStream) {
        output.write(content.toByteArray(Charsets.UTF_8))
    }
}

/** 字节数组请求体 */
private class BytesRequestBody(
    private val bytes: ByteArray,
    override val contentType: String?
) : RequestBody() {
    override fun contentLength(): Long = bytes.size.toLong()

    override fun writeTo(output: java.io.OutputStream) {
        output.write(bytes)
    }
}

/** 文件请求体 */
private class FileRequestBody(
    private val file: java.io.File,
    private val _contentType: String?
) : RequestBody() {
    override val contentType: String? get() = _contentType
        ?: when (file.extension.lowercase()) {
            "json" -> "application/json"
            "xml" -> "application/xml"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }

    override fun contentLength(): Long = file.length()

    override fun writeTo(output: java.io.OutputStream) {
        file.inputStream().use { input ->
            input.copyTo(output)
        }
    }
}

/** 空请求体 */
private class EmptyRequestBody : RequestBody() {
    override val contentType: String? = null
    override fun contentLength(): Long = 0L
    override fun writeTo(output: java.io.OutputStream) {
        // 无操作
    }
}
