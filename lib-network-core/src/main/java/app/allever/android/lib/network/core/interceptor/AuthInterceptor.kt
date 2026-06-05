package app.allever.android.lib.network.core.interceptor

import android.util.Log
import app.allever.android.lib.network.core.engine.NetRequest
import app.allever.android.lib.network.core.engine.NetResponse
import app.allever.android.lib.network.core.response.ResponseAdapter
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection

/**
 * Token 认证拦截器
 *
 * 功能：
 * 1. 自动为每个请求注入 Authorization 头
 * 2. 检测 401 错误（Token 过期）时自动刷新 Token
 * 3. Token 刷新成功后自动重发原请求
 *
 * 使用方式：
 * ```kotlin
 * AuthInterceptor(
 *     tokenProvider = { TokenManager.getToken() },
 *     onTokenExpired = { TokenManager.refreshToken() },
 *     tokenHeaderKey = "Authorization",
 *     tokenFormatter = { "Bearer $it" }
 * )
 * ```
 */
class AuthInterceptor(
    /** 获取当前 Token 的回调 */
    private val tokenProvider: (() -> String?)?,
    /** Token 过期时的刷新回调（返回新的 Token） */
    private val onTokenExpired: (suspend () -> String?)? = null,
    /** Token 在请求头中的 key */
    private val tokenHeaderKey: String = "Authorization",
    /** Token 格式化函数（如 "Bearer $token"） */
    private val tokenFormatter: ((String) -> String) = { "Bearer $it" },
    /** 需要认证的 URL 前缀列表（空则所有请求都带 Token） */
    private val authUrlPrefixes: Set<String> = emptySet(),
    /** 配置中的 successCode，用于识别业务层面的 Token 过期 */
    private val successCode: Int = 0,
    /** 配置中的 responseClass，用于解析响应 */
    private val responseClass: Class<*>? = null
) : NetInterceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        /** 业务层面 Token 过期的通用错误码 */
        const val TOKEN_EXPIRED_CODE = 401
    }

    override fun intercept(chain: NetChain): NetResponse {
        val originalRequest = chain.request!!

        // 判断是否需要携带 Token
        val needAuth = authUrlPrefixes.isEmpty() ||
                authUrlPrefixes.any { originalRequest.url.startsWith(it) }

        // 注入 Token
        val authenticatedRequest = if (needAuth && tokenProvider != null) {
            val token = tokenProvider.invoke()
            if (!token.isNullOrBlank()) {
                injectToken(originalRequest, token)
            } else {
                originalRequest
            }
        } else {
            originalRequest
        }

        // 发起请求
        var response = chain.proceed(authenticatedRequest)

        // 检查是否需要刷新 Token（HTTP 401 或业务码表示过期）
        if (needAuth && shouldRefreshToken(response)) {
            Log.d(TAG, "检测到 Token 过期，尝试刷新...")

            val newToken = tryRefreshToken()
            if (newToken != null) {
                Log.d(TAG, "Token 刷新成功，重发请求...")
                // 用新 Token 重发请求
                val retriedRequest = injectToken(originalRequest, newToken)
                response = chain.proceed(retriedRequest)
            } else {
                Log.w(TAG, "Token 刷新失败")
            }
        }

        return response
    }

    /**
     * 向请求中注入 Token
     */
    private fun injectToken(request: NetRequest, token: String): NetRequest {
        return NetRequest.Builder()
            .url(request.url)
            .method(request.method)
            .headers(request.headers)
            .body(request.body)
            .tag(request.tag)
            .connectTimeout(request.connectTimeoutMs)
            .readTimeout(request.readTimeoutMs)
            .writeTimeout(request.writeTimeoutMs)
            .header(tokenHeaderKey, tokenFormatter(token))
            .build()
    }

    /**
     * 判断是否需要刷新 Token
     */
    private fun shouldRefreshToken(response: NetResponse): Boolean {
        // HTTP 层面的 401
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) return true

        // 业务层面：如果配置了 responseClass，尝试解析业务码
        val clazz = responseClass ?: return false
        val config = app.allever.android.lib.network.core.NetCore.config
            ?: return false
        val converter = config.converter
        val responseBody = response.body ?: return false

        return try {
            val parsedResponse = converter.convert(responseBody, clazz) ?: return false
            val code = ResponseAdapter.extractCode(parsedResponse, config)
            code == TOKEN_EXPIRED_CODE
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 尝试刷新 Token
     */
    private fun tryRefreshToken(): String? {
        val refresher = onTokenExpired ?: return null
        return try {
            runBlocking { refresher() }
        } catch (e: Exception) {
            Log.e(TAG, "Token 刷新异常", e)
            null
        }
    }
}
