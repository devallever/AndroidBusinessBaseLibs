package app.allever.android.lib.ad.provider.pangle

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerSize
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardItem
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAd
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdLoadListener
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedRequest

class PangleAdProvider : BaseAdProvider() {

    companion object {
        private const val TAG = "PangleAdProvider"
        const val PROVIDER_NAME = "PANGLE"
    }

    private var interstitialAd: PAGInterstitialAd? = null
    private var rewardedAd: PAGRewardedAd? = null
    private var bannerAd: PAGBannerAd? = null

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        log("$TAG: Initializing Pangle with appId: ${config.appId}")

        if (isInit()) {
            log("$TAG: Pangle already initialized")
            callback?.invoke()
            return
        }

        val pagConfig = PAGConfig.Builder()
            .appId(config.appId)
            .debugLog(true)
            .supportMultiProcess(false)
            .build()

        PAGSdk.init(context, pagConfig, object : PAGSdk.PAGInitCallback {
            override fun success() {
                isInitialized = true
                log("$TAG: Pangle initialized successfully")
                callback?.invoke()
            }

            override fun fail(code: Int, message: String?) {
                logE("$TAG: Pangle initialization failed: $code - $message")
                callback?.invoke()
            }
        })
    }

    override fun doLoadAd(
        context: Context,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        when (adType) {
            AdType.INTERSTITIAL -> loadInterstitialAd(adId, callback)
            AdType.REWARD_VIDEO -> loadRewardedAd(adId, callback)
            AdType.BANNER -> loadBannerAd(adId, callback)
            else -> {
                log("$TAG: ${adType.name} not supported yet for Pangle")
                callback?.onAdFail(-1, "${adType.name} not supported yet")
            }
        }
    }

    override fun doShowAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
        when (adType) {
            AdType.INTERSTITIAL -> showInterstitialAd(activity, callback)
            AdType.REWARD_VIDEO -> showRewardedAd(activity, callback)
            AdType.BANNER -> showBannerAd(container, callback)
            else -> {
                log("$TAG: ${adType.name} show not implemented for Pangle")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAd = null
        rewardedAd = null
        bannerAd = null
    }

    private fun loadInterstitialAd(adId: String, callback: IAdCallback?) {
        log("$TAG: Loading interstitial ad: $adId")

        val request = PAGInterstitialRequest()
        PAGInterstitialAd.loadAd(adId, request, object : PAGInterstitialAdLoadListener {
            override fun onError(code: Int, message: String) {
                log("$TAG: Interstitial ad failed to load: $code - $message")
                callback?.onAdFail(code, message)
            }

            override fun onAdLoaded(ad: PAGInterstitialAd) {
                log("$TAG: Interstitial ad loaded successfully")
                interstitialAd = ad
                cacheAd(AdType.INTERSTITIAL, ad)
                
                val simulatedECPM = generateSimulatedPrice()
                log("$TAG: Interstitial ad (simulated eCPM: $$simulatedECPM)")
                callback?.onAdLoadedWithPrice(simulatedECPM)

                ad.setAdInteractionCallback(object : PAGInterstitialAdInteractionCallback() {
                    override fun onAdShowed() {
                        log("$TAG: Interstitial ad showed")
                        callback?.onAdShow()
                    }

                    override fun onAdClicked() {
                        log("$TAG: Interstitial ad clicked")
                        callback?.onAdClick()
                    }

                    override fun onAdDismissed() {
                        log("$TAG: Interstitial ad dismissed")
                        interstitialAd = null
                        removeCachedAd(AdType.INTERSTITIAL)
                        callback?.onAdDismiss()

                        preloadAdOnDismiss(AdType.INTERSTITIAL)
                    }

                    override fun onAdShowFailed(errorModel: PAGErrorModel) {
                        log("$TAG: Interstitial ad show failed: ${errorModel.errorCode}")
                        interstitialAd = null
                        removeCachedAd(AdType.INTERSTITIAL)
                        callback?.onAdFail(errorModel.errorCode, errorModel.errorMessage ?: "Show failed")
                    }
                })
            }
        })
    }

    private fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        interstitialAd?.show(activity) ?: run {
            log("$TAG: Interstitial ad not ready")
            callback?.onAdFail(-1, "Interstitial ad not loaded")
        }
    }

    private fun loadRewardedAd(adId: String, callback: IAdCallback?) {
        log("$TAG: Loading rewarded ad: $adId")

        val request = PAGRewardedRequest()
        PAGRewardedAd.loadAd(adId, request, object : PAGRewardedAdLoadListener {
            override fun onError(code: Int, message: String) {
                log("$TAG: Rewarded ad failed to load: $code - $message")
                callback?.onAdFail(code, message)
            }

            override fun onAdLoaded(ad: PAGRewardedAd) {
                log("$TAG: Rewarded ad loaded successfully")
                rewardedAd = ad
                cacheAd(AdType.REWARD_VIDEO, ad)
                
                val simulatedECPM = generateSimulatedPrice()
                log("$TAG: Rewarded ad (simulated eCPM: $$simulatedECPM)")
                callback?.onAdLoadedWithPrice(simulatedECPM)

                ad.setAdInteractionCallback(object : PAGRewardedAdInteractionCallback() {
                    override fun onAdShowed() {
                        log("$TAG: Rewarded ad showed")
                        callback?.onAdShow()
                    }

                    override fun onAdClicked() {
                        log("$TAG: Rewarded ad clicked")
                        callback?.onAdClick()
                    }

                    override fun onAdDismissed() {
                        log("$TAG: Rewarded ad dismissed")
                        rewardedAd = null
                        removeCachedAd(AdType.REWARD_VIDEO)
                        callback?.onAdDismiss()

                        preloadAdOnDismiss(AdType.REWARD_VIDEO)
                    }

                    override fun onUserEarnedReward(pagRewardItem: PAGRewardItem?) {
                        log("$TAG: User earned reward: ${pagRewardItem?.rewardName} - ${pagRewardItem?.rewardAmount}")
                        callback?.onAdRewarded(
                            pagRewardItem?.rewardAmount?.toInt() ?: 0,
                            pagRewardItem?.rewardName ?: "coins"
                        )
                    }
                })
            }
        })
    }

    private fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        rewardedAd?.show(activity) ?: run {
            log("$TAG: Rewarded ad not ready")
            callback?.onAdFail(-1, "Rewarded ad not loaded")
        }
    }

    private fun loadBannerAd(adId: String, callback: IAdCallback?) {
        log("$TAG: Loading banner ad: $adId")

        try {
            val bannerSize = PAGBannerSize.BANNER_W_320_H_50

            val request = PAGBannerRequest(bannerSize)

            PAGBannerAd.loadAd(adId, request, object : PAGBannerAdLoadListener {
                override fun onError(code: Int, message: String) {
                    log("$TAG: Banner ad failed to load: $code - $message")
                    callback?.onAdFail(code, message)
                }

                override fun onAdLoaded(ad: PAGBannerAd) {
                    log("$TAG: Banner ad loaded successfully")
                    bannerAd = ad
                    cacheAd(AdType.BANNER, ad)
                    
                    val simulatedECPM = generateSimulatedPrice()
                    log("$TAG: Banner ad (simulated eCPM: $$simulatedECPM)")
                    callback?.onAdLoadedWithPrice(simulatedECPM)

                    ad.setAdInteractionCallback(object : PAGBannerAdInteractionCallback() {
                        override fun onAdShowed() {
                            log("$TAG: Banner ad showed")
                            callback?.onAdShow()
                        }

                        override fun onAdClicked() {
                            log("$TAG: Banner ad clicked")
                            callback?.onAdClick()
                        }
                    })
                }
            })
        } catch (e: Exception) {
            logE("$TAG: Error loading banner ad", e.message)
            callback?.onAdFail(-1, e.message ?: "Unknown error")
        }
    }

    private fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        val ad = bannerAd

        if (ad == null || container == null) {
            log("$TAG: Banner ad or container not ready")
            callback?.onAdFail(-1, "Banner ad not ready")
            return
        }

        try {
            container.removeAllViews()
            container.addView(ad.bannerView)
            log("$TAG: Banner ad showed")
            callback?.onAdShow()
        } catch (e: Exception) {
            logE("$TAG: Error showing banner ad", e.message)
            callback?.onAdFail(-1, e.message ?: "Unknown error")
        }
    }

    private fun generateSimulatedPrice(): Double {
        val minPrice = 1.0
        val maxPrice = 5.0
        return minPrice + (Math.random() * (maxPrice - minPrice))
    }
}
