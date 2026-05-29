package app.allever.android.lib.ad.core.base

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE

abstract class BaseAdProvider : IAdProvider {

    protected open fun loadSplashAd(context: Context, adId: String, callback: IAdCallback?) {}
    protected open fun loadInterstitialAd(context: Context, adId: String, callback: IAdCallback?) {}
    protected open fun loadRewardedAd(context: Context, adId: String, callback: IAdCallback?) {}
    protected open fun loadBannerAd(context: Context, adId: String, callback: IAdCallback?) {}

    protected open fun showSplashAd(activity: Activity, callback: IAdCallback?) {}
    protected open fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {}
    protected open fun showRewardedAd(activity: Activity, callback: IAdCallback?) {}
    protected open fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {}

    protected abstract fun onDestroy()

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

    protected fun initInternal(
        realInit: () -> Unit,
        callback: (() -> Unit)?
    ) {
        val providerType = getProviderType()
        log("${TAG}: $providerType Initializing...")

        if (isInit()) {
            log("${TAG}: $providerType already initialized")
            callback?.invoke()
            return
        }

        realInit.invoke()
    }

    protected fun finishInit(callback: (() -> Unit)?) {
        isInitialized = true
        val providerType = getProviderType()
        log("${TAG}: $providerType initialized successfully")
        callback?.invoke()
    }

    protected fun handleOnAdLoaded(adType: AdType, ad: Any, callback: IAdCallback?) {
        val providerType = getProviderType()
        log("${TAG}: $providerType ${adType.name} ad loaded successfully")
        cacheAd(adType, ad)
        val simulatedECPM = generateSimulatedPrice()
        log("${TAG}: $providerType ${adType.name} ad (simulated eCPM: $$simulatedECPM)")
        callback?.onAdLoadedWithPrice(simulatedECPM)
    }

    protected fun handleOnAdLoadFail(
        adType: AdType,
        errorCode: Int,
        errorMessage: String,
        callback: IAdCallback?
    ) {
        val providerType = getProviderType()
        log("${TAG}: $providerType ${adType.name} ad failed to load: $errorMessage")
        callback?.onAdFail(errorCode, errorMessage)
    }

    protected fun handleOnAdShow(adType: AdType, callback: IAdCallback?) {
        val providerType = getProviderType()
        log("${TAG}: $providerType ${adType.name} ad showed")
        callback?.onAdShow()
    }

    protected fun handleOnAdShowFail(
        adType: AdType,
        errorCode: Int,
        errorMessage: String,
        callback: IAdCallback?
    ) {
        val providerType = getProviderType()
        log("${TAG}: $providerType ${adType.name} ad failed to show: $errorMessage")
        callback?.onAdFail(errorCode, errorMessage)
    }

    //click
    protected fun handleOnAdClick(adType: AdType, callback: IAdCallback?) {
        val providerType = getProviderType()
        log("${TAG}: $providerType ${adType.name} ad clicked")
        callback?.onAdClick()
    }

    protected fun handleAdDismissed(adType: AdType, callback: IAdCallback?) {
        val providerType = getProviderType()
        log("${TAG}: $providerType ${adType.name} ad dismissed")
        removeCachedAd(adType)
        callback?.onAdDismiss()
        preloadAdOnDismiss(adType)
    }

    protected fun handleOnAdRewarded(
        adType: AdType,
        rewardAmount: Int,
        rewardName: String,
        callback: IAdCallback?
    ) {
        val providerType = getProviderType()
        log("${TAG}:${adType.name}: User earned reward from $providerType ")
        callback?.onAdRewarded(rewardAmount, rewardName)
    }

    protected fun showBannerInternal(
        bannerView: View?,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        if (bannerView == null || container == null) {
            handleOnAdShowFail(AdType.BANNER, -1, "Banner ad not ready", callback)
            return
        }

        try {
            container.removeAllViews()
            container.addView(bannerView)
            handleOnAdShow(AdType.BANNER, callback)
        } catch (e: Exception) {
            handleOnAdShowFail(AdType.BANNER, -1, e.message ?: "Unknown error", callback)
        }
    }

    protected fun showSplashAdInternal(
        splashAd: Any?,
        callback: IAdCallback?,
        realShow: () -> Unit
    ) {
        splashAd?.let { ad ->
            try {
                realShow.invoke()
                callback?.onAdShow()
            } catch (e: Exception) {
                handleOnAdShowFail(AdType.SPLASH, -1, e.message ?: "Unknown error", callback)
            }
        } ?: run {
            handleOnAdShowFail(AdType.SPLASH, -1, "Splash ad not ready", callback)
        }
    }

    protected fun showInterstitialAdInternal(
        interstitialAd: Any?,
        callback: IAdCallback?,
        realShow: () -> Unit
    ) {
        interstitialAd?.let { ad ->
            try {
                realShow.invoke()
                callback?.onAdShow()
            } catch (e: Exception) {
                handleOnAdShowFail(AdType.INTERSTITIAL, -1, e.message ?: "Unknown error", callback)
            }
        }
    }

    protected fun showRewardedAdInternal(
        rewardedAd: Any?,
        callback: IAdCallback?,
        realShow: () -> Unit
    ) {
        rewardedAd?.let { ad ->
            try {
                realShow.invoke()
                callback?.onAdShow()
            } catch (e: Exception) {
                handleOnAdShowFail(AdType.REWARD_VIDEO, -1, e.message ?: "Unknown error", callback)
            }
        }
    }

    protected open fun doLoadAd(
        context: Context,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        when (adType) {
            AdType.SPLASH -> loadSplashAd(context, adId, callback)
            AdType.INTERSTITIAL -> loadInterstitialAd(context, adId, callback)
            AdType.REWARD_VIDEO -> loadRewardedAd(context, adId, callback)
            AdType.BANNER -> loadBannerAd(context, adId, callback)
            else -> {
                handleOnAdLoadFail(adType, -1, "${adType.name} not supported yet", callback)
            }
        }
    }

    protected open fun doShowAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        when (adType) {
            AdType.SPLASH -> showSplashAd(activity, callback)
            AdType.INTERSTITIAL -> showInterstitialAd(activity, callback)
            AdType.REWARD_VIDEO -> showRewardedAd(activity, callback)
            AdType.BANNER -> showBannerAd(container, callback)
            else -> {
                log("${getProviderType()}: ${adType.name} show not implemented")
            }
        }
    }

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

        AdManager.currentStrategy.preload(App.context, adType)
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

    protected fun generateSimulatedPrice(): Double {
        val minPrice = 1.0
        val maxPrice = 5.0
        return minPrice + (Math.random() * (maxPrice - minPrice))
    }
}
