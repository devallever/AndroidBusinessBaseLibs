package app.allever.android.lib.network.core.engine

/**
 * 抽象请求体
 *
 * 各引擎需将此抽象转换为自身实现：
 * - OkHttp → okhttp3.RequestBody
 * - HttpURLConnection → OutputStream 写入
 */
abstract class NetBody {

    /** Content-Type (如 "application/json; charset=utf-8") */
    abstract val contentType: String?

    /** 请求体内容长度，未知返回 -1 */
    abstract fun contentLength(): Long

    /** 将请求体写入输出流 */
    abstract fun writeTo(output: java.io.OutputStream)

    // ==================== 工厂方法 ====================

    companion object {
        /**
         * 从字符串创建请求体
         * @param content 字符串内容
         * @param contentType MIME 类型，默认 application/json
         */
        fun create(content: String, contentType: String = "application/json; charset=utf-8"): NetBody {
            return StringNetBody(content, contentType)
        }

        /**
         * 从字节数组创建请求体
         */
        fun create(bytes: ByteArray, contentType: String = "application/octet-stream"): NetBody {
            return BytesNetBody(bytes, contentType)
        }

        /**
         * 从 File 创建请求体（用于上传文件）
         * @param file 文件
         * @param contentType MIME 类型
         */
        fun create(file: java.io.File, contentType: String? = null): NetBody {
            return FileNetBody(file, contentType)
        }

        /**
         * 空请求体
         */
        fun empty(): NetBody = EmptyNetBody()
    }
}

/** 字符串请求体 */
private class StringNetBody(
    private val content: String,
    override val contentType: String?
) : NetBody() {
    override fun contentLength(): Long = content.toByteArray(Charsets.UTF_8).size.toLong()

    override fun writeTo(output: java.io.OutputStream) {
        output.write(content.toByteArray(Charsets.UTF_8))
    }
}

/** 字节数组请求体 */
private class BytesNetBody(
    private val bytes: ByteArray,
    override val contentType: String?
) : NetBody() {
    override fun contentLength(): Long = bytes.size.toLong()

    override fun writeTo(output: java.io.OutputStream) {
        output.write(bytes)
    }
}

/** 文件请求体 */
private class FileNetBody(
    private val file: java.io.File,
    private val _contentType: String?
) : NetBody() {
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
private class EmptyNetBody : NetBody() {
    override val contentType: String? = null
    override fun contentLength(): Long = 0L
    override fun writeTo(output: java.io.OutputStream) {
        // 无操作
    }
}

/** 旧名兼容别名（后续版本移除） */
@Deprecated("请使用 NetBody 替代", ReplaceWith("NetBody"))
typealias RequestBody = NetBody
