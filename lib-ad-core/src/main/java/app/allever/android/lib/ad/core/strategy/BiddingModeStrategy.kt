package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdManager.LoadMode
import app.allever.android.lib.ad.core.AdManager.getActiveProvider
import app.allever.android.lib.ad.core.AdManager.loadMode
import app.allever.android.lib.ad.core.AdManager.providerPool
import app.allever.android.lib.ad.core.AdManager.strategyPool
import app.allever.android.lib.ad.core.AdManager.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.core.type.BiddingResult
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import kotlinx.coroutines.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.coroutines.resume

class BiddingModeStrategy : BaseModeStrategy() {

    companion object {
        private const val TAG = "BiddingModeStrategy"
    }

    private data class BiddingEntry(
        val success: Boolean,
        val eCPM: Double = 0.0,
        val errorCode: Int = -1,
        val errorMessage: String = ""
    )

    override fun loadAd(
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {
        log("$TAG: [BIDDING] Starting bidding for ${adType.name}")
        log("$TAG: [BIDDING] === BIDDING WITH COROUTINES ===")
        log("$TAG: [BIDDING] Using coroutineScope + async for parallel requests")

        val biddingProviders = getProviders()

        if (biddingProviders.isEmpty()) {
            logE("$TAG: [BIDDING] No providers with bidding support available")
            val activeProvider = getActiveProvider()
            if (activeProvider != null) {
                log("$TAG: [BIDDING] Falling back to single provider mode")
                strategyPool[LoadMode.SINGLE]?.loadAd(context, adType, callback)
            } else {
                callback?.onAdFail(-1, "No available providers for bidding")
            }
            return
        }

        log("$TAG: [BIDDING] Parallel loading ${biddingProviders.size} providers with coroutines...")

        CoroutineScope(Dispatchers.Main).launch {
            var results: Map<String, BiddingEntry> = emptyMap()

            try {
                val timeout = getBiddingTimeout(biddingProviders)

                results = withTimeout(timeout) {
                    parallelBiddingRequest(biddingProviders, context, adType)
                }

                handleBiddingResults(results, adType, callback, isPreload = false)

            } catch (e: TimeoutCancellationException) {
                logE("$TAG: [BIDDING] ⏰ TIMEOUT! (${getBiddingTimeout(biddingProviders)}ms)")
                if (results.isEmpty()) {
                    callback?.onAdFail(-1, "Bidding timeout")
                } else {
                    handleBiddingResults(results, adType, callback, isPreload = false)
                }

            } catch (e: Exception) {
                logE("$TAG: [BIDDING] ❌ Error: ${e.message}")
                callback?.onAdFail(-1, e.message ?: "Unknown error")
            }
        }
    }

    override fun checkCache(
        adType: AdType,
        callback: IAdCallback?
    ): Boolean {
        val activeProvider = getActiveProvider() ?: return false

        if (activeProvider.isReady(adType)) {
            val providerType = activeProvider.getProviderType()
            log("${TAG}: [CACHE-BIDDING] ✅ Using last bidding winner cache: $providerType")
            callback?.onAdLoaded()
            return true
        }

        log("${TAG}: [CACHE-BIDDING] No valid cache from previous bidding winner")
        return false
    }

    override fun preload(
        context: Context,
        adType: AdType
    ) {
        log("${TAG}: [PRELOAD-BIDDING] Starting pre-bidding for ${adType.name}")
        log("${TAG}: [PRELOAD-BIDDING] Purpose: Re-bid after ad dismiss to find new winner")

        if (loadMode != LoadMode.BIDDING) {
            logE("${TAG}: [PRELOAD-BIDDING] ERROR: Current mode is ${loadMode.name}, not BIDDING")
            return
        }

        val biddingProviders = getProviders()

        if (biddingProviders.isEmpty()) {
            logE("${TAG}: [PRELOAD-BIDDING] No bidding providers available")
            return
        }

        log("${TAG}: [PRELOAD-BIDDING] Parallel requesting ${biddingProviders.size} providers with coroutines...")

        CoroutineScope(Dispatchers.Main).launch {
            var results: Map<String, BiddingEntry> = emptyMap()

            try {
                val timeout = getBiddingTimeout(biddingProviders)

                results = withTimeout(timeout) {
                    parallelBiddingRequest(biddingProviders, context, adType)
                }

                handleBiddingResults(results, adType, null, isPreload = true)

            } catch (e: TimeoutCancellationException) {
                logE("$TAG: [PRELOAD-BIDDING] ⏰ TIMEOUT!")

            } catch (e: Exception) {
                logE("$TAG: [PRELOAD-BIDDING] ❌ Error: ${e.message}")
            }
        }
    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportBidding }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private suspend fun parallelBiddingRequest(
        providers: List<Pair<String, AdProviderConfig>>,
        context: Context,
        adType: AdType
    ): Map<String, BiddingEntry> {

        return coroutineScope {
            providers.mapIndexed { index, (providerType, config) ->
                async {
                    tryLoadFromSingleProvider(index, providers.size, providerType, config, context, adType)
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
            log("$TAG: [BIDDING] [$index/$totalSize] $providerType not initialized, skip")
            return Pair(providerType, BiddingEntry(success = false, errorCode = -1, errorMessage = "Not initialized"))
        }

        val adId = config.getAdIdByType(adType) ?: run {
            logE("$TAG: [BIDDING] [$index/$totalSize] ERROR: No ad ID for $providerType")
            return Pair(providerType, BiddingEntry(success = false, errorCode = -1, errorMessage = "No ad ID"))
        }

        log("$TAG: [BIDDING] [$index/$totalSize] Requesting: $providerType")

        return suspendCancellableCoroutine { continuation ->
            provider.loadAd(context, adType, adId, object : IAdCallback {
                override fun onAdLoaded() {
                    log("$TAG: [BIDDING] [$index/$totalSize] ✅ Loaded: $providerType | Price: \$0.00")
                    if (continuation.isActive) {
                        continuation.resume(Pair(providerType, BiddingEntry(success = true, eCPM = 0.0)))
                    }
                }

                override fun onAdLoadedWithPrice(price: Double) {
                    log("$TAG: [BIDDING] [$index/$totalSize] ✅ Loaded: $providerType | Price: $$price")
                    if (continuation.isActive) {
                        continuation.resume(Pair(providerType, BiddingEntry(success = true, eCPM = price))
                        )
                    }
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    log("$TAG: [BIDDING] [$index/$totalSize] ❌ Failed: $providerType | Error($errorCode): $errorMessage")
                    if (continuation.isActive) {
                        continuation.resume(
                            Pair(
                                providerType,
                                BiddingEntry(
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
                log("$TAG: [BIDDING] [$index/$totalSize] ⚠️ Cancelled: $providerType")
            }
        }
    }

    private fun handleBiddingResults(
        results: Map<String, BiddingEntry>,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean
    ) {
        val modeTag = if (isPreload) "[PRELOAD-BIDDING]" else "[BIDDING]"

        log("$TAG: $modeTag === ${if (isPreload) "PRE-LOAD" else "BIDDING"} COMPLETED ===")
        log("$TAG: $modeTag Total responses: ${results.size}")

        val winner = results.entries
            .filter { it.value.success }
            .maxByOrNull { it.value.eCPM }

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

            log("$TAG: $modeTag 🏆 $actionLabel: ${result.providerType}" +
                    " | Price: $${result.formattedPrice}" +
                    " | Source: $priceSource")

            switchToProvider(winner.key)

            if (isPreload) {
                log("$TAG: $modeTag ✅ Preload successful! Next ad will use: ${winner.key}")
                log("$TAG: $modeTag 📦 Ad cached and ready for next show()")
            } else {
                callback?.onAdLoadedWithPrice(result.eCPM)
            }

        } else {
            logE("$TAG: $modeTag ❌ ALL PROVIDERS FAILED")
            if (!isPreload) {
                callback?.onAdFail(-1, "All bidding providers failed")
            } else {
                logE("$TAG: $modeTag ⚠️  Preload failed - no ad available for next request")
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
