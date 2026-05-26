package app.allever.android.lib.ad.provider.bigo

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
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

    override fun init(config: Map<String, Any>, callback: (() -> Unit)?) {
        Log.d(TAG, "Initializing Bigo with config: $config")

        val context = config["context"] as? Context ?: run {
            Log.e(TAG, "Context not found in config")
            return
        }

        if (isInit()) {
            Log.w(TAG, "Bigo already initialized")
            callback?.invoke()
            return
        }

        val appId = config["appId"] as? String ?: ""
        
        val adConfig = AdConfig.Builder()
            .setAppId(appId)
            .setDebug(true)
            .build()

        BigoAdSdk.initialize(context, adConfig) {
            isInitialized = true
            Log.d(TAG, "Bigo initialized successfully")
            callback?.invoke()
        }
    }

    override fun doLoadAd(
        activity: Activity,
        adType: AdType,
        adId: String,
        callback: IAdCallback?
    ) {
        when (adType) {
            AdType.INTERSTITIAL -> loadInterstitialAd(adId, callback)
            AdType.REWARD_VIDEO -> loadRewardedAd(adId, callback)
            AdType.BANNER -> loadBannerAd(activity, adId, callback)
            else -> {
                Log.w(TAG, "${adType.name} not supported yet for Bigo")
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
                Log.w(TAG, "${adType.name} show not implemented for Bigo")
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
        Log.d(TAG, "Loading interstitial ad: $adId")

        val request = InterstitialAdRequest.Builder()
            .withSlotId(adId)
            .build()

        val loader = InterstitialAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<InterstitialAd> {
                override fun onError(adError: AdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${adError.code}")
                    callback?.onAdFail(adError.code, adError.message ?: "Load failed")
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    cacheAd(AdType.INTERSTITIAL, ad)
                    callback?.onAdLoaded()

                    ad.setAdInteractionListener(object : AdInteractionListener {
                        override fun onAdError(adError: AdError) {
                            Log.w(TAG, "Interstitial ad error: ${adError.code}")
                            interstitialAd = null
                            removeCachedAd(AdType.INTERSTITIAL)
                            callback?.onAdFail(adError.code, adError.message ?: "Ad error")
                        }

                        override fun onAdImpression() {
                            Log.d(TAG, "Interstitial ad showed")
                            callback?.onAdShow()
                        }

                        override fun onAdClicked() {
                            Log.d(TAG, "Interstitial ad clicked")
                            callback?.onAdClick()
                        }

                        override fun onAdOpened() {
                            Log.d(TAG, "Interstitial ad opened")
                        }

                        override fun onAdClosed() {
                            Log.d(TAG, "Interstitial ad dismissed")
                            interstitialAd = null
                            removeCachedAd(AdType.INTERSTITIAL)
                            callback?.onAdDismiss()
                        }
                    })
                }
            })
            .build()

        loader.loadAd(request)
    }

    private fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        interstitialAd?.show(activity) ?: run {
            Log.w(TAG, "Interstitial ad not ready")
            callback?.onAdFail(-1, "Interstitial ad not loaded")
        }
    }

    private fun loadRewardedAd(adId: String, callback: IAdCallback?) {
        Log.d(TAG, "Loading rewarded ad: $adId")

        val request = RewardVideoAdRequest.Builder()
            .withSlotId(adId)
            .build()

        val loader = RewardVideoAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<RewardVideoAd> {
                override fun onError(adError: AdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${adError.code}")
                    callback?.onAdFail(adError.code, adError.message ?: "Load failed")
                }

                override fun onAdLoaded(ad: RewardVideoAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    cacheAd(AdType.REWARD_VIDEO, ad)
                    callback?.onAdLoaded()

                    ad.setAdInteractionListener(object : RewardAdInteractionListener {
                        override fun onAdError(adError: AdError) {
                            Log.w(TAG, "Rewarded ad error: ${adError.code}")
                            rewardedAd = null
                            removeCachedAd(AdType.REWARD_VIDEO)
                            callback?.onAdFail(adError.code, adError.message ?: "Ad error")
                        }

                        override fun onAdImpression() {
                            Log.d(TAG, "Rewarded ad showed")
                            callback?.onAdShow()
                        }

                        override fun onAdClicked() {
                            Log.d(TAG, "Rewarded ad clicked")
                            callback?.onAdClick()
                        }

                        override fun onAdOpened() {
                            Log.d(TAG, "Rewarded ad opened")
                        }

                        override fun onAdClosed() {
                            Log.d(TAG, "Rewarded ad dismissed")
                            rewardedAd = null
                            removeCachedAd(AdType.REWARD_VIDEO)
                            callback?.onAdDismiss()
                        }

                        override fun onAdRewarded() {
                            Log.d(TAG, "User earned reward from Bigo")
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
            Log.w(TAG, "Rewarded ad not ready")
            callback?.onAdFail(-1, "Rewarded ad not loaded")
        }
    }

    private fun loadBannerAd(activity: Activity, adId: String, callback: IAdCallback?) {
        Log.d(TAG, "Loading banner ad: $adId")

        try {
            val request = BannerAdRequest.Builder()
                .withSlotId(adId)
                .withAdSizes(AdSize.LARGE_BANNER)
                .build()

            val loader = BannerAdLoader.Builder()
                .withAdLoadListener(object : AdLoadListener<BannerAd> {
                    override fun onError(adError: AdError) {
                        Log.w(TAG, "Banner ad failed to load: ${adError.code}")
                        callback?.onAdFail(adError.code, adError.message ?: "Load failed")
                    }

                    override fun onAdLoaded(ad: BannerAd) {
                        Log.d(TAG, "Banner ad loaded successfully")
                        bannerAd = ad
                        cacheAd(AdType.BANNER, ad)
                        callback?.onAdLoaded()

                        ad.setAdInteractionListener(object : AdInteractionListener {
                            override fun onAdError(adError: AdError) {
                                Log.w(TAG, "Banner ad error: ${adError.code}")
                                bannerAd = null
                                removeCachedAd(AdType.BANNER)
                                callback?.onAdFail(adError.code, adError.message ?: "Ad error")
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "Banner ad showed")
                                callback?.onAdShow()
                            }

                            override fun onAdClicked() {
                                Log.d(TAG, "Banner ad clicked")
                                callback?.onAdClick()
                            }

                            override fun onAdOpened() {
                                Log.d(TAG, "Banner ad opened")
                            }

                            override fun onAdClosed() {
                                Log.d(TAG, "Banner ad closed")
                            }
                        })
                    }
                })
                .build()

            loader.loadAd(request)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banner ad", e)
            callback?.onAdFail(-1, e.message ?: "Unknown error")
        }
    }

    private fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        val ad = bannerAd
        
        if (ad == null || container == null) {
            Log.w(TAG, "Banner ad or container not ready")
            callback?.onAdFail(-1, "Banner ad not ready")
            return
        }

        try {
            container.removeAllViews()
            container.addView(ad.adView())
            Log.d(TAG, "Banner ad showed in container")
            callback?.onAdShow()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing banner ad", e)
            callback?.onAdFail(-1, e.message ?: "Unknown error")
        }
    }
}
