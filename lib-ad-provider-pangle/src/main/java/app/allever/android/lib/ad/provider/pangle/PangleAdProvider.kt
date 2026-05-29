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
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAd
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenAdLoadListener
import com.bytedance.sdk.openadsdk.api.open.PAGAppOpenRequest
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
    private var splashAd: PAGAppOpenAd? = null

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        initInternal(realInit = {
            val pagConfig = PAGConfig.Builder()
                .appId(config.appId)
                .debugLog(true)
                .supportMultiProcess(false)
                .build()

            PAGSdk.init(context, pagConfig, object : PAGSdk.PAGInitCallback {
                override fun success() {
                    finishInit(callback)
                }

                override fun fail(code: Int, message: String?) {
                    logE("$TAG: Pangle initialization failed: $code - $message")
                }
            })
        }, callback)
    }

    override fun onDestroy() {
        interstitialAd = null
        rewardedAd = null
        bannerAd = null
        splashAd = null
    }

    override fun loadSplashAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading splash ad: $adId")

        val request = PAGAppOpenRequest()
        PAGAppOpenAd.loadAd(adId, request, object : PAGAppOpenAdLoadListener {
            override fun onError(code: Int, message: String) {
                handleOnAdLoadFail(AdType.SPLASH, code, message, callback)
            }

            override fun onAdLoaded(ad: PAGAppOpenAd) {
                splashAd = ad
                handleOnAdLoaded(AdType.SPLASH, ad, callback)

                ad.setAdInteractionCallback(object : PAGAppOpenAdInteractionCallback() {
                    override fun onAdShowed() {
                        handleOnAdShow(AdType.SPLASH, callback)
                    }

                    override fun onAdClicked() {
                        handleOnAdClick(AdType.SPLASH, callback)
                    }

                    override fun onAdDismissed() {
                        splashAd = null
                        handleAdDismissed(AdType.SPLASH, callback)
                    }
                })
            }
        })
    }

    override fun showSplashAd(activity: Activity, callback: IAdCallback?) {
        showSplashAdInternal(splashAd, callback) {
            splashAd?.show(activity)
        }
    }

    override fun loadInterstitialAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading interstitial ad: $adId")

        val request = PAGInterstitialRequest()
        PAGInterstitialAd.loadAd(adId, request, object : PAGInterstitialAdLoadListener {
            override fun onError(code: Int, message: String) {
                handleOnAdLoadFail(AdType.INTERSTITIAL, code, message, callback)
            }

            override fun onAdLoaded(ad: PAGInterstitialAd) {
                interstitialAd = ad
                handleOnAdLoaded(AdType.INTERSTITIAL, ad, callback)

                ad.setAdInteractionCallback(object : PAGInterstitialAdInteractionCallback() {
                    override fun onAdShowed() {
                        handleOnAdShow(AdType.INTERSTITIAL, callback)
                    }

                    override fun onAdClicked() {
                        handleOnAdClick(AdType.INTERSTITIAL, callback)
                    }

                    override fun onAdDismissed() {
                        interstitialAd = null
                        handleAdDismissed(AdType.INTERSTITIAL, callback)
                    }

                    override fun onAdShowFailed(errorModel: PAGErrorModel) {
                        interstitialAd = null
                        handleOnAdShowFail(
                            AdType.INTERSTITIAL,
                            errorModel.errorCode,
                            errorModel.errorMessage,
                            callback
                        )
                    }
                })
            }
        })
    }

    override fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        showInterstitialAdInternal(interstitialAd, callback) {
            interstitialAd?.show(activity)
        }
    }

    override fun loadRewardedAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading rewarded ad: $adId")

        val request = PAGRewardedRequest()
        PAGRewardedAd.loadAd(adId, request, object : PAGRewardedAdLoadListener {
            override fun onError(code: Int, message: String) {
                handleOnAdLoadFail(AdType.REWARD_VIDEO, code, message, callback)
            }

            override fun onAdLoaded(ad: PAGRewardedAd) {
                rewardedAd = ad
                handleOnAdLoaded(AdType.REWARD_VIDEO, ad, callback)

                ad.setAdInteractionCallback(object : PAGRewardedAdInteractionCallback() {
                    override fun onAdShowed() {
                        handleOnAdShow(AdType.REWARD_VIDEO, callback)
                    }

                    override fun onAdClicked() {
                        handleOnAdClick(AdType.REWARD_VIDEO, callback)
                    }

                    override fun onAdDismissed() {
                        rewardedAd = null
                        handleAdDismissed(AdType.REWARD_VIDEO, callback)
                    }

                    override fun onUserEarnedReward(pagRewardItem: PAGRewardItem?) {
                        callback?.onAdRewarded(
                            pagRewardItem?.rewardAmount ?: 0,
                            pagRewardItem?.rewardName ?: ""
                        )
                    }
                })
            }
        })
    }

    override fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        showRewardedAdInternal(rewardedAd, callback) {
            rewardedAd?.show(activity)
        }
    }

    override fun loadBannerAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading banner ad: $adId")

        try {
            val bannerSize = PAGBannerSize.BANNER_W_320_H_50

            val request = PAGBannerRequest(bannerSize)

            PAGBannerAd.loadAd(adId, request, object : PAGBannerAdLoadListener {
                override fun onError(code: Int, message: String) {
                    handleOnAdLoadFail(AdType.BANNER, code, message, callback)
                }

                override fun onAdLoaded(ad: PAGBannerAd) {
                    bannerAd = ad
                    handleOnAdLoaded(AdType.BANNER, ad, callback)

                    ad.setAdInteractionCallback(object : PAGBannerAdInteractionCallback() {
                        override fun onAdShowed() {
                            handleOnAdShow(AdType.BANNER, callback)
                        }

                        override fun onAdClicked() {
                            handleOnAdClick(AdType.BANNER, callback)
                        }
                    })
                }
            })
        } catch (e: Exception) {
            handleOnAdLoadFail(AdType.BANNER, -1, e.message ?: "Unknown error", callback)
        }
    }

    override fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        val ad = bannerAd
        showBannerInternal(ad?.bannerView, container, callback)
    }
}
