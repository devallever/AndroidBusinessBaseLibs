package app.allever.android.sample.ad.applovin

import android.app.Activity
import android.app.Application
import android.os.Bundle
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.MaxSegment
import com.applovin.mediation.MaxSegmentCollection
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * https://support.axon.ai/zh/max/android/overview/integration
 */
object AppLovinManager {

    private var isInit = false
    private var mIAdConfig: IAdConfig = TestAdConfig()

    //书歌Test: tRLgvBNKtFZuwWs2XqXJt_3X9yyl7oCA-1N-LBASDS9GDGrDaMunCbzHMWK63bVl_NmwB5g0k5sCUD6BAEgmda
//    private const val SDK_KEY = "tRLgvBNKtFZuwWs2XqXJt_3X9yyl7oCA-1N-LBASDS9GDGrDaMunCbzHMWK63bVl_NmwB5g0k5sCUD6BAEgmda"
    //AppLovin Test
    private const val SDK_KEY =
        "05TMDQ5tZabpXQ45_UTbmEGNUtVAzSTzT6KmWQc5_CuWdzccS4DCITZoL3yIWUG3bbq60QC_d4WF28tUC4gVTF"

    private lateinit var mContext: Application
    private var mInterAdCache: MaxInterstitialAd? = null
    private var mRewardAdCache: MaxRewardedAd? = null
    private var mInterAdCacheTime = 0L
    private var mRewardAdCacheTime = 0L
    private const val CACHE_TIME_OUT = 45 * 60 * 1000L

    private class AdjustLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        }

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

    fun init(iAdConfig: IAdConfig, block: () -> Unit) {
        mIAdConfig = iAdConfig
        mContext = App.app
        if (isInit) {
            log("AppLovin SDK already initialized")
            block()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Create the initialization configuration
            val initConfig = AppLovinSdkInitializationConfiguration.builder(SDK_KEY)
                .setMediationProvider(AppLovinMediationProvider.MAX)
                .setSegmentCollection(
                    MaxSegmentCollection.builder()
                        .addSegment(MaxSegment(849, listOf(1, 3)))
                        .build()
                )
                .build()

            // Configure the SDK settings if needed before or after SDK initialization.
//            val settings = AppLovinSdk.getInstance(App.context).settings
//            settings.userIdentifier = "«user-ID»"
//            settings.setExtraParameter("uid2_token", "«token-value»")
//            settings.termsAndPrivacyPolicyFlowSettings.apply {
//                isEnabled = true
//                privacyPolicyUri = Uri.parse("«https://your-company-name.com/privacy-policy»")
//                termsOfServiceUri = Uri.parse("«https://your-company-name.com/terms-of-service»")
//            }
            AppLovinSdk.getInstance(App.context).apply {
                settings.isCreativeDebuggerEnabled = true
            }

            // Initialize the SDK with the configuration
            AppLovinSdk.getInstance(App.context).initialize(initConfig) { sdkConfig ->
                // Start loading ads
                log("AppLovin SDK initialized")

                // Initialize Adjust SDK
                val config =
                    AdjustConfig(App.context, "{YourAppToken}", AdjustConfig.ENVIRONMENT_SANDBOX)
                Adjust.initSdk(config)

                App.app.registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())

                block()

                isInit = true
            }
        }
    }

    fun justLoadInter(adCallback: AdCallback? = null) {
        val cacheTime = System.currentTimeMillis() - mInterAdCacheTime
        if (mInterAdCache != null && cacheTime < CACHE_TIME_OUT) {
            log("使用InterAdCache")
            adCallback?.onAdLoaded()
            return
        }

        mInterAdCache = null

        val interstitialAd =
            MaxInterstitialAd(mIAdConfig.getAdId(IAdConfig.Companion.INTER_AD), mContext)
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                log("interAd: 加载成功")
                mInterAdCache = interstitialAd
                mInterAdCacheTime = System.currentTimeMillis()
                log("interAd: 缓存成功")
            }

            override fun onAdDisplayed(p0: MaxAd) {
                log("interAd: 显示")
                adCallback?.onAdShow()
            }

            override fun onAdHidden(p0: MaxAd) {
                log("interAd: 隐藏")
                mInterAdCache = null
                adCallback?.onAdDismiss()
                justLoadInter()
            }

            override fun onAdClicked(p0: MaxAd) {
                log("interAd: 点击")
                adCallback?.onAdClick()
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                logE("interAd: 加载失败 -> ${p1.code} -> ${p1.message}")
            }

            override fun onAdDisplayFailed(
                p0: MaxAd,
                p1: MaxError
            ) {
                logE("interAd: 显示失败 -> ${p1.code} -> ${p1.message}")
                mInterAdCache = null
                adCallback?.onAdFailLoad()
                justLoadInter()
            }

        })

        // Load the first ad
        interstitialAd.loadAd()
    }

    fun justLoadReward(adCallback: AdCallback? = null) {
        val cacheTime = System.currentTimeMillis() - mRewardAdCacheTime
        if (mRewardAdCache != null && cacheTime < CACHE_TIME_OUT) {
            log("使用RewardAdCache")
            adCallback?.onAdLoaded()
            return
        }

        mRewardAdCache = null

        val rewardedAd = MaxRewardedAd.getInstance(mIAdConfig.getAdId(IAdConfig.REWARD_AD))
        rewardedAd.setListener(object : MaxRewardedAdListener {
            override fun onUserRewarded(
                p0: MaxAd,
                p1: MaxReward
            ) {
                log("RewardAd: 获取奖励")
                adCallback?.onRewarded()
            }

            override fun onAdLoaded(maxAd: MaxAd) {
                log("rewardAd: 加载成功")
                mRewardAdCache = rewardedAd
                mRewardAdCacheTime = System.currentTimeMillis()
                log("rewardAd: 缓存成功")
                adCallback?.onAdLoaded()
            }

            override fun onAdDisplayed(p0: MaxAd) {
                log("RewardAd: 显示")
                adCallback?.onAdShow()
            }

            override fun onAdHidden(p0: MaxAd) {
                log("RewardAd: 关闭")
                mRewardAdCache = null
                adCallback?.onAdDismiss()
                justLoadReward(adCallback)
            }

            override fun onAdClicked(p0: MaxAd) {
            }

            override fun onAdLoadFailed(p0: String, adError: MaxError) {
                logE("rewardAd: 加载失败 -> ${adError.code}: ${adError.message}")
                adCallback?.onAdFailLoad()
            }

            override fun onAdDisplayFailed(
                p0: MaxAd,
                adError: MaxError
            ) {
                log("RewardAd: 显示失败")
                mRewardAdCache = null
                adCallback?.onAdFailLoad()
                justLoadReward(adCallback)
            }

        })

        rewardedAd.loadAd()

    }

    fun showInter(activity: Activity, adCallback: AdCallback? = null) {

        if (mInterAdCache == null) {
            justLoadInter()
            log("InterAdCache: 缓存中无广告, 加载广告")
            adCallback?.onAdFailLoad()
            return
        }

        val cacheTime = System.currentTimeMillis() - mInterAdCacheTime
        if (cacheTime > CACHE_TIME_OUT) {
            log("InterAdCache: 缓存已过期，加载广告")
            mInterAdCache = null
            justLoadInter()
            adCallback?.onAdFailLoad()
            return
        }

        log("使用InterAdCache")
        mInterAdCache?.showAd(activity)
    }

    fun showReward(activity: Activity, adCallback: AdCallback? = null) {
        if (mRewardAdCache == null) {
            adCallback?.onAdFailLoad()
            justLoadReward(adCallback)
            return
        }

        val cacheTime = System.currentTimeMillis() - mRewardAdCacheTime
        if (cacheTime > CACHE_TIME_OUT) {
            adCallback?.onAdFailLoad()
            justLoadReward(adCallback)
            return
        }

        log("使用RewardAdCache")
        mRewardAdCache?.showAd(activity)
    }
}