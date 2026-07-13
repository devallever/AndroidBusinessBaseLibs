package app.allever.android.lib.network.core.engine.body

import java.io.File
import java.io.OutputStream

/** 文件请求体 */
class FileNetBody(
    private val file: File,
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
            "mp4", "m4v", "mov", "avi", "wmv", "flv", "webm" -> "video/mp4"
            "mp3", "wav", "ogg", "flac", "aac", "m4a" -> "audio/mpeg"
            "txt" -> "text/plain"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            else -> "application/octet-stream"
        }

    override fun contentLength(): Long = file.length()

    override fun writeTo(output: OutputStream) {
        file.inputStream().use { input ->
            input.copyTo(output)
        }
    }
}
