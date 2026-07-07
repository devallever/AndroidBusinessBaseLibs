package app.allever.android.lib.ad.core.strategy

import android.annotation.SuppressLint
import android.content.Context
import app.allever.android.lib.ad.core.AdCore.LoadMode
import app.allever.android.lib.ad.core.AdCore.getActiveProvider
import app.allever.android.lib.ad.core.AdCore.providerPool
import app.allever.android.lib.ad.core.AdCore.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.core.type.BiddingResult
import app.allever.android.lib.core.ext.log
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

class BiddingModeStrategy : BaseModeStrategy() {

    private data class BiddingEntry(
        val success: Boolean,
        val eCPM: Double = 0.0,
        val errorCode: Int = -1,
        val errorMessage: String = ""
    )

    override fun loadAd(
        context: Context, adType: AdType, callback: IAdCallback?
    ) {
        executeBidding(
            context = context,
            adType = adType,
            callback = callback,
            isPreload = false,
            checkMode = false
        )
    }

    override fun preload(
        context: Context, adType: AdType
    ) {
        executeBidding(
            context = context,
            adType = adType,
            callback = null,
            isPreload = true,
            checkMode = true
        )
    }

    override fun checkCache(
        adType: AdType, callback: IAdCallback?
    ): Boolean {
        val activeProvider = getActiveProvider(adType) ?: return false
        val providerType = activeProvider.getProviderType()

        if (activeProvider.isReady(adType)) {
            AdLog.logMessage("Using last bidding winner cache", providerType = providerType, adType = adType)
            return true
        }

        AdLog.logMessage("No valid cache from previous bidding winner", strategyName = TAG, adType = adType)
        return false

    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportBidding }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private fun executeBidding(
        context: Context,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean,
        checkMode: Boolean = false
    ) {
        AdLog.logMessage("Start binding", strategyName = TAG, isPreload = isPreload)
        if (!isPreload) {
            AdLog.logMessage("=== BIDDING WITH COROUTINES ===", strategyName = TAG, isPreload = false)
            AdLog.logMessage("Using coroutineScope + async for parallel requests", isPreload = false)
        } else {
            AdLog.logMessage("Purpose: Re-bid after ad dismiss to find new winner", strategyName = TAG, isPreload = true)
        }

        if (checkMode && !checkLoadMode(LoadMode.BIDDING, isPreload)) {
            return
        }

        val biddingProviders = getProviders()

        if (biddingProviders.isEmpty()) {
            AdLog.logMessage( "No providers with bidding support available", strategyName = TAG, isPreload = isPreload)

            if (!isPreload) {
                fallbackToSingle(context, adType, callback, false)
            }
            return
        }

        AdLog.logMessage("Parallel ${if (isPreload) "requesting" else "loading"} ${biddingProviders.size} providers with coroutines", isPreload = isPreload)

        scope.launch {
            val callbackRef = WeakReference(callback)
            val timeout = getBiddingTimeout(biddingProviders)

            // 1. 将 deferredList 声明在 try 块外部，以便 catch 块可以访问
            // 注意：async 必须在当前的协程作用域内启动，这里直接在 launch 内启动
            val deferredList = biddingProviders.mapIndexed { index, (providerType, config) ->
                async {
                    try {
                        tryLoadFromSingleProvider(index, biddingProviders.size, providerType, config, context, adType)
                    } catch (e: Exception) {
                        // 内部消化异常，防止 async 抛出未捕获异常导致外层崩溃
                        Pair(providerType, BiddingEntry(success = false, errorMessage = e.message ?: "Cancelled"))
                    }
                }
            }

            try {
                // 2. withTimeout 仅包裹 awaitAll()
                // 如果超时，只会取消 awaitAll 的等待，不会自动取消外部的 async 任务
                val collectedResults = withTimeout(timeout) {
                    deferredList.awaitAll().toMap()
                }

                if (collectedResults.isNotEmpty()) {
                    handleBiddingResults(collectedResults, adType, callbackRef.get(), isPreload)

                    AdLog.logMessage(
                        message = "All providers responded within ${timeout}ms",
                        strategyName = TAG,
                        isPreload = isPreload
                    )
                } else {
                    if (!isPreload) fallbackToSingle(context, adType, callbackRef.get(), false)
                }

            } catch (e: TimeoutCancellationException) {
                // 3. 超时异常捕获：收集已完成的结果
                // getCompleted() 会获取已计算的结果，如果没有完成会抛出 IllegalStateException，所以需要过滤
                val partialResults = deferredList
                    .filter { it.isCompleted && !it.isCancelled }
                    .mapNotNull { runCatching { it.getCompleted() }.getOrNull() }
                    .toMap()

                // 4. ⚠️ 关键补漏：手动取消所有尚未完成的 async 任务！
                // 因为 async 提到了 withTimeout 外部，超时后它们不会被自动取消。
                // 如果不调用 cancel()，这些网络请求协程会在后台继续运行，造成资源浪费。
                deferredList.forEach { if (it.isActive) it.cancel() }

                if (partialResults.isNotEmpty()) {
                    AdLog.logMessage(
                        message = "⏰ TIMEOUT! (${timeout}ms) | Collected ${partialResults.size}/${biddingProviders.size} results before timeout",
                        strategyName = TAG,
                        isPreload = isPreload,
                        success = false
                    )
                    handleBiddingResults(partialResults, adType, callbackRef.get(), isPreload)
                } else {
                    AdLog.logMessage(
                        message = "No results collected, all failed or cancelled",
                        strategyName = TAG,
                        adType = adType,
                        isPreload = isPreload,
                        success = false
                    )
                    if (!isPreload) fallbackToSingle(context, adType, callbackRef.get(), false)
                }
            }
        }
    }

    private suspend fun tryLoadFromSingleProvider(
        index: Int,
        totalSize: Int,
        providerType: String,
        config: AdProviderConfig,
        context: Context,
        adType: AdType
    ): Pair<String, BiddingEntry> {

        val provider = providerPool[providerType]
        if (provider == null) {
            AdLog.logMessage("[$index/$totalSize] $providerType not initialized, skip", strategyName = TAG, adType = adType)
            return Pair(
                providerType,
                BiddingEntry(success = false, errorCode = -1, errorMessage = "Not initialized")
            )
        }

        val adId = config.getAdIdByType(adType) ?: run {
            AdLog.logMessage("[$index/$totalSize] ERROR: No ad ID for $providerType", strategyName = TAG, adType = adType, providerType = providerType, success = false)
            return Pair(
                providerType,
                BiddingEntry(success = false, errorCode = -1, errorMessage = "No ad ID")
            )
        }

        AdLog.logMessage("[$index/$totalSize] Requesting: $providerType", strategyName = TAG, adType = adType)

        return suspendCancellableCoroutine { continuation ->
            val adCallback = object : IAdCallback {
                private fun complete() {
                    pendingCallbacks.remove(this)
                }

                override fun onAdLoadedWithPrice(price: Double) {
                    AdLog.logMessage("[$index/$totalSize] Loaded: $providerType | Price: $$price", strategyName = TAG, success = true, adType = adType, providerType = providerType)
                    if (continuation.isActive) {
                        continuation.resume(
                            Pair(providerType, BiddingEntry(success = true, eCPM = price))
                        )
                    }
                    complete()
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    AdLog.logMessage("[$index/$totalSize] Failed: $providerType | Error($errorCode): $errorMessage", strategyName = TAG, success = false, adType = adType, providerType = providerType)
                    if (continuation.isActive) {
                        continuation.resume(
                            Pair(
                                providerType, BiddingEntry(
                                    success = false,
                                    errorCode = errorCode,
                                    errorMessage = errorMessage
                                )
                            )
                        )
                    }
                    complete()
                }

                override fun onAdShow() {}
                override fun onAdClick() {}
                override fun onAdDismiss() {}
                override fun onAdRewarded(amount: Int, name: String) {}
            }

            // 持有强引用，防止 Provider 内部 WeakReference 包装的 callback 被 GC 回收
            pendingCallbacks.add(adCallback)
            provider.loadAd(context, adType, adId, adCallback)

            continuation.invokeOnCancellation {
                pendingCallbacks.remove(adCallback)
                AdLog.logMessage("[$index/$totalSize] ⚠️ Cancelling: $providerType", strategyName = TAG, success = false, adType = adType, providerType = providerType)
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun handleBiddingResults(
        results: Map<String, BiddingEntry>,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean,
    ) {
        AdLog.logMessage("=== ${if (isPreload) "PRE-LOAD" else "BIDDING"} COMPLETED ===", strategyName = TAG, adType = adType, isPreload =  isPreload)
        AdLog.logMessage("Total responses: ${results.size}", strategyName = TAG, adType = adType, isPreload =  isPreload)

        val winner = results.entries.filter { it.value.success }.maxByOrNull { it.value.eCPM }

        if (winner != null) {
            val result = BiddingResult(
                providerType = winner.key,
                eCPM = winner.value.eCPM,
                adType = adType,
                loadTime = 0,
                timestamp = System.currentTimeMillis()
            )

            val priceSource = if (result.eCPM > 0) {
                "Simulated random price (for testing)"
            } else {
                "No price available"
            }

            val actionLabel = if (isPreload) "PRE-LOADED" else "WINNER"


                AdLog.logMessage("🏆 $actionLabel: ${result.providerType}" + " | Price: $${result.formattedPrice}" + " | Source: $priceSource", strategyName = TAG, adType = adType, isPreload = isPreload)


            switchToProvider(winner.key, adType)

            if (isPreload) {
                AdLog.logMessage("Preload successful! Next ad will use: ${winner.key}", strategyName = TAG, adType = adType, isPreload = true, success = true)
                AdLog.logMessage("📦 Ad cached and ready for next show()", strategyName = TAG, adType = adType)
            } else {
                callback?.onAdLoadedWithPrice(result.eCPM)
            }

        } else {
            AdLog.logMessage( "No winner", isPreload = isPreload, success = false, adType = adType)

            if (!isPreload) {
                callback?.onAdFail(-1, "All bidding providers failed")
            } else {
                AdLog.logMessage("Preload failed - no ad available for next request", strategyName = TAG, adType = adType)
            }
        }

        if (!isPreload) {
            logBiddingDetails(results)
        }
    }

    private fun getBiddingTimeout(providers: List<Pair<String, AdProviderConfig>>): Long {
        if (providers.isEmpty()) return 20000L

        return providers.maxOf { (_, config) ->
            config.biddingTimeout.coerceAtLeast(10000L)
        }
    }

    private fun logBiddingDetails(results: Map<String, BiddingEntry>) {
        val sb = StringBuilder()
        sb.appendLine("┌─────────────────────────────────────┐")
        sb.appendLine("│       BIDDING RESULTS DETAIL       │")
        sb.appendLine("├──────────┬────────┬────────┬────────┤")
        sb.appendLine("│ Provider │ Status │  eCPM  │  Time  │")
        sb.appendLine("├──────────┼────────┼────────┼────────┤")

        results.forEach { (type, entry) ->
            val status = if (entry.success) "✓ WIN" else "✗ FAIL"
            val price = if (entry.success) "$${"%.2f".format(entry.eCPM)}" else "-"
            sb.appendLine("│ $type │ $status │ $price │    -   │")
        }

        sb.appendLine("└──────────┴────────┴────────┴────────┘")
        log(sb.toString())
    }
}
