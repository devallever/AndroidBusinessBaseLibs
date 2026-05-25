package app.allever.android.sample.ad.applovin

interface AdCallback {
    fun onAdLoaded() {}
    fun onAdFailLoad() {}
    fun onAdShow() {}
    fun onAdDismiss() {}

    fun onAdClick() {}

    fun onRewarded() {}

}