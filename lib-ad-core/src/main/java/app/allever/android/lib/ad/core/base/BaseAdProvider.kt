package app.allever.android.lib.ad.core.base

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE

abstract class BaseAdProvider : IAdProvider {

    companion object {
        private const val TAG = "BaseAdProvider"
    }

    protected var isInitialized = false

    protected val adCache = mutableMapOf<AdType, Any>()

    private val adIdCache = mutableMapOf<AdType, String>()

    private val adCacheTimeMap = mutableMapOf<AdType, Long>()

    var autoPreloadEnabled = true

    private var cacheExpireTimeMs: Long = 60 * 60 * 1000L

    override fun isInit(): Boolean = isInitialized

    override fun isReady(adType: AdType): Boolean {
        if (!adCache.containsKey(adType)) {
            return false
        }
        return !isCacheExpired(adType)
    }

    override fun loadAd(
        context: Context,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        if (!isInitialized) {
            logE("$TAG: Ad provider not initialized, please call init() first")
            callback?.onAdFail(-1, "Ad provider not initialized")
            return
        }

        if (adId.isNotEmpty()) {
            adIdCache[adType] = adId
        }
        doLoadAd(context, adType, adId, callback)
    }

    override fun showAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        when {
            !adCache.containsKey(adType) -> {
                log("$TAG: ${adType.name} not cached, loading...")
                doLoadAd(activity, adType, adIdCache[adType] ?: return, callback)
            }

            isCacheExpired(adType) -> {
                log("$TAG: ${adType.name} cache expired (${getCacheAge(adType)}ms old), clearing and reloading")
                removeCachedAd(adType)
                callback?.onAdFail(-2, "Cache expired for ${adType.name}")

                val adId = getAdId(adType)
                if (adId != null) {
                    log("$TAG: Reloading ${adType.name} with id: $adId")
                    doLoadAd(activity, adType, adId, object : IAdCallback {
                        override fun onAdLoaded() {
                            log("$TAG: ${adType.name} reloaded successfully after expiration")
                        }

                        override fun onAdFail(errorCode: Int, errorMessage: String) {
                            log("$TAG: ${adType.name} reload failed after expiration: $errorMessage")
                        }
                    })
                } else {
                    log("$TAG: No adId found for ${adType.name}, cannot reload")
                }
            }

            else -> {
                doShowAd(activity, adType, container, callback)
            }
        }
    }

    override fun destroy() {
        adCache.clear()
        adIdCache.clear()
        adCacheTimeMap.clear()
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
        adCacheTimeMap[adType] = System.currentTimeMillis()
        log("$TAG: ${adType.name} cached at ${adCacheTimeMap[adType]}")
    }

    protected fun getCachedAd(adType: AdType): Any? {
        return if (isCacheExpired(adType)) {
            log("$TAG: ${adType.name} cache expired on access, removing")
            removeCachedAd(adType)
            null
        } else {
            adCache[adType]
        }
    }

    protected fun removeCachedAd(adType: AdType) {
        adCache.remove(adType)
        adCacheTimeMap.remove(adType)
    }

    protected fun getAdId(adType: AdType): String? {
        return adIdCache[adType]
    }

    protected fun shouldAutoPreload(adType: AdType): Boolean {
        return autoPreloadEnabled && (
            adType == AdType.INTERSTITIAL ||
            adType == AdType.REWARD_VIDEO ||
            adType == AdType.SPLASH
        )
    }

    protected fun preloadAdOnDismiss(adType: AdType) {
        if (!shouldAutoPreload(adType)) {
            log("$TAG: Auto preload disabled for ${adType.name}")
            return
        }

        log("$TAG: [PRELOAD] Starting preload for ${adType.name} (triggered by ad dismiss)")
        log("$TAG: [PRELOAD] 📌 This is the ONLY time we preload - after user closes the ad")
        log("$TAG: [PRELOAD] Strategy: Use → Close → Preload next → Ready for next show")

        when (AdManager.loadMode) {
            AdManager.LoadMode.BIDDING -> {
                log("$TAG: [BIDDING MODE] Re-bidding for ${adType.name} after dismiss")
                log("$TAG: [BIDDING] Will request ALL bidding providers and select winner")
                AdManager.preloadForBidding(App.context, adType)
            }
            AdManager.LoadMode.WATERFALL -> {
                log("$TAG: [WATERFALL MODE] Preloading ${adType.name} after dismiss")
                AdManager.preloadForWaterfall(App.context, adType)
            }
            AdManager.LoadMode.SINGLE -> {
                //log
                log("$TAG: [SINGLE MODE] Preloading ${adType.name} after dismiss")
                AdManager.preloadForSingle(App.context, adType)
            }
        }
    }

    /**
     * 检查缓存是否过期
     */
    private fun isCacheExpired(adType: AdType): Boolean {
        val age = getCacheAge(adType)
        val expired = age > cacheExpireTimeMs

        if (expired) {
            logE("$TAG: ${adType.name} cache age: ${age}ms > expire time: ${cacheExpireTimeMs}ms")
        }

        return expired
    }

    private fun getCacheAge(adType: AdType): Long {
        val cacheTime = adCacheTimeMap[adType] ?: return -1
        return System.currentTimeMillis() - cacheTime
    }
}
