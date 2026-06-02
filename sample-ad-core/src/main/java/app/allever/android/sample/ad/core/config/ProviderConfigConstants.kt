package app.allever.android.sample.ad.core.config

import app.allever.android.lib.ad.core.config.AdProviderConfig

object ProviderConfigConstants {
    val ADMOB = AdProviderConfig(
        appId = AdIdConstants.AdMob.APP_ID,
        splashAdId = AdIdConstants.AdMob.SPLASH_AD_ID,
        interstitialAdId = AdIdConstants.AdMob.INTERSTITIAL_AD_ID,
        rewardVideoAdId = AdIdConstants.AdMob.REWARD_VIDEO_AD_ID,
        bannerAdId = AdIdConstants.AdMob.BANNER_AD_ID,
        nativeAdId = AdIdConstants.AdMob.NATIVE_AD_ID,
        supportWaterfall = true,
        supportBidding = true
    )

    val PANGLE = AdProviderConfig(
        appId = AdIdConstants.Pangle.APP_ID,
        splashAdId = AdIdConstants.Pangle.SPLASH_AD_ID,
        interstitialAdId = AdIdConstants.Pangle.INTERSTITIAL_AD_ID,
        rewardVideoAdId = AdIdConstants.Pangle.REWARD_VIDEO_AD_ID,
        bannerAdId = AdIdConstants.Pangle.BANNER_AD_ID,
        nativeAdId = AdIdConstants.Pangle.NATIVE_AD_ID,
        supportWaterfall = true,
        supportBidding = true
    )

    val BIGO = AdProviderConfig(
        appId = AdIdConstants.Bigo.APP_ID,
        splashAdId = AdIdConstants.Bigo.SPLASH_AD_ID,
        interstitialAdId = AdIdConstants.Bigo.INTERSTITIAL_AD_ID,
        rewardVideoAdId = AdIdConstants.Bigo.REWARD_VIDEO_AD_ID,
        bannerAdId = AdIdConstants.Bigo.BANNER_AD_ID,
        nativeAdId = AdIdConstants.Bigo.NATIVE_AD_ID,
        supportWaterfall = true,
        supportBidding = true
    )

    val APPLOVIN = AdProviderConfig(
        appId = AdIdConstants.AppLovin.APP_ID,
        splashAdId = AdIdConstants.AppLovin.SPLASH_AD_ID,
        interstitialAdId = AdIdConstants.AppLovin.INTERSTITIAL_AD_ID,
        rewardVideoAdId = AdIdConstants.AppLovin.REWARD_VIDEO_AD_ID,
        bannerAdId = AdIdConstants.AppLovin.BANNER_AD_ID,
        nativeAdId = AdIdConstants.AppLovin.NATIVE_AD_ID,
        supportWaterfall = true,
        supportBidding = true
    )
}