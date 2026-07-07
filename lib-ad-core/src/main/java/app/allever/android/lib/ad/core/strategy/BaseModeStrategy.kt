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

    /**
     * 持有活跃的 IAdCallback 强引用，防止在 provider.loadAd() 返回后
     * 被 GC 过早回收（Provider 内部使用 WeakReference 包装 callback）。
     *
     * 问题场景：策略创建匿名 IAdCallback 传给 provider.loadAd()，
     * 该方法立即返回（SDK 异步加载），此时匿名对象无强引用；
     * 而 Provider 内部 callbackRef = WeakReference(callback) 是弱引用，
     * 导致 callback 被 GC 回收，callbackRef.get() 返回 null，
     * 回调永不触发（BiddingMode 的 continuation.resume() 永不调用，
     * WaterfallMode 的瀑布流卡住，SingleMode 的 preload 回调失效）。
     *
     * 解决方案：在此集合中持有强引用，回调触发或协程取消时释放。
     * scope 取消时（如 Activity destroy），所有 continuation 会被 cancel，
     * invokeOnCancellation 会释放强引用，不会造成泄漏。
     */
    protected val pendingCallbacks =
        java.util.Collections.synchronizedList(mutableListOf<IAdCallback>())

    fun destroy() {
        _scope.cancel()
        pendingCallbacks.clear()
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
