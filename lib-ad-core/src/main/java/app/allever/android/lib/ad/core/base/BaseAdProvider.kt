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
        when {
            !adCache.containsKey(adType) -> {
                Log.d(TAG, "${adType.name} not cached, loading...")
                doLoadAd(activity, adType, adIdCache[adIdCache.keys.firstOrNull()] ?: return, callback)
            }
            
            isCacheExpired(adType) -> {
                Log.w(TAG, "${adType.name} cache expired (${getCacheAge(adType)}ms old), clearing and reloading")
                removeCachedAd(adType)
                callback?.onAdFail(-2, "Cache expired for ${adType.name}")
                
                val adId = getAdId(adType)
                if (adId != null) {
                    Log.d(TAG, "Reloading ${adType.name} with id: $adId")
                    doLoadAd(activity, adType, adId, object : IAdCallback {
                        override fun onAdLoaded() {
                            Log.d(TAG, "${adType.name} reloaded successfully after expiration")
                        }

                        override fun onAdFail(errorCode: Int, errorMessage: String) {
                            Log.w(TAG, "${adType.name} reload failed after expiration: $errorMessage")
                        }
                    })
                } else {
                    Log.w(TAG, "No adId found for ${adType.name}, cannot reload")
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
        Log.d(TAG, "${adType.name} cached at ${adCacheTimeMap[adType]}")
    }

    protected fun getCachedAd(adType: AdType): Any? {
        return if (isCacheExpired(adType)) {
            Log.w(TAG, "${adType.name} cache expired on access, removing")
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

    private fun isCacheExpired(adType: AdType): Boolean {
        val cacheTime = adCacheTimeMap[adType] ?: return true
        val age = System.currentTimeMillis() - cacheTime
        val expired = age > cacheExpireTimeMs
        
        if (expired) {
            Log.w(TAG, "${adType.name} cache age: ${age}ms > expire time: ${cacheExpireTimeMs}ms")
        }
        
        return expired
    }

    private fun getCacheAge(adType: AdType): Long {
        val cacheTime = adCacheTimeMap[adType] ?: return -1
        return System.currentTimeMillis() - cacheTime
    }
}
