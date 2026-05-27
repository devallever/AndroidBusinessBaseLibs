package app.allever.android.lib.ad.provider.bigo

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
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

class BigoAdProvider : BaseAdProvider() {

    companion object {
        private const val TAG = "BigoAdProvider"
        const val PROVIDER_NAME = "BIGO"
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardVideoAd? = null
    private var bannerAd: BannerAd? = null

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        log("$TAG: Initializing Bigo with appId: ${config.appId}")

        if (isInit()) {
            log("$TAG: Bigo already initialized")
            callback?.invoke()
            return
        }

        val bigoConfig = AdConfig.Builder()  // ✅ 现在 Bigo 的 AdConfig 不会冲突了！
            .setAppId(config.appId)
            .setDebug(true)
            .build()

        BigoAdSdk.initialize(context, bigoConfig) {
            isInitialized = true
            log("$TAG: Bigo initialized successfully")
            callback?.invoke()
        }
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
                log("$TAG: ${adType.name} not supported yet for Bigo")
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
                log("$TAG: ${adType.name} show not implemented for Bigo")
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

        val request = InterstitialAdRequest.Builder()
            .withSlotId(adId)
            .build()

        val loader = InterstitialAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<InterstitialAd> {
                override fun onError(adError: AdError) {
                    log("$TAG: Interstitial ad failed to load: ${adError.code}")
                    callback?.onAdFail(adError.code, adError.message ?: "Load failed")
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    log("$TAG: Interstitial ad loaded successfully")
                    interstitialAd = ad
                    cacheAd(AdType.INTERSTITIAL, ad)
                    
                    val simulatedECPM = generateSimulatedPrice()
                    log("$TAG: Interstitial ad (simulated eCPM: $$simulatedECPM)")
                    callback?.onAdLoadedWithPrice(simulatedECPM)

                    ad.setAdInteractionListener(object : AdInteractionListener {
                        override fun onAdError(adError: AdError) {
                            log("$TAG: Interstitial ad error: ${adError.code}")
                            interstitialAd = null
                            removeCachedAd(AdType.INTERSTITIAL)
                            callback?.onAdFail(adError.code, adError.message ?: "Ad error")
                        }

                        override fun onAdImpression() {
                            log("$TAG: Interstitial ad showed")
                            callback?.onAdShow()
                        }

                        override fun onAdClicked() {
                            log("$TAG: Interstitial ad clicked")
                            callback?.onAdClick()
                        }

                        override fun onAdOpened() {
                            log("$TAG: Interstitial ad opened")
                        }

                        override fun onAdClosed() {
                            log("$TAG: Interstitial ad dismissed")
                            interstitialAd = null
                            removeCachedAd(AdType.INTERSTITIAL)
                            callback?.onAdDismiss()

                            preloadAdOnDismiss(AdType.INTERSTITIAL)
                        }
                    })
                }
            })
            .build()

        loader.loadAd(request)
    }

    private fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        interstitialAd?.show(activity) ?: run {
            log("$TAG: Interstitial ad not ready")
            callback?.onAdFail(-1, "Interstitial ad not loaded")
        }
    }

    private fun loadRewardedAd(adId: String, callback: IAdCallback?) {
        log("$TAG: Loading rewarded ad: $adId")

        val request = RewardVideoAdRequest.Builder()
            .withSlotId(adId)
            .build()

        val loader = RewardVideoAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<RewardVideoAd> {
                override fun onError(adError: AdError) {
                    log("$TAG: Rewarded ad failed to load: ${adError.code}")
                    callback?.onAdFail(adError.code, adError.message ?: "Load failed")
                }

                override fun onAdLoaded(ad: RewardVideoAd) {
                    log("$TAG: Rewarded ad loaded successfully")
                    rewardedAd = ad
                    cacheAd(AdType.REWARD_VIDEO, ad)
                    val simulatedECPM = generateSimulatedPrice()
                    log("${TAG}: Rewarded ad (simulated eCPM: $$simulatedECPM)")
                    callback?.onAdLoadedWithPrice(simulatedECPM)

                    ad.setAdInteractionListener(object : RewardAdInteractionListener {
                        override fun onAdError(adError: AdError) {
                            log("$TAG: Rewarded ad error: ${adError.code}")
                            rewardedAd = null
                            removeCachedAd(AdType.REWARD_VIDEO)
                            callback?.onAdFail(adError.code, adError.message ?: "Ad error")
                        }

                        override fun onAdImpression() {
                            log("$TAG: Rewarded ad showed")
                            callback?.onAdShow()
                        }

                        override fun onAdClicked() {
                            log("$TAG: Rewarded ad clicked")
                            callback?.onAdClick()
                        }

                        override fun onAdOpened() {
                            log("$TAG: Rewarded ad opened")
                        }

                        override fun onAdClosed() {
                            log("$TAG: Rewarded ad dismissed")
                            rewardedAd = null
                            removeCachedAd(AdType.REWARD_VIDEO)
                            callback?.onAdDismiss()

                            preloadAdOnDismiss(AdType.REWARD_VIDEO)
                        }

                        override fun onAdRewarded() {
                            log("$TAG: User earned reward from Bigo")
                            callback?.onAdRewarded(1, "coins")
                        }

                    })
                }
            })
            .build()

        loader.loadAd(request)
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
            val request = BannerAdRequest.Builder()
                .withSlotId(adId)
                .withAdSizes(AdSize.LARGE_BANNER)
                .build()

            val loader = BannerAdLoader.Builder()
                .withAdLoadListener(object : AdLoadListener<BannerAd> {
                    override fun onError(adError: AdError) {
                        log("$TAG: Banner ad failed to load: ${adError.code}")
                        callback?.onAdFail(adError.code, adError.message ?: "Load failed")
                    }

                    override fun onAdLoaded(ad: BannerAd) {
                        log("$TAG: Banner ad loaded successfully")
                        bannerAd = ad
                        cacheAd(AdType.BANNER, ad)
                        callback?.onAdLoaded()

                        ad.setAdInteractionListener(object : AdInteractionListener {
                            override fun onAdError(adError: AdError) {
                                log("$TAG: Banner ad error: ${adError.code}")
                                bannerAd = null
                                removeCachedAd(AdType.BANNER)
                                callback?.onAdFail(adError.code, adError.message ?: "Ad error")
                            }

                            override fun onAdImpression() {
                                log("$TAG: Banner ad showed")
                                callback?.onAdShow()
                            }

                            override fun onAdClicked() {
                                log("$TAG: Banner ad clicked")
                                callback?.onAdClick()
                            }

                            override fun onAdOpened() {
                                log("$TAG: Banner ad opened")
                            }

                            override fun onAdClosed() {
                                log("$TAG: Banner ad closed")
                            }
                        })
                    }
                })
                .build()

            loader.loadAd(request)
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
            container.addView(ad.adView())
            log("$TAG: Banner ad showed in container")
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
