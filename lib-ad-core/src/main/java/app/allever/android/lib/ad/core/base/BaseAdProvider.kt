package app.allever.android.lib.ad.core.base

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.app.App

abstract class BaseAdProvider : IAdProvider {

    companion object {
        private const val TAG = "BaseAdProvider"
    }

    protected var isInitialized = false

    protected val adCache = mutableMapOf<AdType, Any>()

    private val adIdCache = mutableMapOf<AdType, String>()

    var autoPreloadEnabled = true

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
        
        adIdCache[adType] = adId
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
        adIdCache.clear()
        onDestroy()
    }

    protected abstract fun doLoadAd(
        context: Context,
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

    protected fun getAdId(adType: AdType): String? {
        return adIdCache[adType]
    }

    protected fun shouldAutoPreload(adType: AdType): Boolean {
        return autoPreloadEnabled && (adType == AdType.INTERSTITIAL || adType == AdType.REWARD_VIDEO)
    }

    protected fun preloadAdOnDismiss(adType: AdType) {
        if (!shouldAutoPreload(adType)) {
            Log.d(TAG, "Auto preload disabled for ${adType.name}")
            return
        }

        val adId = getAdId(adType) ?: run {
            Log.w(TAG, "No cached adId for ${adType.name}, cannot preload")
            return
        }

        Log.d(TAG, "Starting auto preload for ${adType.name} with id: $adId")
        
        doLoadAd(App.context, adType, adId, object : IAdCallback {
            override fun onAdLoaded() {
                Log.d(TAG, "${adType.name} preloaded successfully and cached")
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                Log.w(TAG, "${adType.name} preload failed: $errorMessage")
                removeCachedAd(adType)
            }
        })
    }
}
