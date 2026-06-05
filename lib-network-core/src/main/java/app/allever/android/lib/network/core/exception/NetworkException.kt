package app.allever.android.lib.network.core.exception

/**
 * 网络异常基类 - 密封类，覆盖所有网络相关错误场景
 */
sealed class NetworkException(
    open val code: Int = -1,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /** 用户可读的错误展示文本 */
    abstract val displayMessage: String

    // ==================== HTTP 层错误 ====================

    /** HTTP 状态码错误 (非 2xx) */
    data class HttpError(
        override val code: Int,           // HTTP status code
        val httpMessage: String = "",
        override val cause: Throwable? = null
    ) : NetworkException(code, "HTTP $code: $httpMessage", cause) {
        override val displayMessage: String
            get() = when (code) {
                400 -> "请求参数错误"
                401 -> "未授权，请重新登录"
                403 -> "没有访问权限"
                404 -> "请求的资源不存在"
                in 400..499 -> "客户端错误 ($code)"
                500 -> "服务器内部错误"
                502 -> "网关错误"
                503 -> "服务不可用"
                in 500..599 -> "服务器错误 ($code)"
                else -> "网络请求失败 ($code)"
            }
    }

    /** 业务逻辑错误（HTTP 成功但业务码非 successCode） */
    data class BizError(
        override val code: Int,           // 业务错误码
        val bizMsg: String = "",          // 业务错误消息
        override val cause: Throwable? = null
    ) : NetworkException(code, bizMsg.ifEmpty { "业务异常($code)" }, cause) {
        override val displayMessage: String get() = bizMsg.ifEmpty { message }
    }

    // ==================== 网络层错误 ====================

    /** 无网络连接 */
    data class NoNetworkError(
        override val cause: Throwable? = null
    ) : NetworkException(-1, "无网络连接", cause) {
        override val displayMessage: String get() = "当前无网络连接，请检查网络设置"
    }

    /** 连接超时 */
    data class TimeoutError(
        override val cause: Throwable? = null
    ) : NetworkException(-1, "连接超时", cause) {
        override val displayMessage: String get() = "网络连接超时，请稍后重试"
    }

    /** 连接失败（DNS、拒绝连接等） */
    data class ConnectError(
        override val cause: Throwable? = null
    ) : NetworkException(-1, "连接失败", cause) {
        override val displayMessage: String get() = "无法连接到服务器，请检查网络"
    }

    /** SSL/TLS 证书错误 */
    data class SslError(
        override val cause: Throwable? = null
    ) : NetworkException(-1, "SSL证书验证失败", cause) {
        override val displayMessage: String get() = "安全证书验证失败"
    }

    // ==================== 数据层错误 ====================

    /** JSON 解析错误 */
    data class ParseError(
        val detail: String = "",
        override val cause: Throwable? = null
    ) : NetworkException(-1, "数据解析失败: $detail", cause) {
        override val displayMessage: String get() = "数据格式异常"
    }

    /** 响应体为空 */
    data class EmptyBodyError(
        override val cause: Throwable? = null
    ) : NetworkException(-1, "响应体为空", cause) {
        override val displayMessage: String get() = "服务器返回数据为空"
    }

    // ==================== 其他 ====================

    /** 请求被取消 */
    data class CanceledError(
        override val cause: Throwable? = null
    ) : NetworkException(-1, "请求已取消", cause) {
        override val displayMessage: String get() = ""
    }

    /** 未知错误 */
    data class UnknownError(
        override val cause: Throwable? = null
    ) : NetworkException(-1, "未知网络错误", cause) {
        override val displayMessage: String get() = "网络异常，请稍后重试"
    }
}
