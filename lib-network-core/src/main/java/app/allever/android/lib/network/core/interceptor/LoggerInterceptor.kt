package app.allever.android.lib.network.core.interceptor

import android.util.Log
import app.allever.android.lib.network.core.engine.HttpRequest
import app.allever.android.lib.network.core.engine.HttpResponse

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
    private val tag: String = "Network"
) : Interceptor {

    override fun intercept(chain: InterceptorChain): HttpResponse {
        if (!enabled) return chain.proceed(chain.request!!)

        val request = chain.request!!
        logRequest(request)

        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val elapsedMs = System.currentTimeMillis() - startTime

        logResponse(response, elapsedMs)

        return response
    }

    private fun logRequest(request: HttpRequest) {
        Log.d(tag, "┌────────── Request ──────────→")
        Log.d(tag, "${request.method} ${request.url}")
        Log.d(tag, "Headers: ${request.headers}")
        request.body?.let {
            Log.d(tag, "Body: ${it.contentLength()} bytes")
        } ?: run {
            Log.d(tag, "Body: null")
        }
        Log.d(tag, "Timeouts: connect=${request.connectTimeoutMs}ms read=${request.readTimeoutMs}ms")
    }

    private fun logResponse(response: HttpResponse, elapsedMs: Long) {
        Log.d(tag, "←───────── Response ───────────")
        Log.d(tag, "Code: ${response.code} (${elapsedMs}ms)")
        Log.d(tag, "Headers: ${response.headers.keys}")
        val bodyPreview = response.bodyString?.let {
            if (it.length > 1024) it.substring(0, 1024) + "...(truncated)" else it
        }
        Log.d(tag, "Body: $bodyPreview")
        Log.d(tag, "───────────────────────────────")
    }
}
