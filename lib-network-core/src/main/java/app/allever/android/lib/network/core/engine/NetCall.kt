package app.allever.android.lib.network.core.engine

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 抽象调用 - 支持同步、异步、协程三种执行方式
 *
 * 各引擎实现此接口以提供统一的取消能力
 */
interface NetCall {

    /** 同步执行（阻塞当前线程） */
    fun execute(): NetResponse

    /** 异步执行（回调方式） */
    fun enqueue(callback: NetCallback)

    /** 取消请求 */
    fun cancel()

    /** 是否已执行 */
    val isExecuted: Boolean

    /** 是否已取消 */
    val isCanceled: Boolean

    /** 协程挂起方式执行（自动取消） */
    suspend fun await(): NetResponse = suspendCancellableCoroutine { continuation ->
        val callback = object : NetCallback {
            override fun onSuccess(response: NetResponse) {
                continuation.resume(response)
            }

            override fun onFailure(exception: Exception) {
                continuation.resumeWithException(exception)
            }
        }

        enqueue(callback)

        continuation.invokeOnCancellation {
            cancel()
        }
    }
}

/**
 * 异步回调接口
 */
interface NetCallback {
    fun onSuccess(response: NetResponse)
    fun onFailure(exception: Exception)
}

/** 旧名兼容别名（后续版本移除） */
@Deprecated("请使用 NetCall 替代", ReplaceWith("NetCall"))
typealias Call = NetCall

@Deprecated("请使用 NetCallback 替代", ReplaceWith("NetCallback"))
typealias CallCallback = NetCallback
