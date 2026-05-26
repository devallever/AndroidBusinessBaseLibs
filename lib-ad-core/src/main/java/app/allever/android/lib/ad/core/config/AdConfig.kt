package app.allever.android.lib.ad.core.config

data class AdConfig(
    val adProviderType: String = "",  // ✅ 改为 String，完全解耦
    val appId: String = "",
    val splashAdId: String = "",
    val interstitialAdId: String = "",
    val rewardVideoAdId: String = "",
    val bannerAdId: String = "",
    val nativeAdId: String = ""
) {
    fun isNotEmpty(): Boolean = adProviderType.isNotEmpty()
}
