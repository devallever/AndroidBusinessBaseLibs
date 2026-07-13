package app.allever.android.lib.network.core.engine.body

import java.io.OutputStream

/** 字符串请求体 */
class StringNetBody(
    private val content: String,
    override val contentType: String?
) : NetBody() {
    override fun contentLength(): Long = content.toByteArray(Charsets.UTF_8).size.toLong()

    override fun writeTo(output: OutputStream) {
        output.write(content.toByteArray(Charsets.UTF_8))
    }
}