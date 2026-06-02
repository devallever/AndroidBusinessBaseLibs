package app.allever.android.lib.ad.core.base

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.strategy.AdLog
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.app.App

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

    protected val TAG: String
        get() = this::class.simpleName ?: "BaseAdProvider"

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
        val providerType = getProviderType()

        if (!isInitialized) {
            AdLog.logMessage(
                message = "Ad provider not initialized, please call init() first",
                providerType = providerType,
                adType = adType,
                success = false
            )
            callback?.onAdFail(-1, "Ad provider not initialized")
            return
        }

        if (adId.isNotEmpty()) {
            adIdCache[adType] = adId
        }

        AdLog.logMessage(
            message = "Starting to load | ID: $adId",
            providerType = providerType,
            adType = adType,
        )

        doLoadAd(context, adType, adId, callback)
    }

    override fun showAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        val providerType = getProviderType()

        when {
            !adCache.containsKey(adType) -> {
                AdLog.logMessage(
                    message = "${adType.name} not cached, loading...",
                    providerType = providerType,
                    adType = adType,
                )
                doLoadAd(activity, adType, adIdCache[adType] ?: return, callback)
            }

            isCacheExpired(adType) -> {
                AdLog.logMessage(
                    message = "${adType.name} cache expired (${getCacheAge(adType)}ms old), clearing and reloading",
                    providerType = providerType,
                    adType = adType,
                    success = false
                )
                removeCachedAd(adType)
                callback?.onAdFail(-2, "Cache expired for ${adType.name}")

                val adId = getAdId(adType)
                if (adId != null) {
                    AdLog.logMessage(
                        message = "Reloading ${adType.name} | ID: $adId",
                        providerType = providerType,
                        adType = adType,
                    )
                    doLoadAd(activity, adType, adId, object : IAdCallback {
                        override fun onAdLoaded() {
                            AdLog.logMessage(
                                message = "${adType.name} reloaded successfully after expiration",
                                providerType = providerType,
                                adType = adType,
                                success = true
                            )
                        }

                        override fun onAdFail(errorCode: Int, errorMessage: String) {
                            AdLog.logMessage(
                                message = "${adType.name} reload failed after expiration: $errorMessage",
                                providerType = providerType,
                                adType = adType,
                                success = false
                            )
                        }
                    })
                } else {
                    AdLog.logMessage(
                        message = "No adId found for ${adType.name}, cannot reload",
                        providerType = providerType,
                        adType = adType,
                        success = false
                    )
                }
            }

            else -> {
                doShowAd(activity, adType, container, callback)
            }
        }
    }

    override fun destroy() {
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "Destroying provider - clearing all caches",
            providerType = providerType,
        )
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
        AdLog.logMessage(
            message = "Initializing...",
            providerType = providerType,
        )

        if (isInit()) {
            AdLog.logMessage(
                message = "Already initialized",
                providerType = providerType,
                adType = AdType.BANNER,
                success = true
            )
            callback?.invoke()
            return
        }

        realInit.invoke()
    }

    protected fun finishInit(callback: (() -> Unit)?) {
        isInitialized = true
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "Initialized successfully",
            providerType = providerType,
            success = true
        )
        callback?.invoke()
    }

    protected fun handleOnAdLoaded(adType: AdType, ad: Any, callback: IAdCallback?) {
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "${adType.name} ad loaded successfully",
            providerType = providerType,
            adType = adType,
            success = true
        )
        cacheAd(adType, ad)
        val simulatedECPM = generateSimulatedPrice()
        
        AdLog.logMessage(
            message = "Simulated eCPM: $$simulatedECPM",
            providerType = providerType,
            adType = adType,
        )
        
        callback?.onAdLoadedWithPrice(simulatedECPM)
    }

    protected fun handleOnAdLoadFail(
        adType: AdType,
        errorCode: Int,
        errorMessage: String,
        callback: IAdCallback?
    ) {
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "${adType.name} ad failed to load | Error($errorCode): $errorMessage",
            providerType = providerType,
            adType = adType,
            success = false
        )
        callback?.onAdFail(errorCode, errorMessage)
    }

    protected fun handleOnAdShow(adType: AdType, callback: IAdCallback?) {
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "${adType.name} ad showed",
            providerType = providerType,
            adType = adType,
            success = true
        )
        callback?.onAdShow()
    }

    protected fun handleOnAdShowFail(
        adType: AdType,
        errorCode: Int,
        errorMessage: String,
        callback: IAdCallback?
    ) {
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "${adType.name} ad failed to show | Error($errorCode): $errorMessage",
            providerType = providerType,
            adType = adType,
            success = false
        )
        callback?.onAdFail(errorCode, errorMessage)
    }

    protected fun handleOnAdClick(adType: AdType, callback: IAdCallback?) {
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "${adType.name} ad clicked",
            providerType = providerType,
            adType = adType,
        )
        callback?.onAdClick()
    }

    protected fun handleAdDismissed(adType: AdType, callback: IAdCallback?) {
        val providerType = getProviderType()
        AdLog.logMessage(
            message = "${adType.name} ad dismissed",
            providerType = providerType,
            adType = adType,
        )
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
        AdLog.logMessage(
            message = "User earned reward | Amount: $rewardAmount, Name: $rewardName",
            providerType = providerType,
            adType = adType,
            success = true
        )
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
        } ?: run {
            handleOnAdShowFail(AdType.INTERSTITIAL, -1, "Interstitial ad not ready", callback)
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
        } ?: run {
            handleOnAdShowFail(AdType.REWARD_VIDEO, -1, "Rewarded ad not ready", callback)
        }
    }

    protected open fun doLoadAd(
        context: Context,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        val providerType = getProviderType()
        
        when (adType) {
            AdType.SPLASH -> loadSplashAd(context, adId, callback)
            AdType.INTERSTITIAL -> loadInterstitialAd(context, adId, callback)
            AdType.REWARD_VIDEO -> loadRewardedAd(context, adId, callback)
            AdType.BANNER -> loadBannerAd(context, adId, callback)
            else -> {
                AdLog.logMessage(
                    message = "${adType.name} not supported yet",
                    providerType = providerType,
                    adType = adType,
                    success = false
                )
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
        val providerType = getProviderType()
        
        when (adType) {
            AdType.SPLASH -> showSplashAd(activity, callback)
            AdType.INTERSTITIAL -> showInterstitialAd(activity, callback)
            AdType.REWARD_VIDEO -> showRewardedAd(activity, callback)
            AdType.BANNER -> showBannerAd(container, callback)
            else -> {
                AdLog.logMessage(
                    message = "${adType.name} show not implemented",
                    providerType = providerType,
                    adType = adType,
                    success = false
                )
            }
        }
    }

    protected fun cacheAd(adType: AdType, ad: Any) {
        val providerType = getProviderType()
        adCache[adType] = ad
        adCacheTimeMap[adType] = System.currentTimeMillis()
        
        AdLog.logMessage(
            message = "${adType.name} cached at ${adCacheTimeMap[adType]}",
            providerType = providerType,
            adType = adType,
        )
    }

    protected fun getCachedAd(adType: AdType): Any? {
        val providerType = getProviderType()
        
        return if (isCacheExpired(adType)) {
            AdLog.logMessage(
                message = "${adType.name} cache expired on access, removing",
                providerType = providerType,
                adType = adType,
                success = false
            )
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
        val providerType = getProviderType()
        
        if (!shouldAutoPreload(adType)) {
            AdLog.logMessage(
                message = "Auto preload disabled for ${adType.name}",
                providerType = providerType,
                adType = adType,
            )
            return
        }

        AdLog.logMessage(
            message = "Starting preload (triggered by ad dismiss)",
            providerType = providerType,
            adType = adType,
        )
        AdLog.logMessage(
            message = "📌 This is the ONLY time we preload - after user closes the ad",
            providerType = providerType,
            adType = adType,
        )
        AdLog.logMessage(
            message = "Strategy: Use → Close → Preload next → Ready for next show",
            providerType = providerType,
            adType = adType,
        )

        AdManager.currentStrategy.preload(App.context, adType)
    }

    private fun isCacheExpired(adType: AdType): Boolean {
        val providerType = getProviderType()
        val age = getCacheAge(adType)
        val expired = age > cacheExpireTimeMs

        if (expired) {
            AdLog.logMessage(
                message = "${adType.name} cache expired | Age: ${age}ms > Expire time: ${cacheExpireTimeMs}ms",
                providerType = providerType,
                adType = adType,
                success = false
            )
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
