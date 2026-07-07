package app.allever.android.lib.ad.core.base

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType

interface IAdProvider {

    fun getProviderType(): String

    fun init(context: Context, config: AdProviderConfig, callback: (() -> Unit)? = null)

    fun isInit(): Boolean

    fun isReady(adType: AdType): Boolean

    fun loadSplashAd(context: Context, adId: String, callback: IAdCallback?)
    fun loadInterstitialAd(context: Context, adId: String, callback: IAdCallback?)
    fun loadRewardedAd(context: Context, adId: String, callback: IAdCallback?)
    fun loadBannerAd(context: Context, adId: String, callback: IAdCallback?)

    fun showSplashAd(activity: Activity, callback: IAdCallback?)
    fun showInterstitialAd(activity: Activity, callback: IAdCallback?)
    fun showRewardedAd(activity: Activity, callback: IAdCallback?)
    fun showBannerAd(container: ViewGroup?, callback: IAdCallback?)

    fun loadAd(
        context: Context, adType: AdType, adId: String, callback: IAdCallback? = null
    )

    fun showAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup? = null,
        callback: IAdCallback? = null
    )

    fun destroy()
}
