package app.allever.android.lib.network.engine.huc

import android.os.Build
import app.allever.android.lib.network.core.engine.Call
import app.allever.android.lib.network.core.engine.CallCallback
import app.allever.android.lib.network.core.engine.HttpRequest
import app.allever.android.lib.network.core.engine.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * HttpURLConnection 异步调用实现
 *
 * 支持同步、异步、协程三种执行方式，以及取消操作。
 *
 * 取消机制：通过 AtomicBoolean 标志位 + CancellationException 实现。
 * 对于长时间运行的 I/O 操作，取消会在下一次检查点生效。
 */
class UrlConnectionCall(
    private val engine: UrlConnectionEngine,
    private val request: HttpRequest
) : Call {

    private val _executed = AtomicBoolean(false)
    private val _canceled = AtomicBoolean(false)

    override val isExecuted: Boolean get() = _executed.get()
    override val isCanceled: Boolean get() = _canceled.get()

    override fun execute(): HttpResponse {
        checkNotCanceled()
        _executed.set(true)
        return engine.execute(request)
    }

    override fun enqueue(callback: CallCallback) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                checkNotCanceled()
                val response = engine.execute(request)
                callback.onSuccess(response)
            } catch (e: Exception) {
                if (_canceled.get()) {
                    callback.onFailure(CancellationException("请求已取消"))
                } else {
                    callback.onFailure(e)
                }
            }
        }
    }

    override fun cancel() {
        _canceled.set(true)
    }

    /**
     * 协程挂起方式执行
     */
    override suspend fun await(): HttpResponse = suspendCancellableCoroutine { continuation ->
        enqueue(object : CallCallback {
            override fun onSuccess(response: HttpResponse) {
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

    private fun checkNotCanceled() {
        if (_canceled.get()) {
            throw CancellationException("请求已被取消")
        }
    }
}
