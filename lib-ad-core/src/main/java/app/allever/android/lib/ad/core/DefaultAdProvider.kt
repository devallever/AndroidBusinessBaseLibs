package app.allever.android.lib.ad.core

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig

class DefaultAdProvider : BaseAdProvider() {
    override fun onDestroy() {

    }

    companion object {
        const val PROVIDER_NAME = "DEFAULT"
    }

    override fun getProviderType() = PROVIDER_NAME

    override fun init(
        context: Context,
        config: AdProviderConfig,
        callback: (() -> Unit)?
    ) {

    }

    override fun loadSplashAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {

    }

    override fun loadInterstitialAd(
        context: Context,
        adId: String,
        callback: IAdCallback?
    ) {
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