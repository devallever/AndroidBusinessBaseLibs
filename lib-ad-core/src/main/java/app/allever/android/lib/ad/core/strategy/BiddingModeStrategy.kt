package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdManager.LoadMode
import app.allever.android.lib.ad.core.AdManager.getActiveProvider
import app.allever.android.lib.ad.core.AdManager.providerPool
import app.allever.android.lib.ad.core.AdManager.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.core.type.BiddingResult
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
            logPrefix = AdLog.PREFIX_BIDDING,
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
            logPrefix = AdLog.PREFIX_BIDDING,
            checkMode = true
        )
    }

    override fun checkCache(
        adType: AdType, callback: IAdCallback?
    ): Boolean {
        val activeProvider = getActiveProvider() ?: return false

        if (activeProvider.isReady(adType)) {
            val providerType = activeProvider.getProviderType()
            logAction(AdLog.PREFIX_CACHE, "Using last bidding winner cache", providerType)
            return true
        }

        log(AdLog.format(TAG, AdLog.PREFIX_CACHE, "No valid cache from previous bidding winner"))
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
        logPrefix: String,
        checkMode: Boolean = false
    ) {
        logAction(logPrefix, "Starting ${if (isPreload) "pre-" else ""}bidding", adType.name, isPreload)

        if (!isPreload) {
            log(AdLog.format(TAG, logPrefix, "=== BIDDING WITH COROUTINES ==="))
            log(AdLog.format(TAG, logPrefix, "Using coroutineScope + async for parallel requests"))
        } else {
            log(AdLog.format(TAG, logPrefix, "Purpose: Re-bid after ad dismiss to find new winner"))
        }

        if (checkMode && !checkLoadMode(LoadMode.BIDDING, logPrefix, isPreload)) {
            return
        }

        val biddingProviders = getProviders()

        if (biddingProviders.isEmpty()) {
            logError(logPrefix, "No providers with bidding support available", isPreload)

            if (!isPreload) {
                fallbackToSingle(context, adType, callback, logPrefix, isPreload)
            }
            return
        }

        logAction(
            logPrefix,
            "Parallel ${if (isPreload) "requesting" else "loading"}",
            "${biddingProviders.size} providers with coroutines",
            isPreload
        )

        scope.launch {
            var results: Map<String, BiddingEntry> = emptyMap()

            try {
                val timeout = getBiddingTimeout(biddingProviders)

                results = withTimeout(timeout) {
                    parallelBiddingRequest(biddingProviders, context, adType)
                }

                handleBiddingResults(results, adType, callback, isPreload, logPrefix)

            } catch (e: TimeoutCancellationException) {
                logE(AdLog.formatTimeout(TAG, logPrefix, getBiddingTimeout(biddingProviders), isPreload))

                if (!isPreload) {
                    if (results.isEmpty()) {
                        callback?.onAdFail(-1, "Bidding timeout")
                    } else {
                        handleBiddingResults(results, adType, callback, isPreload, logPrefix)
                    }
                }

            } catch (e: Exception) {
                logError(logPrefix, "Error: ${e.message}", isPreload)

                if (!isPreload) {
                    callback?.onAdFail(-1, e.message ?: "Unknown error")
                }
            }
        }
    }

    private suspend fun parallelBiddingRequest(
        providers: List<Pair<String, AdProviderConfig>>, context: Context, adType: AdType
    ): Map<String, BiddingEntry> {

        return coroutineScope {
            providers.mapIndexed { index, (providerType, config) ->
                async {
                    tryLoadFromSingleProvider(
                        index,
                        providers.size,
                        providerType,
                        config,
                        context,
                        adType
                    )
                }
            }.awaitAll().toMap()
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
            log(AdLog.format(TAG, AdLog.PREFIX_BIDDING, "[$index/$totalSize] $providerType not initialized, skip"))
            return Pair(
                providerType,
                BiddingEntry(success = false, errorCode = -1, errorMessage = "Not initialized")
            )
        }

        val adId = config.getAdIdByType(adType) ?: run {
            logError(AdLog.PREFIX_BIDDING, "[$index/$totalSize] ERROR: No ad ID for $providerType")
            return Pair(
                providerType,
                BiddingEntry(success = false, errorCode = -1, errorMessage = "No ad ID")
            )
        }

        log(AdLog.format(TAG, AdLog.PREFIX_BIDDING, "[$index/$totalSize] Requesting: $providerType"))

        return suspendCancellableCoroutine { continuation ->
            provider.loadAd(context, adType, adId, object : IAdCallback {
                override fun onAdLoaded() {
                    log(AdLog.formatSuccess(TAG, AdLog.PREFIX_BIDDING, "[$index/$totalSize] Loaded: $providerType | Price: \$0.00"))
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
                    log(AdLog.formatSuccess(TAG, AdLog.PREFIX_BIDDING, "[$index/$totalSize] Loaded: $providerType | Price: $$price"))
                    if (continuation.isActive) {
                        continuation.resume(
                            Pair(providerType, BiddingEntry(success = true, eCPM = price))
                        )
                    }
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    log(AdLog.formatError(TAG, AdLog.PREFIX_BIDDING, "[$index/$totalSize] Failed: $providerType | Error($errorCode): $errorMessage"))
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
                log(AdLog.format(TAG, AdLog.PREFIX_BIDDING, "[$index/$totalSize] ⚠️ Cancelled: $providerType"))
            }
        }
    }

    private fun handleBiddingResults(
        results: Map<String, BiddingEntry>,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean,
        logPrefix: String
    ) {
        log(AdLog.format(TAG, logPrefix, "=== ${if (isPreload) "PRE-LOAD" else "BIDDING"} COMPLETED ==="))
        log(AdLog.format(TAG, logPrefix, "Total responses: ${results.size}"))

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

            log(
                AdLog.format(TAG, logPrefix, "🏆 $actionLabel: ${result.providerType}" + " | Price: $${result.formattedPrice}" + " | Source: $priceSource")
            )

            switchToProvider(winner.key)

            if (isPreload) {
                logSuccess(logPrefix, "Preload successful! Next ad will use: ${winner.key}", isPreload)
                log(AdLog.format(TAG, logPrefix, "📦 Ad cached and ready for next show()"))
            } else {
                callback?.onAdLoadedWithPrice(result.eCPM)
            }

        } else {
            logError(logPrefix, "ALL PROVIDERS FAILED", isPreload)
            
            if (!isPreload) {
                callback?.onAdFail(-1, "All bidding providers failed")
            } else {
                logError(logPrefix, "Preload failed - no ad available for next request", isPreload)
            }
        }

        if (!isPreload) {
            logBiddingDetails(results)
        }
    }

    private fun getBiddingTimeout(providers: List<Pair<String, AdProviderConfig>>): Long {
        if (providers.isEmpty()) return 5000L

        return providers.maxOf { (_, config) ->
            config.biddingTimeout.coerceAtLeast(1000L)
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
