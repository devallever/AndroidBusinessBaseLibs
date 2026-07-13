package app.allever.android.lib.network.core.engine.body

import java.io.OutputStream


/** 空请求体 */
class EmptyNetBody : NetBody() {
    override val contentType: String? = null
    override fun contentLength(): Long = 0L
    override fun writeTo(output: OutputStream) {
        // 无操作
    }
}