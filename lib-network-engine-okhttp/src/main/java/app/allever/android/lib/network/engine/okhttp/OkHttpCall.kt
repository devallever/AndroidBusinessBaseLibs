package app.allever.android.lib.network.engine.okhttp

import app.allever.android.lib.network.core.engine.NetCallback
import app.allever.android.lib.network.core.engine.NetCall
import app.allever.android.lib.network.core.engine.NetRequest
import app.allever.android.lib.network.core.engine.NetResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OkHttp 异步调用实现
 *
 * 包装 okhttp3.Call，提供同步、异步、协程三种执行方式。
 * 取消操作直接委托给 OkHttp 原生的 cancel()，更可靠。
 */
class OkHttpCall(
    private val okCall: okhttp3.Call,
    private val request: NetRequest
) : NetCall {

    override val isExecuted: Boolean get() = okCall.isExecuted()
    override val isCanceled: Boolean get() = okCall.isCanceled()

    override fun execute(): NetResponse {
        checkNotCanceled()
        val response = okCall.execute()
        return convertResponse(response)
    }

    override fun enqueue(callback: NetCallback) {
        okCall.enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                callback.onSuccess(convertResponse(response))
            }

            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                if (call.isCanceled()) {
                    callback.onFailure(CancellationException("请求已取消"))
                } else {
                    callback.onFailure(e)
                }
            }
        })
    }

    override fun cancel() {
        okCall.cancel()
    }

    /**
     * 协程挂起方式执行
     * 利用 OkHttp 原生异步能力 + 协程挂起
     */
    override suspend fun await(): NetResponse = suspendCancellableCoroutine { continuation ->
        enqueue(object : NetCallback {
            override fun onSuccess(response: NetResponse) {
                continuation.resume(response)
            }

            override fun onFailure(exception: Exception) {
                continuation.resumeWithException(exception)
            }
        })

        continuation.invokeOnCancellation {
            cancel()
        }
    }

    // ==================== 内部工具 ====================

    private fun checkNotCanceled() {
        if (isCanceled) {
            throw CancellationException("请求已被取消")
        }
    }

    /**
     * 将 okhttp3.Response 转换为 NetResponse
     */
    private fun convertResponse(okResponse: okhttp3.Response): NetResponse {
        val body = okResponse.body?.bytes()
        val headers = mutableMapOf<String, String>()

        okResponse.headers.names().forEach { name ->
            okResponse.headers.values(name).firstOrNull()?.let { value ->
                headers[name] = value
            }
        }

        return NetResponse(
            code = okResponse.code,
            message = okResponse.message,
            headers = headers,
            body = body,
            request = request,
            contentLength = okResponse.body?.contentLength() ?: -1L,
            elapsedMs = 0L // 异步场景耗时由外部统计
        )
    }
}
