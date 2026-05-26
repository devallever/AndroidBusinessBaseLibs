package app.allever.android.lib.ad.core.ad.native_

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import app.allever.android.lib.ad.core.callback.IAdCallback

abstract class BaseNativeAd : INativeAd {

    companion object {
        private const val TAG = "BaseNativeAd"
    }

    private var isLoaded = false
    private var currentContainer: ViewGroup? = null

    override fun isReady(): Boolean = isLoaded

    override fun loadAndShow(
        activity: Activity,
        adId: String,
        container: ViewGroup,
        callback: IAdCallback?
    ) {
        load(activity, adId, object : IAdCallback {
            override fun onAdLoaded() {
                isLoaded = true
                show(container, callback)
                callback?.onAdLoaded()
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                Log.w(TAG, "Native ad load failed: $errorMessage")
                isLoaded = false
                callback?.onAdFail(errorCode, errorMessage)
            }
        })
    }

    override fun load(activity: Activity, adId: String, callback: IAdCallback?) {
        isLoaded = false
        doLoad(activity, adId, object : IAdCallback {
            override fun onAdLoaded() {
                isLoaded = true
                callback?.onAdLoaded()
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                isLoaded = false
                Log.w(TAG, "Native ad load failed: $errorMessage")
                callback?.onAdFail(errorCode, errorMessage)
            }
        })
    }

    override fun show(container: ViewGroup, callback: IAdCallback?) {
        if (!isLoaded) {
            Log.w(TAG, "Native ad not loaded yet")
            return
        }
        currentContainer = container
        doShow(container, callback)
    }

    override fun destroy() {
        isLoaded = false
        currentContainer = null
        doDestroy()
    }

    protected abstract fun doLoad(
        activity: Activity,
        adId: String,
        callback: IAdCallback?
    )

    protected abstract fun doShow(
        container: ViewGroup,
        callback: IAdCallback?
    )

    protected open fun doDestroy() {}

    protected fun getContainer(): ViewGroup? = currentContainer
}
