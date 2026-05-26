package app.allever.android.lib.ad.core.ad.native_

import android.app.Activity
import android.view.ViewGroup
import app.allever.android.lib.ad.core.callback.IAdCallback

interface INativeAd {

    fun loadAndShow(
        activity: Activity,
        adId: String,
        container: ViewGroup,
        callback: IAdCallback? = null
    )

    fun load(activity: Activity, adId: String, callback: IAdCallback? = null)

    fun show(container: ViewGroup, callback: IAdCallback? = null)

    fun isReady(): Boolean

    fun destroy()
}
