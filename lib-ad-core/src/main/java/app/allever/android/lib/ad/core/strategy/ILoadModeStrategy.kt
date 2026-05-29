package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType

interface ILoadModeStrategy {
    fun loadAd(context: Context, adType: AdType, callback: IAdCallback?)

    fun preload(context: Context, adType: AdType)

    fun checkCache(adType: AdType, callback: IAdCallback?): Boolean

    fun getProviders(): List<Pair<String, AdProviderConfig>>
}