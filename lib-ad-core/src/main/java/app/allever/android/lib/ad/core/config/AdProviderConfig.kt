package app.allever.android.lib.ad.core.config

import app.allever.android.lib.ad.core.type.AdType

data class AdProviderConfig(
    val adProviderType: String = "",
    val appId: String = "",
    val splashAdId: String = "",
    val interstitialAdId: String = "",
    val rewardVideoAdId: String = "",
    val bannerAdId: String = "",
    val nativeAdId: String = ""
) {
    fun isNotEmpty(): Boolean = adProviderType.isNotEmpty()

    fun getAdIdByType(adType: AdType): String? {
        return when (adType) {
            AdType.SPLASH -> splashAdId
            AdType.INTERSTITIAL -> interstitialAdId
            AdType.REWARD_VIDEO -> rewardVideoAdId
            AdType.BANNER -> bannerAdId
            AdType.NATIVE -> nativeAdId
        }
    }

    companion object {
        fun empty() = AdProviderConfig()
    }
}
