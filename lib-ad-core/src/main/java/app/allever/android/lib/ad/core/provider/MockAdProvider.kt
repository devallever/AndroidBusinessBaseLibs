package app.allever.android.lib.ad.core.provider

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import kotlin.random.Random

class MockAdProvider : BaseAdProvider() {

    companion object {
        private const val TAG = "MockAdProvider"
        const val PROVIDER_NAME = "MOCK"
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(config: Map<String, Any>, callback: (() -> Unit)?) {
        Log.d(TAG, "Initializing MockAdProvider with config: $config")
        mainHandler.postDelayed({
            isInitialized = true
            Log.d(TAG, "MockAdProvider initialized successfully")
            callback?.invoke()
        }, 500)
    }

    override fun doLoadAd(
        activity: Activity,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        Log.d(TAG, "Loading ${adType.name} ad with id: $adId")

        mainHandler.postDelayed({
            if (Random.nextBoolean()) {
                Log.d(TAG, "${adType.name} ad loaded successfully")
                cacheAd(adType, MockAdWrapper(adType, adId))
                callback?.onAdLoaded()
            } else {
                Log.d(TAG, "${adType.name} ad load failed (simulated)")
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
        Log.d(TAG, "Showing ${adType.name} ad")

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
        Log.d(TAG, "MockAdProvider destroyed")
    }

    private fun handleSplashShow(
        activity: Activity,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        Log.d(TAG, "Showing splash ad in container")
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
        Log.d(TAG, "Showing interstitial ad")
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
        Log.d(TAG, "Showing reward video ad")
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
        Log.d(TAG, "Showing banner ad in container")
        mainHandler.postDelayed({
            callback?.onAdShow()
        }, 300)
    }

    private fun handleNativeShow(
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        Log.d(TAG, "Showing native ad in container")
        mainHandler.postDelayed({
            callback?.onAdShow()
        }, 300)
    }

    data class MockAdWrapper(val adType: AdType, val adId: String)
}
