package app.allever.android.lib.ad.core.base

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType

abstract class BaseAdProvider : IAdProvider {

    companion object {
        private const val TAG = "BaseAdProvider"
    }

    protected var isInitialized = false

    protected val adCache = mutableMapOf<AdType, Any>()

    override fun isInit(): Boolean = isInitialized

    override fun isReady(adType: AdType): Boolean {
        return adCache.containsKey(adType)
    }

    override fun loadAd(
        activity: Activity,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        if (!isInitialized) {
            Log.w(TAG, "Ad provider not initialized, please call init() first")
            callback?.onAdFail(-1, "Ad provider not initialized")
            return
        }
        doLoadAd(activity, adType, adId, callback)
    }

    override fun showAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        if (!isReady(adType)) {
            Log.w(TAG, "${adType.name} not ready, please load first")
            return
        }
        doShowAd(activity, adType, container, callback)
    }

    override fun destroy() {
        adCache.clear()
        onDestroy()
    }

    protected abstract fun doLoadAd(
        activity: Activity,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    )

    protected abstract fun doShowAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup?,
        callback: IAdCallback?
    )

    protected open fun onDestroy() {}

    protected fun cacheAd(adType: AdType, ad: Any) {
        adCache[adType] = ad
    }

    protected fun getCachedAd(adType: AdType): Any? {
        return adCache[adType]
    }

    protected fun removeCachedAd(adType: AdType) {
        adCache.remove(adType)
    }
}
