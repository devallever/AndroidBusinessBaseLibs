package app.allever.android.lib.ad.core.provider

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import kotlin.random.Random

class MockAdProvider : BaseAdProvider() {

    companion object {
        private const val TAG = "MockAdProvider"
        const val PROVIDER_NAME = "MOCK"
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(config: Map<String, Any>, callback: (() -> Unit)?) {
        log("$TAG: Initializing MockAdProvider with config: $config")
        if (isInit()) {
            log("$TAG: MockAdProvider already initialized")
            callback?.invoke()
            return
        }
        mainHandler.postDelayed({
            isInitialized = true
            log("$TAG: MockAdProvider initialized successfully")
            callback?.invoke()
        }, 500)
    }

    override fun doLoadAd(
        activity: Context,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading ${adType.name} ad with id: $adId")

        mainHandler.postDelayed({
            if (Random.nextBoolean()) {
                log("$TAG: ${adType.name} ad loaded successfully")
                cacheAd(adType, MockAdWrapper(adType, adId))
                callback?.onAdLoaded()
            } else {
                log("$TAG: ${adType.name} ad load failed (simulated)")
                callback?.onAdFail(-1, "Simulated load failure for ${adType.name}")
            }
        }, 1000)
    }

    override fun doShowAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        log("$TAG: Showing ${adType.name} ad")

        when (adType) {
            AdType.SPLASH -> handleSplashShow(activity, container, callback)
            AdType.INTERSTITIAL -> handleInterstitialShow(activity, callback)
            AdType.REWARD_VIDEO -> handleRewardVideoShow(activity, callback)
            AdType.BANNER -> handleBannerShow(container, callback)
            AdType.NATIVE -> handleNativeShow(container, callback)
        }

        removeCachedAd(adType)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("$TAG: MockAdProvider destroyed")
    }

    private fun handleSplashShow(
        activity: Activity,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        log("$TAG: Showing splash ad in container")
        mainHandler.postDelayed({
            callback?.onAdShow()
            mainHandler.postDelayed({
                callback?.onAdDismiss()
            }, 3000)
        }, 500)
    }

    private fun handleInterstitialShow(
        activity: Activity,
        callback: IAdCallback?
    ) {
        log("$TAG: Showing interstitial ad")
        mainHandler.postDelayed({
            callback?.onAdShow()
            mainHandler.postDelayed({
                callback?.onAdDismiss()
            }, 3000)
        }, 500)
    }

    private fun handleRewardVideoShow(
        activity: Activity,
        callback: IAdCallback?
    ) {
        log("$TAG: Showing reward video ad")
        mainHandler.postDelayed({
            callback?.onAdShow()
            mainHandler.postDelayed({
                callback?.onAdRewarded(1, "coins")
                callback?.onAdDismiss()
            }, 5000)
        }, 500)
    }

    private fun handleBannerShow(
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        log("$TAG: Showing banner ad in container")
        mainHandler.postDelayed({
            callback?.onAdShow()
        }, 300)
    }

    private fun handleNativeShow(
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        log("$TAG: Showing native ad in container")
        mainHandler.postDelayed({
            callback?.onAdShow()
        }, 300)
    }

    data class MockAdWrapper(val adType: AdType, val adId: String)
}
