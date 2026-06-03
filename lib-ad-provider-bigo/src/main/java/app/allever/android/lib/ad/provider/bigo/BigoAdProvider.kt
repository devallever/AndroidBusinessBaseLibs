package app.allever.android.lib.ad.provider.bigo

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import sg.bigo.ads.BigoAdSdk
import sg.bigo.ads.api.AdConfig
import sg.bigo.ads.api.AdError
import sg.bigo.ads.api.AdInteractionListener
import sg.bigo.ads.api.AdLoadListener
import sg.bigo.ads.api.AdSize
import sg.bigo.ads.api.BannerAd
import sg.bigo.ads.api.BannerAdLoader
import sg.bigo.ads.api.BannerAdRequest
import sg.bigo.ads.api.InterstitialAd
import sg.bigo.ads.api.InterstitialAdLoader
import sg.bigo.ads.api.InterstitialAdRequest
import sg.bigo.ads.api.RewardAdInteractionListener
import sg.bigo.ads.api.RewardVideoAd
import sg.bigo.ads.api.RewardVideoAdLoader
import sg.bigo.ads.api.RewardVideoAdRequest
import sg.bigo.ads.api.SplashAd
import sg.bigo.ads.api.SplashAdInteractionListener
import sg.bigo.ads.api.SplashAdLoader
import sg.bigo.ads.api.SplashAdRequest

import java.lang.ref.WeakReference

class BigoAdProvider : BaseAdProvider() {

    companion object {
        private const val TAG = "BigoAdProvider"
        const val PROVIDER_NAME = "Bigo"
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardVideoAd? = null
    private var bannerAd: BannerAd? = null
    private var splashAd: SplashAd? = null

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        val safeCallback = WeakReference(callback)

        initInternal(realInit = {
            val bigoConfig = AdConfig.Builder()
                .setAppId(config.appId)
                .setDebug(true)
                .build()

            BigoAdSdk.initialize(context, bigoConfig) {
                safeCallback.get()?.invoke()
                finishInit(null)
            }
        }, callback = null)
    }

    override fun onDestroy() {
        interstitialAd?.setAdInteractionListener(null)
        rewardedAd?.setAdInteractionListener(null)
        splashAd?.setAdInteractionListener(null)
        bannerAd?.setAdInteractionListener(null)
        interstitialAd = null
        rewardedAd = null
        bannerAd = null
        splashAd = null
    }

    override fun loadSplashAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading splash ad: $adId")

        val request = SplashAdRequest.Builder()
            .withSlotId(adId)
            .build()

        val loader = SplashAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<SplashAd> {
                override fun onError(adError: AdError) {
                    handleOnAdLoadFail(AdType.SPLASH, adError.code, adError.message, callback)
                }

                override fun onAdLoaded(ad: SplashAd) {
                    splashAd = ad
                    handleOnAdLoaded(AdType.SPLASH, ad, callback)

                    ad.setAdInteractionListener(object : SplashAdInteractionListener {
                        override fun onAdError(adError: AdError) {
                            splashAd = null
                            handleOnAdLoadFail(
                                AdType.SPLASH,
                                adError.code,
                                adError.message,
                                callback
                            )
                        }

                        override fun onAdImpression() {
                            handleOnAdShow(AdType.SPLASH, callback)
                        }

                        override fun onAdClicked() {
                            handleOnAdClick(AdType.SPLASH, callback)
                        }

                        override fun onAdOpened() {
                            log("$TAG: Splash ad opened")
                        }

                        override fun onAdClosed() {

                        }

                        override fun onAdSkipped() {
                            splashAd = null
                            handleAdDismissed(AdType.SPLASH, callback)
                        }

                        override fun onAdFinished() {
                            log("$TAG: Splash ad finish")

                        }
                    })
                }
            })
            .build()

        loader.loadAd(request)
    }

    override fun showSplashAd(activity: Activity, callback: IAdCallback?) {
        showSplashAdInternal(splashAd, callback) {
            splashAd?.show(activity)
        }
    }

    override fun loadInterstitialAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading interstitial ad: $adId")

        val request = InterstitialAdRequest.Builder()
            .withSlotId(adId)
            .build()

        val loader = InterstitialAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<InterstitialAd> {
                override fun onError(adError: AdError) {
                    handleOnAdLoadFail(AdType.INTERSTITIAL, adError.code, adError.message, callback)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    handleOnAdLoaded(AdType.INTERSTITIAL, ad, callback)

                    ad.setAdInteractionListener(object : AdInteractionListener {
                        override fun onAdError(adError: AdError) {
                            interstitialAd = null
                            handleOnAdShowFail(
                                AdType.INTERSTITIAL,
                                adError.code,
                                adError.message,
                                callback
                            )
                        }

                        override fun onAdImpression() {
                            handleOnAdShow(AdType.INTERSTITIAL, callback)
                        }

                        override fun onAdClicked() {
                            handleOnAdClick(AdType.INTERSTITIAL, callback)
                        }

                        override fun onAdOpened() {
                        }

                        override fun onAdClosed() {
                            interstitialAd = null
                            handleAdDismissed(AdType.INTERSTITIAL, callback)
                        }
                    })
                }
            })
            .build()

        loader.loadAd(request)
    }

    override fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        showInterstitialAdInternal(interstitialAd, callback) {
            interstitialAd?.show(activity)
        }
    }

    override fun loadRewardedAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading rewarded ad: $adId")

        val request = RewardVideoAdRequest.Builder()
            .withSlotId(adId)
            .build()

        val loader = RewardVideoAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<RewardVideoAd> {
                override fun onError(adError: AdError) {
                    handleOnAdLoadFail(AdType.REWARD_VIDEO, adError.code, adError.message, callback)
                }

                override fun onAdLoaded(ad: RewardVideoAd) {
                    rewardedAd = ad
                    handleOnAdLoaded(AdType.REWARD_VIDEO, ad, callback)

                    ad.setAdInteractionListener(object : RewardAdInteractionListener {
                        override fun onAdError(adError: AdError) {
                            rewardedAd = null
                            handleOnAdShowFail(
                                AdType.REWARD_VIDEO,
                                adError.code,
                                adError.message,
                                callback
                            )
                        }

                        override fun onAdImpression() {
                            handleOnAdShow(AdType.REWARD_VIDEO, callback)
                        }

                        override fun onAdClicked() {
                            handleOnAdClick(AdType.REWARD_VIDEO, callback)
                        }

                        override fun onAdOpened() {
                        }

                        override fun onAdClosed() {
                            rewardedAd = null
                            handleAdDismissed(AdType.REWARD_VIDEO, callback)
                        }

                        override fun onAdRewarded() {
                            handleOnAdRewarded(AdType.REWARD_VIDEO, 1, "conins", callback)
                        }

                    })
                }
            })
            .build()

        loader.loadAd(request)
    }

    override fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        showRewardedAdInternal(rewardedAd, callback) {
            rewardedAd?.show(activity)
        }
    }

    override fun loadBannerAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading banner ad: $adId")

        try {
            val request = BannerAdRequest.Builder()
                .withSlotId(adId)
                .withAdSizes(AdSize.LARGE_BANNER)
                .build()

            val loader = BannerAdLoader.Builder()
                .withAdLoadListener(object : AdLoadListener<BannerAd> {
                    override fun onError(adError: AdError) {
                        handleOnAdLoadFail(AdType.BANNER, adError.code, adError.message, callback)
                    }

                    override fun onAdLoaded(ad: BannerAd) {
                        bannerAd = ad
                        handleOnAdLoaded(AdType.BANNER, ad, callback)

                        ad.setAdInteractionListener(object : AdInteractionListener {
                            override fun onAdError(adError: AdError) {
                                bannerAd = null
                                handleOnAdShowFail(
                                    AdType.BANNER,
                                    adError.code,
                                    adError.message,
                                    callback
                                )
                            }

                            override fun onAdImpression() {
                                handleOnAdShow(AdType.BANNER, callback)
                            }

                            override fun onAdClicked() {
                                handleOnAdClick(AdType.BANNER, callback)
                            }

                            override fun onAdOpened() {
                            }

                            override fun onAdClosed() {
                                handleAdDismissed(AdType.BANNER, callback)
                            }
                        })
                    }
                })
                .build()

            loader.loadAd(request)
        } catch (e: Exception) {
            handleOnAdShowFail(AdType.BANNER, -1, e.message ?: "Unknown error", callback)
        }
    }

    override fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        val ad = bannerAd
        showBannerInternal(ad?.adView(), container, callback)
    }

    override fun destroy() {
        interstitialAd = null
        rewardedAd = null
        splashAd = null
        bannerAd = null
    }
}
