package app.allever.android.lib.network.core.engine.body

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Multipart 请求体（支持多文件上传）
 */
class MultipartNetBody(
    private val parts: List<NetBodyPart>,
    private val boundary: String = generateBoundary()
) : NetBody() {

    override val contentType: String? = "multipart/form-data; boundary=$boundary"

    override fun contentLength(): Long {
        return try {
            val outputStream = ByteArrayOutputStream()
            writeTo(outputStream)
            outputStream.size().toLong()
        } catch (e: Exception) {
            -1L
        }
    }

    override fun writeTo(output: OutputStream) {
        parts.forEach { part ->
            output.write("--$boundary\r\n".toByteArray(Charsets.UTF_8))

            if (part.filename != null) {
                val contentType = part.contentType ?: "application/octet-stream"
                output.write("Content-Disposition: form-data; name=\"${part.name}\"; filename=\"${part.filename}\"\r\n".toByteArray(Charsets.UTF_8))
                output.write("Content-Type: $contentType\r\n".toByteArray(Charsets.UTF_8))
            } else {
                output.write("Content-Disposition: form-data; name=\"${part.name}\"\r\n".toByteArray(Charsets.UTF_8))
            }

            output.write("\r\n".toByteArray(Charsets.UTF_8))

            if (part.file != null) {
                part.file.inputStream().use { input ->
                    input.copyTo(output)
                }
            } else if (part.content != null) {
                output.write(part.content)
            }

            output.write("\r\n".toByteArray(Charsets.UTF_8))
        }

        output.write("--$boundary--\r\n".toByteArray(Charsets.UTF_8))
    }

    companion object {
        private fun generateBoundary(): String {
            return "NetBodyBoundary_${System.currentTimeMillis()}_${UUID.randomUUID()}"
        }
    }
}