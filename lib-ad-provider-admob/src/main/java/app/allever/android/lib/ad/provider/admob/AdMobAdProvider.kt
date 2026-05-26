package app.allever.android.lib.ad.provider.admob

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import com.google.android.gms.ads.*
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

    override fun getProviderType(): String = PROVIDER_NAME

    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        log("$TAG: Initializing AdMob with appId: ${config.appId}")

        if (isInit()) {
            log("$TAG: AdMob already initialized")
            callback?.invoke()
            return
        }

        MobileAds.initialize(context) {
            isInitialized = true
            log("$TAG: AdMob initialized successfully")
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
            AdType.INTERSTITIAL -> loadInterstitialAd(context, adId, callback)
            AdType.REWARD_VIDEO -> loadRewardedAd(context, adId, callback)
            AdType.BANNER -> loadBannerAd(context, adId, callback)
            else -> {
                log("$TAG: ${adType.name} not supported yet for AdMob")
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
                log("$TAG: ${adType.name} show not implemented for AdMob")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAd = null
        rewardedAd = null
    }

    private fun loadInterstitialAd(
        activity: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading interstitial ad: $adId")

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(activity, adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                log("$TAG: Interstitial ad failed to load: ${adError.message}")
                callback?.onAdFail(adError.code, adError.message)
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                log("$TAG: Interstitial ad loaded successfully")
                interstitialAd = ad
                cacheAd(AdType.INTERSTITIAL, ad)
                
                val simulatedECPM = generateSimulatedPrice()
                log("$TAG: Interstitial ad (simulated eCPM: $$simulatedECPM)")
                callback?.onAdLoadedWithPrice(simulatedECPM)

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        log("$TAG: Interstitial ad dismissed")
                        interstitialAd = null
                        removeCachedAd(AdType.INTERSTITIAL)
                        callback?.onAdDismiss()

                        preloadAdOnDismiss(AdType.INTERSTITIAL)
                    }

                    override fun onAdShowedFullScreenContent() {
                        log("$TAG: Interstitial ad showed")
                        callback?.onAdShow()
                    }

                    override fun onAdClicked() {
                        log("$TAG: Interstitial ad clicked")
                        callback?.onAdClick()
                    }
                }
            }
        })
    }

    private fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        interstitialAd?.show(activity) ?: run {
            log("$TAG: Interstitial ad not ready")
            callback?.onAdFail(-1, "Interstitial ad not loaded")
        }
    }

    private fun loadRewardedAd(
        activity: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading rewarded ad: $adId")

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(activity, adId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                log("$TAG: Rewarded ad failed to load: ${adError.message}")
                callback?.onAdFail(adError.code, adError.message)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                log("$TAG: Rewarded ad loaded successfully")
                rewardedAd = ad
                cacheAd(AdType.REWARD_VIDEO, ad)
                
                val simulatedECPM = generateSimulatedPrice()
                log("$TAG: Rewarded ad (simulated eCPM: $$simulatedECPM)")
                callback?.onAdLoadedWithPrice(simulatedECPM)

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        log("$TAG: Rewarded ad dismissed")
                        rewardedAd = null
                        removeCachedAd(AdType.REWARD_VIDEO)
                        callback?.onAdDismiss()

                        preloadAdOnDismiss(AdType.REWARD_VIDEO)
                    }

                    override fun onAdShowedFullScreenContent() {
                        log("$TAG: Rewarded ad showed")
                        callback?.onAdShow()
                    }

                    override fun onAdClicked() {
                        log("$TAG: Rewarded ad clicked")
                        callback?.onAdClick()
                    }
                }
            }
        })
    }

    private fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        rewardedAd?.show(activity) { rewardItem ->
            log("$TAG: User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            callback?.onAdRewarded(rewardItem.amount, rewardItem.type)
        } ?: run {
            log("$TAG: Rewarded ad not ready")
            callback?.onAdFail(-1, "Rewarded ad not loaded")
        }
    }

    private fun loadBannerAd(
        activity: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading banner ad: $adId")

        try {
            val adView = AdView(activity)
            adView.adUnitId = adId

            val displayMetrics = activity.resources.displayMetrics
            val adWidth = displayMetrics.widthPixels
            val autoAdWidth = getScreenWidth(activity)
            adView.setAdSize(
                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    activity,
                    autoAdWidth
                )
            )

            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    log("$TAG: Banner ad loaded successfully")
                    cacheAd(AdType.BANNER, adView)
                    
                    val simulatedECPM = generateSimulatedPrice()
                    log("$TAG: Banner ad (simulated eCPM: $$simulatedECPM)")
                    callback?.onAdLoadedWithPrice(simulatedECPM)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    log("$TAG: Banner ad failed to load: ${adError.message}")
                    callback?.onAdFail(adError.code, adError.message)
                }

                override fun onAdOpened() {
                    callback?.onAdClick()
                }
            }

            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            logE("$TAG: Error loading banner ad", e.message)
            callback?.onAdFail(-1, e.message ?: "Unknown error")
        }
    }

    private fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        val adView = getCachedAd(AdType.BANNER) as? AdView

        if (adView == null || container == null) {
            log("$TAG: Banner ad or container not ready")
            callback?.onAdFail(-1, "Banner ad not ready")
            return
        }

        try {
            container.removeAllViews()
            container.addView(adView)
            log("$TAG: Banner ad showed")
            callback?.onAdShow()
        } catch (e: Exception) {
            logE("$TAG: Error showing banner ad", e.message)
            callback?.onAdFail(-1, e.message ?: "Unknown error")
        }
    }

    private fun getScreenWidth(context: Context): Int {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)
        val density = outMetrics.density
        return (outMetrics.widthPixels / density).toInt()
    }

    private fun generateSimulatedPrice(): Double {
        val minPrice = 1.0
        val maxPrice = 5.0
        return minPrice + (Math.random() * (maxPrice - minPrice))
    }
}
