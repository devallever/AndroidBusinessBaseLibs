package app.flash.tunnel.vpn.lib.admob

import com.google.android.gms.ads.AdValue

interface AdCallback {
    open fun onStart() {}
    open fun onLoaded(adObj: Any) {}
    open fun onFailedToLoad(code: Int, err: String = "") {}
    open fun onShow() {}
    open fun onShowFailed(code: Int, err: String = "") {}
    open fun onClicked() {}
    open fun onAdListenerDestroy() {}
    open fun onDismiss() {}

    open fun onAdPaid(adValue: AdValue) {}

    open fun onCache() {}

    open fun onRewarded() {}
    open fun onRewardFail() {}

}