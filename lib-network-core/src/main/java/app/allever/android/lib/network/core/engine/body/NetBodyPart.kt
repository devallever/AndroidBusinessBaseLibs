package app.allever.android.lib.network.core.engine.body

import java.io.File

/**
 * Multipart 请求体的单个部分
 */
open class NetBodyPart(
    val name: String,
    val filename: String? = null,
    val contentType: String? = null,
    val content: ByteArray? = null,
    val file: File? = null
)