package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdCore
import app.allever.android.lib.ad.core.AdCore.LoadMode
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseModeStrategy : ILoadModeStrategy {

    protected val adManager = AdCore

    protected val TAG: String
        get() = this::class.simpleName ?: "BaseModeStrategy"

    private val _scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    protected val scope: CoroutineScope
        get() = _scope

    fun destroy() {
        _scope.cancel()
    }

    protected fun requireProvider(
        provider: IAdProvider?,
        callback: IAdCallback?,
        isPreload: Boolean,
    ): Boolean {
        if (provider == null) {
            AdLog.logMessage("No active provider available", strategyName = TAG, isPreload = isPreload, success = false)
            if (!isPreload) {
                callback?.onAdFail(-1, "No active provider")
            }
            return false
        }
        return true
    }

    protected fun requireAdId(
        adId: String?,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean,
    ): Boolean {
        if (adId.isNullOrEmpty()) {
            AdLog.logMessage("No ad ID for ${adType.name}", strategyName = TAG, isPreload = isPreload, success = false, adType = adType)
            if (!isPreload) {
                callback?.onAdFail(-1, "No ad ID for ${adType.name}")
            }
            return false
        }
        return true
    }

    protected fun fallbackToSingle(
        context: Context,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean = false
    ) {
        AdLog.logMessage("Falling back to SINGLE mode", isPreload = isPreload)

        if (!isPreload) {
            adManager.strategyPool[LoadMode.SINGLE]?.loadAd(context, adType, callback)
        } else {
            adManager.strategyPool[LoadMode.SINGLE]?.preload(context, adType)
        }
    }

    protected fun checkLoadMode(
        expectedMode: LoadMode,
        isPreload: Boolean = false
    ): Boolean {
        if (adManager.loadMode != expectedMode) {
            AdLog.logMessage("Current mode is ${adManager.loadMode.name}, not ${expectedMode.name}", strategyName =  TAG, isPreload = isPreload, success = false)
            return false
        }
        return true
    }
}
