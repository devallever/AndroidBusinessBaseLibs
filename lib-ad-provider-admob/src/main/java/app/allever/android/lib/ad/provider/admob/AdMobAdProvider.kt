package app.allever.android.lib.ad.provider.admob

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobAdProvider : BaseAdProvider() {

    companion object {
        private const val TAG = "AdMobAdProvider"
        const val PROVIDER_NAME = "ADMOB"
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var splashAd: AppOpenAd? = null

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        initInternal(realInit = {
            MobileAds.initialize(context) {
                finishInit(callback)
            }
        },callback)
    }

    override fun onDestroy() {
        interstitialAd = null
        rewardedAd = null
        splashAd = null
    }

    override fun loadSplashAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {

        log("$TAG: Loading splash ad: $adId")

        AppOpenAd.load(
            context,
            adId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    splashAd = ad
                    handleOnAdLoaded(AdType.SPLASH, ad,  callback)

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            splashAd = null
                            handleAdDismissed(AdType.SPLASH, callback)
                        }

                        override fun onAdShowedFullScreenContent() {
                            handleOnAdShow(AdType.SPLASH, callback)
                        }

                        override fun onAdClicked() {
                            handleOnAdClick(AdType.SPLASH, callback)
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    handleOnAdLoadFail(AdType.SPLASH, adError.code, adError.message, callback)
                }
            }
        )
    }

    override fun showSplashAd(activity: Activity, callback: IAdCallback?) {
        showSplashAdInternal(splashAd, callback) {
            splashAd?.show( activity)
        }
    }

    override fun loadInterstitialAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading interstitial ad: $adId")

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                handleOnAdLoadFail(AdType.INTERSTITIAL, adError.code, adError.message, callback)
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                handleOnAdLoaded(AdType.INTERSTITIAL, ad, callback)

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        handleAdDismissed(AdType.INTERSTITIAL, callback)
                    }

                    override fun onAdShowedFullScreenContent() {
                        handleOnAdShow(AdType.INTERSTITIAL, callback)
                    }

                    override fun onAdClicked() {
                        handleOnAdClick(AdType.INTERSTITIAL, callback)
                    }
                }
            }
        })
    }

    override fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        showInterstitialAdInternal(interstitialAd, callback) {
            interstitialAd?.show(activity)
        }
    }

    override fun loadRewardedAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading rewarded ad: $adId")

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, adId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                handleOnAdLoadFail(AdType.REWARD_VIDEO, adError.code, adError.message, callback)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                handleOnAdLoaded(AdType.REWARD_VIDEO, ad, callback)

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        rewardedAd = null
                        handleAdDismissed(AdType.REWARD_VIDEO, callback)
                    }

                    override fun onAdShowedFullScreenContent() {
                        handleOnAdShow(AdType.REWARD_VIDEO, callback)
                    }

                    override fun onAdClicked() {
                        handleOnAdClick(AdType.REWARD_VIDEO, callback)
                    }
                }
            }
        })
    }

    override fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        showRewardedAdInternal(rewardedAd, callback) {
            rewardedAd?.show(activity) {rewardItem ->
                handleOnAdRewarded(AdType.REWARD_VIDEO, rewardItem.amount, rewardItem.type, callback)
            }
        }
    }

    override fun loadBannerAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading banner ad: $adId")

        try {
            val adView = AdView(context)
            adView.adUnitId = adId

            val autoAdWidth = getScreenWidth(context)
            adView.setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    context,
                    autoAdWidth
                )
            )

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    handleOnAdLoaded(AdType.BANNER, adView, callback)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    handleOnAdLoadFail(AdType.BANNER, adError.code, adError.message, callback)
                }

                override fun onAdOpened() {
                    handleOnAdShow(AdType.BANNER, callback)
                }
            }

            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            handleOnAdLoadFail(AdType.BANNER, -1, e.message?: "Unknown error", callback)
        }
    }

    override fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        val adView = getCachedAd(AdType.BANNER) as? View
        showBannerInternal(adView, container,  callback)
    }

    private fun getScreenWidth(context: Context): Int {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)
        val density = outMetrics.density
        return (outMetrics.widthPixels / density).toInt()
    }

}
