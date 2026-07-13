package app.allever.android.lib.network.core.engine.body

import java.io.OutputStream


/** 字节数组请求体 */
class BytesNetBody(
    private val bytes: ByteArray,
    override val contentType: String?
) : NetBody() {
    override fun contentLength(): Long = bytes.size.toLong()

    override fun writeTo(output: OutputStream) {
        output.write(bytes)
    }
}