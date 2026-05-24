package app.allever.android.sample.ad.pangle

interface AdCallback {
    fun onAdLoaded() {}
    fun onAdFailLoad() {}
    fun onAdShow() {}
    fun onAdDismiss() {}

    fun onAdClick() {}

    fun onRewarded() {}

}