package app.allever.android.lib.ad.provider.applovin

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class AppLovinAdProvider : BaseAdProvider() {

    companion object {
        private const val TAG = "AppLovinAdProvider"
        const val PROVIDER_NAME = "AppLovin"
    }

    override fun getProviderType(): String = PROVIDER_NAME

    private var interstitialAd: MaxInterstitialAd? = null
    private var rewardedAd: MaxRewardedAd? = null

    private var splashAd: MaxAppOpenAd? = null

    private var bannerAd: MaxAdView? = null



    override fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)?) {
        val safeCallback = WeakReference(callback)

        initInternal(realInit = {
            CoroutineScope(Dispatchers.IO).launch {
                val initConfig = AppLovinSdkInitializationConfiguration.builder(config.appId)
                    .setMediationProvider(AppLovinMediationProvider.MAX)
                    .build()

                AppLovinSdk.getInstance(context).apply {
                    settings.isCreativeDebuggerEnabled = true
                }

                AppLovinSdk.getInstance(context).initialize(initConfig) { sdkConfig ->
                    safeCallback.get()?.invoke()
                    finishInit(null)
                }
            }
        }, callback = null)
    }

    override fun loadSplashAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading splash ad: $adId")
        splashAd = MaxAppOpenAd(adId)
        splashAd?.setListener(object : MaxAdListener {
            val callbackRef = WeakReference(callback)
            override fun onAdLoaded(p0: MaxAd) {
                handleOnAdLoaded(AdType.SPLASH, splashAd!!, callbackRef.get())
            }

            override fun onAdDisplayed(p0: MaxAd) {
                handleOnAdShow(AdType.SPLASH, callbackRef.get())
            }

            override fun onAdHidden(p0: MaxAd) {
                splashAd = null
                handleAdDismissed(AdType.SPLASH, callbackRef.get())
            }

            override fun onAdClicked(p0: MaxAd) {
                handleOnAdClick(AdType.SPLASH, callbackRef.get())
            }

            override fun onAdLoadFailed(p0: String, error: MaxError) {
                handleOnAdLoadFail(AdType.SPLASH, error.code, error.message, callbackRef.get())
            }

            override fun onAdDisplayFailed(
                ad: MaxAd,
                error: MaxError
            ) {
                handleOnAdShowFail(AdType.SPLASH, error.code, error.message, callbackRef.get())
            }

        })
        splashAd?.loadAd()
    }

    override fun showSplashAd(activity: Activity, callback: IAdCallback?) {
        showSplashAdInternal(splashAd, callback) {
            splashAd?.showAd()
        }
    }

    override fun loadInterstitialAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading interstitial ad: $adId")

        interstitialAd = MaxInterstitialAd(adId)
        interstitialAd?.setListener(object : MaxAdListener {
            val callbackRef = WeakReference(callback)
            override fun onAdLoaded(ad: MaxAd) {
                handleOnAdLoaded(AdType.INTERSTITIAL, interstitialAd!!, callbackRef.get())
            }

            override fun onAdDisplayed(ad: MaxAd) {
                handleOnAdShow(AdType.INTERSTITIAL, callbackRef.get())
            }

            override fun onAdHidden(ad: MaxAd) {
                interstitialAd = null
                handleAdDismissed(AdType.INTERSTITIAL, callbackRef.get())
            }

            override fun onAdClicked(ad: MaxAd) {
                handleOnAdClick(AdType.INTERSTITIAL, callbackRef.get())
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                handleOnAdLoadFail(AdType.INTERSTITIAL, error.code, error.message, callbackRef.get())
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                handleOnAdShowFail(AdType.INTERSTITIAL, error.code, error.message, callbackRef.get())
            }
        })

        interstitialAd?.loadAd()
    }

    override fun showInterstitialAd(activity: Activity, callback: IAdCallback?) {
        showInterstitialAdInternal(interstitialAd, callback) {
            interstitialAd?.showAd(activity)
        }
    }

    override fun loadRewardedAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        log("$TAG: Loading rewarded ad: $adId")

        rewardedAd = MaxRewardedAd.getInstance(adId)
        rewardedAd?.setListener(object : MaxRewardedAdListener {
            val callbackRef = WeakReference(callback)
            override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                handleOnAdRewarded(AdType.REWARD_VIDEO, reward.amount, reward.label, callbackRef.get())
            }

            override fun onAdLoaded(ad: MaxAd) {
                handleOnAdLoaded(AdType.REWARD_VIDEO, rewardedAd!!, callbackRef.get())
            }

            override fun onAdDisplayed(ad: MaxAd) {
                handleOnAdShow(AdType.REWARD_VIDEO, callbackRef.get())
            }

            override fun onAdHidden(ad: MaxAd) {
                rewardedAd = null
                handleAdDismissed(AdType.REWARD_VIDEO, callbackRef.get())
            }

            override fun onAdClicked(ad: MaxAd) {
                handleOnAdClick(AdType.REWARD_VIDEO, callbackRef.get())
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                handleOnAdLoadFail(AdType.REWARD_VIDEO, error.code, error.message, callbackRef.get())
            }

            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                handleOnAdShowFail(AdType.REWARD_VIDEO, error.code, error.message, callbackRef.get())
            }
        })

        rewardedAd?.loadAd()
    }

    override fun showRewardedAd(activity: Activity, callback: IAdCallback?) {
        showRewardedAdInternal(rewardedAd, callback) {
            rewardedAd?.showAd(activity)
        }
    }

    override fun loadBannerAd(context: Context, adId: String, callback: IAdCallback?) {
        log("$TAG: Loading banner ad: $adId")
        bannerAd = MaxAdView(adId)
        bannerAd?.setListener(object : MaxAdViewAdListener {
            val callbackRef = WeakReference(callback)
            override fun onAdExpanded(ad: MaxAd) {
                handleOnAdShow(AdType.BANNER, callbackRef.get())
            }

            override fun onAdCollapsed(ad: MaxAd) {
            }

            override fun onAdLoaded(ad: MaxAd) {
                handleOnAdLoaded(AdType.BANNER, bannerAd!!, callbackRef.get())
            }

            override fun onAdDisplayed(ad: MaxAd) {
                handleOnAdShow(AdType.BANNER, callbackRef.get())
            }

            override fun onAdHidden(ad: MaxAd) {
                bannerAd = null
                handleAdDismissed(AdType.BANNER, callbackRef.get())
            }

            override fun onAdClicked(ad: MaxAd) {
                handleOnAdClick(AdType.BANNER, callbackRef.get())
            }

            override fun onAdLoadFailed(p0: String, error: MaxError) {
                handleOnAdLoadFail(AdType.BANNER, error.code, error.message, callbackRef.get())
            }

            override fun onAdDisplayFailed(
                ad: MaxAd,
                error: MaxError
            ) {
                handleOnAdShowFail(AdType.BANNER, error.code, error.message, callbackRef.get())
            }
        })
        bannerAd?.loadAd()
    }

    override fun showBannerAd(container: ViewGroup?, callback: IAdCallback?) {
        showBannerInternal(bannerAd, container, callback)
    }

    override fun onDestroy() {
        interstitialAd?.setListener(null)
        rewardedAd?.setListener(null)
        splashAd?.setListener(null)
        bannerAd?.setListener(null)
        interstitialAd = null
        rewardedAd = null
        bannerAd = null
        splashAd = null
    }


}
