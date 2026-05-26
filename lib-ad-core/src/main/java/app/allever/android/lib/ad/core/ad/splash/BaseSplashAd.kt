package app.allever.android.lib.ad.core.ad.splash

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import app.allever.android.lib.ad.core.callback.IAdCallback

abstract class BaseSplashAd : ISplashAd {

    companion object {
        private const val TAG = "BaseSplashAd"
    }

    private var isLoaded = false
    private var currentActivity: Activity? = null
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
                show(activity, container, callback)
                callback?.onAdLoaded()
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                Log.w(TAG, "Splash ad load failed: $errorMessage")
                isLoaded = false
                callback?.onAdFail(errorCode, errorMessage)
            }
        })
    }

    override fun load(activity: Activity, adId: String, callback: IAdCallback?) {
        currentActivity = activity
        isLoaded = false
        doLoad(activity, adId, object : IAdCallback {
            override fun onAdLoaded() {
                isLoaded = true
                callback?.onAdLoaded()
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                isLoaded = false
                callback?.onAdFail(errorCode, errorMessage)
            }
        })
    }

    override fun show(activity: Activity, container: ViewGroup, callback: IAdCallback?) {
        if (!isLoaded) {
            Log.w(TAG, "Splash ad not loaded yet")
            return
        }
        currentContainer = container
        doShow(activity, container, callback)
    }

    override fun destroy() {
        isLoaded = false
        currentActivity = null
        currentContainer = null
        doDestroy()
    }

    protected abstract fun doLoad(
        activity: Activity,
        adId: String,
        callback: IAdCallback?
    )

    protected abstract fun doShow(
        activity: Activity,
        container: ViewGroup,
        callback: IAdCallback?
    )

    protected open fun doDestroy() {}

    protected fun getActivity(): Activity? = currentActivity

    protected fun getContainer(): ViewGroup? = currentContainer
}
