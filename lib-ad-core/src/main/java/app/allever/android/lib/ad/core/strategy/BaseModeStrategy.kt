package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.AdManager.LoadMode
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseModeStrategy : ILoadModeStrategy {

    protected val adManager = AdManager

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
        prefix: String = ""
    ): Boolean {
        if (provider == null) {
            logError(prefix, "No active provider available", isPreload)
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
        prefix: String = ""
    ): Boolean {
        if (adId.isNullOrEmpty()) {
            logError(prefix, "No ad ID for ${adType.name}", isPreload)
            if (!isPreload) {
                callback?.onAdFail(-1, "No ad ID for ${adType.name}")
            }
            return false
        }
        return true
    }

    protected fun logAction(
        prefix: String,
        action: String,
        detail: String = "",
        isPreload: Boolean = false
    ) {
        val message = if (detail.isNotEmpty()) "$action: $detail" else action
        log(AdLog.format(TAG, prefix, message, isPreload))
    }

    protected fun logError(
        prefix: String,
        message: String,
        isPreload: Boolean = false
    ) {
        logE(AdLog.formatError(TAG, prefix, message, isPreload))
    }

    protected fun logSuccess(
        prefix: String,
        message: String,
        isPreload: Boolean = false
    ) {
        log(AdLog.formatSuccess(TAG, prefix, message, isPreload))
    }

    protected fun fallbackToSingle(
        context: Context,
        adType: AdType,
        callback: IAdCallback?,
        prefix: String = "",
        isPreload: Boolean = false
    ) {
        logAction(prefix, "Falling back to SINGLE mode", isPreload = isPreload)
        
        if (!isPreload) {
            adManager.strategyPool[LoadMode.SINGLE]?.loadAd(context, adType, callback)
        } else {
            adManager.strategyPool[LoadMode.SINGLE]?.preload(context, adType)
        }
    }

    protected fun checkLoadMode(
        expectedMode: LoadMode,
        prefix: String,
        isPreload: Boolean = false
    ): Boolean {
        if (adManager.loadMode != expectedMode) {
            logError(
                prefix,
                "Current mode is ${adManager.loadMode.name}, not ${expectedMode.name}",
                isPreload
            )
            return false
        }
        return true
    }
}
