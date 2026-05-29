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
        executeBidding(
            context = context,
            adType = adType,
            callback = callback,
            isPreload = false,
            logPrefix = "[BIDDING]",
            checkMode = false
        )
    }

    override fun preload(
        context: Context,
        adType: AdType
    ) {
        executeBidding(
            context = context,
            adType = adType,
            callback = null,
            isPreload = true,
            logPrefix = "[PRELOAD-BIDDING]",
            checkMode = true
        )
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
        log("$TAG: $logPrefix Starting ${if (isPreload) "pre-" else ""}bidding for ${adType.name}")

        if (!isPreload) {
            log("$TAG: $logPrefix === BIDDING WITH COROUTINES ===")
            log("$TAG: $logPrefix Using coroutineScope + async for parallel requests")
        } else {
            log("$TAG: $logPrefix Purpose: Re-bid after ad dismiss to find new winner")
        }

        if (checkMode && loadMode != LoadMode.BIDDING) {
            logE("$TAG: $logPrefix ERROR: Current mode is ${loadMode.name}, not BIDDING")
            return
        }

        val biddingProviders = getProviders()

        if (biddingProviders.isEmpty()) {
            logE("$TAG: $logPrefix No providers with bidding support available")

            if (!isPreload && callback != null) {
                val activeProvider = getActiveProvider()
                if (activeProvider != null) {
                    log("$TAG: $logPrefix Falling back to single provider mode")
                    strategyPool[LoadMode.SINGLE]?.loadAd(context, adType, callback)
                } else {
                    callback.onAdFail(-1, "No available providers for bidding")
                }
            }
            return
        }

        log("$TAG: $logPrefix Parallel ${if (isPreload) "requesting" else "loading"} ${biddingProviders.size} providers with coroutines...")

        CoroutineScope(Dispatchers.Main).launch {
            var results: Map<String, BiddingEntry> = emptyMap()

            try {
                val timeout = getBiddingTimeout(biddingProviders)

                results = withTimeout(timeout) {
                    parallelBiddingRequest(biddingProviders, context, adType)
                }

                handleBiddingResults(results, adType, callback, isPreload, logPrefix)

            } catch (e: TimeoutCancellationException) {
                logE("$TAG: $logPrefix ⏰ TIMEOUT! (${getBiddingTimeout(biddingProviders)}ms)")

                if (!isPreload) {
                    if (results.isEmpty()) {
                        callback?.onAdFail(-1, "Bidding timeout")
                    } else {
                        handleBiddingResults(results, adType, callback, isPreload, logPrefix)
                    }
                }

            } catch (e: Exception) {
                logE("$TAG: $logPrefix ❌ Error: ${e.message}")

                if (!isPreload) {
                    callback?.onAdFail(-1, e.message ?: "Unknown error")
                }
            }
        }
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
        isPreload: Boolean,
        logPrefix: String
    ) {
        log("$TAG: $logPrefix === ${if (isPreload) "PRE-LOAD" else "BIDDING"} COMPLETED ===")
        log("$TAG: $logPrefix Total responses: ${results.size}")

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

            log("$TAG: $logPrefix 🏆 $actionLabel: ${result.providerType}" +
                    " | Price: $${result.formattedPrice}" +
                    " | Source: $priceSource")

            switchToProvider(winner.key)

            if (isPreload) {
                log("$TAG: $logPrefix ✅ Preload successful! Next ad will use: ${winner.key}")
                log("$TAG: $logPrefix 📦 Ad cached and ready for next show()")
            } else {
                callback?.onAdLoadedWithPrice(result.eCPM)
            }

        } else {
            logE("$TAG: $logPrefix ❌ ALL PROVIDERS FAILED")
            if (!isPreload) {
                callback?.onAdFail(-1, "All bidding providers failed")
            } else {
                logE("$TAG: $logPrefix ⚠️  Preload failed - no ad available for next request")
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
