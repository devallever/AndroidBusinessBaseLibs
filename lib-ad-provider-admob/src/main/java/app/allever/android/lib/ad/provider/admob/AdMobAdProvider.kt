package app.allever.android.lib.ad.provider.admob

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
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

    override fun init(config: Map<String, Any>, callback: (() -> Unit)?) {
        Log.d(TAG, "Initializing AdMob with config: $config")
        
        val context = config["context"] as? Context ?: run {
            Log.e(TAG, "Context not found in config")
            return
        }

        if (isInit()) {
            Log.w(TAG, "AdMob already initialized")
            callback?.invoke()
            return
        }

        MobileAds.initialize(context) {
            isInitialized = true
            Log.d(TAG, "AdMob initialized successfully")
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
            AdType.INTERSTITIAL -> loadInterstitialAd(activity, adId, callback)
            AdType.REWARD_VIDEO -> loadRewardedAd(activity, adId, callback)
            AdType.BANNER -> loadBannerAd(activity, adId, callback)
            else -> {
                Log.w(TAG, "${adType.name} not supported yet for AdMob")
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
                Log.w(TAG, "${adType.name} show not implemented for AdMob")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        interstitialAd = null
        rewardedAd = null
    }

    private fun loadInterstitialAd(
        activity: Activity,
        adId: String,
        callback: IAdCallback?
    ) {
        Log.d(TAG, "Loading interstitial ad: $adId")
        
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(activity, adId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.w(TAG, "Interstitial ad failed to load: ${adError.message}")
                callback?.onAdFail(adError.code, adError.message)
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d(TAG, "Interstitial ad loaded successfully")
                interstitialAd = ad
                cacheAd(AdType.INTERSTITIAL, ad)
                callback?.onAdLoaded()
                
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad dismissed")
                        interstitialAd = null
                        removeCachedAd(AdType.INTERSTITIAL)
                        callback?.onAdDismiss()
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Interstitial ad showed")
                        callback?.onAdShow()
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Interstitial ad clicked")
                        callback?.onAdClick()
                    }
                }
            }
        })
    }

    private fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        interstitialAd?.show(activity) ?: run {
            Log.w(TAG, "Interstitial ad not ready")
            callback?.onAdFail(-1, "Interstitial ad not loaded")
        }
    }

    private fun loadRewardedAd(
        activity: Activity,
        adId: String,
        callback: IAdCallback?
    ) {
        Log.d(TAG, "Loading rewarded ad: $adId")

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(activity, adId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.w(TAG, "Rewarded ad failed to load: ${adError.message}")
                callback?.onAdFail(adError.code, adError.message)
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded ad loaded successfully")
                rewardedAd = ad
                cacheAd(AdType.REWARD_VIDEO, ad)
                callback?.onAdLoaded()

                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad dismissed")
                        rewardedAd = null
                        removeCachedAd(AdType.REWARD_VIDEO)
                        callback?.onAdDismiss()
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.d(TAG, "Rewarded ad showed")
                        callback?.onAdShow()
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Rewarded ad clicked")
                        callback?.onAdClick()
                    }
                }
            }
        })
    }

    private fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        rewardedAd?.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            callback?.onAdRewarded(rewardItem.amount, rewardItem.type)
        } ?: run {
            Log.w(TAG, "Rewarded ad not ready")
            callback?.onAdFail(-1, "Rewarded ad not loaded")
        }
    }

    private fun loadBannerAd(
        activity: Activity,
        adId: String,
        callback: IAdCallback?
    ) {
        Log.d(TAG, "Loading banner ad: $adId")

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
                    Log.d(TAG, "Banner ad loaded successfully")
                    cacheAd(AdType.BANNER, adView)
                    callback?.onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Banner ad failed to load: ${adError.message}")
                    callback?.onAdFail(adError.code, adError.message)
                }

                override fun onAdOpened() {
                    callback?.onAdClick()
                }
            }

            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading banner ad", e)
            callback?.onAdFail(-1, e.message ?: "Unknown error")
        }
    }

    private fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        val adView = getCachedAd(AdType.BANNER) as? AdView
        
        if (adView == null || container == null) {
            Log.w(TAG, "Banner ad or container not ready")
            callback?.onAdFail(-1, "Banner ad not ready")
            return
        }

        try {
            container.removeAllViews()
            container.addView(adView)
            Log.d(TAG, "Banner ad showed")
            callback?.onAdShow()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing banner ad", e)
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
}
