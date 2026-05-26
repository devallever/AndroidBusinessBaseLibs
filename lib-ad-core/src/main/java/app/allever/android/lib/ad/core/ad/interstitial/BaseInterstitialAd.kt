package app.allever.android.lib.ad.core.ad.interstitial

import android.app.Activity
import android.util.Log
import app.allever.android.lib.ad.core.callback.IAdCallback

abstract class BaseInterstitialAd : IInterstitialAd {

    companion object {
        private const val TAG = "BaseInterstitialAd"
    }

    private var isLoaded = false
    private var currentActivity: Activity? = null

    override fun isReady(): Boolean = isLoaded

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
                Log.w(TAG, "Interstitial ad load failed: $errorMessage")
                callback?.onAdFail(errorCode, errorMessage)
            }
        })
    }

    override fun show(activity: Activity, callback: IAdCallback?) {
        if (!isLoaded) {
            Log.w(TAG, "Interstitial ad not loaded yet")
            return
        }
        doShow(activity, callback)
    }

    override fun destroy() {
        isLoaded = false
        currentActivity = null
        doDestroy()
    }

    protected abstract fun doLoad(
        activity: Activity,
        adId: String,
        callback: IAdCallback?
    )

    protected abstract fun doShow(activity: Activity, callback: IAdCallback?)

    protected open fun doDestroy() {}

    protected fun getActivity(): Activity? = currentActivity
}
