package app.allever.android.lib.ad.core.ad.splash

import android.app.Activity
import android.view.ViewGroup
import app.allever.android.lib.ad.core.callback.IAdCallback

interface ISplashAd {

    fun loadAndShow(
        activity: Activity,
        adId: String,
        container: ViewGroup,
        callback: IAdCallback? = null
    )

    fun load(activity: Activity, adId: String, callback: IAdCallback? = null)

    fun show(activity: Activity, container: ViewGroup, callback: IAdCallback? = null)

    fun isReady(): Boolean

    fun destroy()
}
