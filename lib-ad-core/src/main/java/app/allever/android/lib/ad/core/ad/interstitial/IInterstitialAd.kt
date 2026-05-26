package app.allever.android.lib.ad.core.ad.interstitial

import android.app.Activity
import app.allever.android.lib.ad.core.callback.IAdCallback

interface IInterstitialAd {

    fun load(activity: Activity, adId: String, callback: IAdCallback? = null)

    fun show(activity: Activity, callback: IAdCallback? = null)

    fun isReady(): Boolean

    fun destroy()
}
