package app.allever.android.sample.im.http

/**
 * 统一响应结构
 */
data class ServerResponse<T>(
    val code: Int = -1,
    val msg: String = "",
    val data: T? = null
)
