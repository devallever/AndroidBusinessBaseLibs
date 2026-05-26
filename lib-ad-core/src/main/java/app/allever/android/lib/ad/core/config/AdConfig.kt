package app.allever.android.lib.ad.core.config

data class AdConfig(
    val adProviderType: AdProviderType = AdProviderType.NONE,
    val appId: String = "",
    val splashAdId: String = "",
    val interstitialAdId: String = "",
    val rewardVideoAdId: String = "",
    val bannerAdId: String = "",
    val nativeAdId: String = ""
)
