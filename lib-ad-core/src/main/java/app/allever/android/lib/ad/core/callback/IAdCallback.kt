package app.allever.android.lib.ad.core.callback

interface IAdCallback {
    fun onAdLoaded() {}

    fun onAdFail(errorCode: Int, errorMessage: String) {}

    fun onAdShow() {}

    fun onAdClick() {}

    fun onAdDismiss() {}

    fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
}
