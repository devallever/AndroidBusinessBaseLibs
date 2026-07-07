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
            val collectedResults = mutableMapOf<String, BiddingEntry>()

            try {
                withTimeout(timeout) {
                    coroutineScope {
                        biddingProviders.mapIndexed { index, (providerType, config) ->
                            launch {
                                val result = tryLoadFromSingleProvider(
                                    index,
                                    biddingProviders.size,
                                    providerType,
                                    config,
                                    context,
                                    adType
                                )
                                synchronized(collectedResults) {
                                    collectedResults[providerType] = result.second
                                }
                            }
                        }
                    }
                }

                AdLog.logMessage(
                    message = "All providers responded within ${timeout}ms",
                    strategyName = TAG,
                    isPreload = isPreload
                )

            } catch (e: TimeoutCancellationException) {
                AdLog.logMessage(
                    message = "⏰ TIMEOUT! (${timeout}ms) | Collected ${collectedResults.size}/${biddingProviders.size} results before timeout",
                    strategyName = TAG,
                    isPreload = isPreload,
                    success = false
                )
            } catch (e: Exception) {
                AdLog.logMessage(
                    message = e.message ?: "Unknown error",
                    strategyName = TAG,
                    isPreload = isPreload,
                    success = false
                )
            }

            // 超时后，只要有结果（无论是否全部完成），都执行竞价
            if (collectedResults.isNotEmpty()) {
                val successCount = collectedResults.values.count { it.success }
                AdLog.logMessage(
                    message = "Executing bidding with ${collectedResults.size} results ($successCount success)",
                    strategyName = TAG,
                    adType = adType,
                    isPreload = isPreload
                )
                handleBiddingResults(collectedResults.toMap(), adType, callbackRef.get(), isPreload)
            } else {
                AdLog.logMessage(
                    message = "No results collected, all failed or cancelled",
                    strategyName = TAG,
                    adType = adType,
                    isPreload = isPreload,
                    success = false
                )
                if (!isPreload) {
                    fallbackToSingle(context, adType, callbackRef.get(), false)
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
            provider.loadAd(context, adType, adId, object : IAdCallback {
                override fun onAdLoaded() {
                    AdLog.logMessage("[$index/$totalSize] Loaded: $providerType | Price: \$0.00", strategyName = TAG, success = true, adType = adType, providerType = providerType)
                    if (continuation.isActive) {
                        continuation.resume(
                            Pair(
                                providerType,
                                BiddingEntry(success = true, eCPM = 0.0)
                            )
                        )
                    }
                }

                override fun onAdLoadedWithPrice(price: Double) {
                    AdLog.logMessage("[$index/$totalSize] Loaded: $providerType | Price: $$price", strategyName = TAG, success = true, adType = adType, providerType = providerType)
                    if (continuation.isActive) {
                        continuation.resume(
                            Pair(providerType, BiddingEntry(success = true, eCPM = price))
                        )
                    }
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
                }

                override fun onAdShow() {}
                override fun onAdClick() {}
                override fun onAdDismiss() {}
                override fun onAdRewarded(amount: Int, name: String) {}
            })

            continuation.invokeOnCancellation {
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
