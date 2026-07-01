package app.flash.tunnel.vpn.lib.common.util

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner


/**
 *
 */
class PollingTask(
    lifecycleOwner: LifecycleOwner? = null,
    val interval: Long = 2000,
    private val mTaskName: String = "Default",
    private val mMaxRetry: Int = -1,
    private val mCondition: (task: PollingTask) -> Boolean,
    private val mExecute: () -> Unit = {},
    private val mOnFail: () -> Unit = {}
) : DefaultLifecycleObserver {
    private var mIsCancel = false
    private var mRetryCount = 0
    private val mHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val task = Runnable {
        start()
    }

    private var executing = false

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    fun start() {
        mIsCancel = false
        if (!mCondition(this) && !mIsCancel) {
            executing = true
            mRetryCount++
            if (mMaxRetry > 0) {
                if (mRetryCount > mMaxRetry) {
                    mOnFail.invoke()
                    executing = false
                    return
                }
            }

            log("${mTaskName}: delay check condition retry $mRetryCount")
            mHandler.postDelayed(task, interval)
            return
        }
        mExecute()
        executing = false
    }

    fun cancel() {
        mIsCancel = true
        mHandler.removeCallbacks(task)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cancel()
    }


    fun executing(): Boolean {
        return executing
    }
}