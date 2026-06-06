package app.allever.android.lib.network.core.interceptor

import app.allever.android.lib.network.core.engine.NetRequest
import app.allever.android.lib.network.core.engine.NetResponse
import app.allever.android.lib.network.core.util.NetLogger

/**
 * 日志拦截器
 *
 * 打印请求和响应的关键信息，便于调试。
 * 建议在 Debug 模式下启用，Release 模式下禁用。
 *
 * 日志格式示例：
 * ┌────────── Request ──────────→
 * GET https://api.example.com/user/1
 * Headers: {Authorization=Bearer xxx, ...}
 * Body: null
 * ←───────── Response ───────────
 * Code: 200 (312ms)
 * Body: {"code":0,"msg":"ok","data":{...}}
 */
class LoggerInterceptor(
    private val enabled: Boolean = true,
    private val tag: String = LoggerInterceptor::class.java.simpleName
) : NetInterceptor {

    override fun intercept(chain: NetChain): NetResponse {
        if (!enabled) return chain.proceed(chain.request!!)

        val request = chain.request!!
        logRequest(request)

        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val elapsedMs = System.currentTimeMillis() - startTime

        logResponse(response, elapsedMs)

        return response
    }

    private fun logRequest(request: NetRequest) {
        NetLogger.log(tag, "┌────────── Request ──────────→")
        NetLogger.log(tag, "${request.method} ${request.url}")
        NetLogger.log(tag, "Headers: ${request.headers}")
        request.body?.let {
            NetLogger.log(tag, "Body: ${it.contentLength()} bytes")
        } ?: run {
            NetLogger.log(tag, "Body: null")
        }
        NetLogger.log(tag, "Timeouts: connect=${request.connectTimeoutMs}ms read=${request.readTimeoutMs}ms")
    }

    private fun logResponse(response: NetResponse, elapsedMs: Long) {
        NetLogger.log(tag, "←───────── Response ───────────")
        NetLogger.log(tag, "Code: ${response.code} (${elapsedMs}ms)")
        NetLogger.log(tag, "Headers: ${response.headers.keys}")
        val bodyPreview = response.bodyString?.let {
            if (it.length > 1024) it.substring(0, 1024) + "...(truncated)" else it
        }
        NetLogger.log(tag, "Body: $bodyPreview")
        NetLogger.log(tag, "───────────────────────────────")
    }
}
