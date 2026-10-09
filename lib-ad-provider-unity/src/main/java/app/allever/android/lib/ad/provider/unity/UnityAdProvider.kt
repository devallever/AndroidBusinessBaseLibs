package app.allever.android.lib.ad.provider.unity

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.UnityAds
import java.lang.ref.WeakReference

class UnityAdProvider: BaseAdProvider() {

    companion object {
        private const val TAG = "UnityAdProvider"
        const val PROVIDER_NAME = "UnityAds"
    }

    private var interstitialAd: String? = null //也就是placementId
//    private var rewardedAd: RewardedAd? = null

    override fun onDestroy() {

    }

    override fun getProviderType(): String {
        return PROVIDER_NAME
    }

    override fun init(
        context: Context,
        config: AdProviderConfig,
        callback: (() -> Unit)?
    ) {
        val safeCallback = WeakReference(callback)
        initInternal(realInit = {
            UnityAds.initialize(context, config.appId, App.DEBUG, object :
                IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    safeCallback.get()?.invoke()
                    finishInit(callback)
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError, message: String) {
                    log(TAG, "Unity Ads initialization failed: $error - $message")
                }
            });
        }, callback = null)
    }

    override fun loadSplashAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        //不支持
    }

    override fun loadInterstitialAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
        UnityAds.load(adId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                interstitialAd = placementId
                handleOnAdLoaded(AdType.INTERSTITIAL, adId, callback)
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String?,
                error: UnityAds.UnityAdsLoadError?,
                message: String?
            ) {
                interstitialAd = null
                handleOnAdLoadFail(AdType.INTERSTITIAL, error?.ordinal ?: -1, error?.name ?: "", callback)
            }

        });
    }

    override fun loadRewardedAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {

    }

    override fun loadBannerAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {

    }

    override fun showSplashAd(
        activity: Activity,
        callback: IAdCallback?
    ) {
        //不支持
    }

    override fun showInterstitialAd(
        activity: Activity,
        callback: IAdCallback?
    ) {
    }

    override fun showRewardedAd(
        activity: Activity,
        callback: IAdCallback?
    ) {
    }

    override fun showBannerAd(
        container: ViewGroup?,
        callback: IAdCallback?
    ) {
    }
}