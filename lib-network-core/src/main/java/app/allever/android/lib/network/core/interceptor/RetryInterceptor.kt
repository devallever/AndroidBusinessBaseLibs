package app.allever.android.lib.network.core.interceptor

import app.allever.android.lib.network.core.engine.NetResponse
import app.allever.android.lib.network.core.exception.NetworkException
import app.allever.android.lib.network.core.util.NetLogger
import kotlin.math.min

/**
 * 重试拦截器
 *
 * 当请求失败时自动重试，支持配置：
 * - 最大重试次数
 * - 重试间隔（线性退避 / 固定间隔）
 * - 可重试的错误类型（仅对网络类错误重试，不对业务错误重试）
 *
 * 注意：此拦截器应在拦截器链中靠后位置（接近引擎），确保重试时重新走完整流程。
 */
class RetryInterceptor(
    /** 最大重试次数（不含首次请求） */
    private val maxRetries: Int = 2,
    /** 重试基础间隔（毫秒） */
    private val retryIntervalMs: Long = 500L,
    /** 是否启用线性退避（每次重试间隔翻倍） */
    private val linearBackoff: Boolean = true,
    /** 自定义判断：该异常是否可重试 */
    private val shouldRetry: ((Throwable) -> Boolean)? = null
) : NetInterceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
        private const val MAX_RETRY_INTERVAL_MS = 5000L
    }

    override fun intercept(chain: NetChain): NetResponse {
        var lastException: Exception? = null

        repeat(maxRetries + 1) { attempt ->
            try {
                return chain.proceed(chain.request!!)
            } catch (e: Exception) {
                lastException = e

                // 判断是否应该重试
                if (attempt >= maxRetries || !isRetryable(e)) {
                    NetLogger.logE(TAG, "重试结束（不可重试或已达上限）: attempt=${attempt + 1}/${maxRetries + 1}, error=${e.message}")
                    throw e
                }

                // 计算等待时间并延迟
                val delay = if (linearBackoff) {
                    min(retryIntervalMs * (1 shl attempt), MAX_RETRY_INTERVAL_MS)
                } else {
                    retryIntervalMs
                }

                NetLogger.log(TAG, "第 ${attempt + 1} 次请求失败，${delay}ms 后重试 (${e.message})")

                if (delay > 0) {
                    Thread.sleep(delay)
                }
            }
        }

        throw lastException ?: IllegalStateException("重试失败")
    }

    /**
     * 判断异常是否可重试
     * - 网络超时、连接失败、SSL 错误 → 可重试
     * - 业务错误、解析错误、取消 → 不重试
     */
    private fun isRetryable(e: Throwable): Boolean {
        // 先检查自定义规则
        shouldRetry?.let { return it(e) }

        // 默认规则：只对网络层错误重试
        val networkException = when (e) {
            is NetworkException -> e
            else -> return isNetworkError(e)
        }

        return when (networkException) {
            is NetworkException.TimeoutError -> true
            is NetworkException.ConnectError -> true
            is NetworkException.NoNetworkError -> false   // 无网络不重试
            is NetworkException.SslError -> true
            is NetworkException.HttpError -> networkException.code >= 500  // 仅服务端错误重试
            is NetworkException.BizError -> false      // 业务错误不重试
            is NetworkException.ParseError -> false     // 解析错误不重试
            is NetworkException.EmptyBodyError -> false // 空响应体不重试
            is NetworkException.CanceledError -> false  // 取消不重试
            is NetworkException.UnknownError -> true
        }
    }

    /**
     * 判断原始 Throwable 是否为网络层面的可重试错误
     */
    private fun isNetworkError(e: Throwable): Boolean {
        return e is java.net.SocketTimeoutException ||
                e is java.net.ConnectException ||
                e is javax.net.ssl.SSLException ||
                e is java.io.IOException
    }
}
