package app.allever.android.lib.ad.core.strategy

import android.content.Context
import android.os.Handler
import android.os.Looper
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.AdManager.LoadMode
import app.allever.android.lib.ad.core.AdManager.getActiveProvider
import app.allever.android.lib.ad.core.AdManager.loadMode
import app.allever.android.lib.ad.core.AdManager.providerPool
import app.allever.android.lib.ad.core.AdManager.strategyPool
import app.allever.android.lib.ad.core.AdManager.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.core.type.BiddingResult
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.inc
import kotlin.text.compareTo
import kotlin.text.set

class BiddingModeStrategy: BaseModeStrategy() {
    
    companion object {
        const val TAG = "BiddingModeStrategy"
    }

    private data class BiddingState(
        val totalProviders: Int,
        val startTime: Long,
        val timeout: Long,
        val results: ConcurrentHashMap<String, BiddingEntry>,
        val callback: IAdCallback?,
        val adType: AdType,
        var completedCount: Int = 0,
        var isFinished: Boolean = false,
        var isPreload: Boolean = false
    )

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

        log("$TAG: [BIDDING] === BIDDING SIMULATION MODE ===")
        log("$TAG: [BIDDING] 1. All providers with supportBidding=true participate")
        log("$TAG: [BIDDING] 2. Each provider generates SIMULATED random price")
        log("$TAG: [BIDDING] 3. Price ranges vary by provider (for testing)")
        log("$TAG: [BIDDING] 4. Winner: provider with HIGHEST simulated eCPM wins")
        log("$TAG: [BIDDING] ===================================")

        val biddingProviders = getProviders()

        if (biddingProviders.isEmpty()) {
            logE("$TAG: [BIDDING] No providers with bidding support available")
            // 降为请求单个广告源
            val activeProvider = getActiveProvider()
            if (activeProvider != null) {
                log("$TAG: [BIDDING] Falling back to single provider mode")
                strategyPool[LoadMode.SINGLE]?.loadAd(context, adType, callback)
            } else {
                callback?.onAdFail(-1, "No available providers for bidding")
            }
            return
        }

        log("$TAG: [BIDDING] Parallel loading ${biddingProviders.size} providers...")

        val biddingState = BiddingState(
            totalProviders = biddingProviders.size,
            startTime = System.currentTimeMillis(),
            timeout = getBiddingTimeout(biddingProviders),
            results = ConcurrentHashMap(),
            callback = callback,
            adType = adType
        )

        biddingProviders.forEachIndexed { index, (providerType, config) ->

            val provider = providerPool[providerType]
            if (provider == null) {
                log("$TAG: [BIDDING] [$index] $providerType not initialized, skip")
                biddingState.markFailed(providerType, -1, "Not initialized")
                return@forEachIndexed
            }

            launchBiddingRequest(
                state = biddingState,
                index = index,
                providerType = providerType,
                provider = provider,
                config = config,
                context = context,
                adType = adType
            )
        }

        startBiddingTimeoutMonitor(biddingState)
    }

    override fun checkCache(
        adType: AdType,
        callback: IAdCallback?
    ): Boolean {
        //这需要竞价成功后，切换provider才行
        val activeProvider = getActiveProvider() ?: return false

        if (activeProvider.isReady(adType)) {
            val providerType = activeProvider.getProviderType()
            log("${TAG}: [CACHE-BIDDING] ✅ Using last bidding winner cache: $providerType")
            log("${TAG}: [CACHE-BIDDING] Note: This was the winner from previous bidding round")

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
        log("${TAG}: [PRELOAD-BIDDING] Mode: ${loadMode.name} (must be BIDDING)")

        if (loadMode != LoadMode.BIDDING) {
            logE("${TAG}: [PRELOAD-BIDDING] ERROR: Current mode is ${loadMode.name}, not BIDDING")
            return
        }

        val biddingProviders = getProviders()

        if (biddingProviders.isEmpty()) {
            logE("${TAG}: [PRELOAD-BIDDING] No bidding providers available")
            return
        }

        log("${TAG}: [PRELOAD-BIDDING] Parallel requesting ${biddingProviders.size} providers...")

        val preloadState = BiddingState(
            totalProviders = biddingProviders.size,
            startTime = System.currentTimeMillis(),
            timeout = getBiddingTimeout(biddingProviders),
            results = ConcurrentHashMap(),
            callback = null,
            adType = adType,
            isPreload = true
        )

        biddingProviders.forEachIndexed { index, (providerType, config) ->

            val provider = providerPool[providerType]
            if (provider == null) {
                log("${TAG}: [PRELOAD-BIDDING] [$index] $providerType not initialized, skip")
                preloadState.markFailed(providerType, -1, "Not initialized")
                return@forEachIndexed
            }

            launchBiddingRequest(
                state = preloadState,
                index = index,
                providerType = providerType,
                provider = provider,
                config = config,
                context = context,
                adType = adType
            )
        }

        startBiddingTimeoutMonitor(preloadState)
    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportBidding }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private fun handleBiddingResponse(
        state: BiddingState,
        providerType: String,
        success: Boolean,
        eCPM: Double = 0.0,
        errorCode: Int = -1,
        errorMessage: String = ""
    ) {
        synchronized(state) {
            if (state.isFinished) return

            state.results[providerType] = BiddingEntry(
                success = success,
                eCPM = eCPM,
                errorCode = errorCode,
                errorMessage = errorMessage
            )

            state.completedCount++

            val modeTag = if (state.isPreload) "[PRELOAD-BIDDING]" else "[BIDDING]"

            val priceInfo = if (success && eCPM > 0) {
                " | eCPM=\$${"%.2f".format(eCPM)} (SIMULATED)"
            } else if (success && eCPM == 0.0) {
                " | eCPM=\$0.00 (No simulation - fallback)"
            } else {
                ""
            }

            log("${TAG}: $modeTag Response received: $providerType" +
                    " | Success=$success" +
                    priceInfo +
                    " | Progress=${state.completedCount}/${state.totalProviders}")

            checkBiddingCompletion(state)
        }
    }

    private fun checkBiddingCompletion(state: BiddingState) {
        val allResponded = state.completedCount >= state.totalProviders
        val elapsed = System.currentTimeMillis() - state.startTime

        if (!allResponded && elapsed < state.timeout) {
            return
        }

        state.isFinished = true

        val elapsedTime = System.currentTimeMillis() - state.startTime
        val modeTag = if (state.isPreload) "[PRELOAD-BIDDING]" else "[BIDDING]"

        log("${TAG}: $modeTag === ${if (state.isPreload) "PRE-LOAD" else "BIDDING"} COMPLETED ===")
        log("${TAG}: $modeTag Time elapsed: ${elapsedTime}ms")
        log("${TAG}: $modeTag Total responses: ${state.completedCount}/${state.totalProviders}")

        val winner = state.results.entries
            .filter { it.value.success }
            .maxByOrNull { it.value.eCPM }

        if (winner != null) {
            val result = BiddingResult(
                providerType = winner.key,
                eCPM = winner.value.eCPM,
                adType = state.adType,
                loadTime = elapsedTime,
                timestamp = System.currentTimeMillis()
            )

            val priceSource = if (result.eCPM > 0) {
                "Simulated random price (for testing)"
            } else {
                "No price available"
            }

            val actionLabel = if (state.isPreload) "PRE-LOADED" else "WINNER"

            log("${TAG}: $modeTag 🏆 $actionLabel: ${result.providerType}" +
                    " | Price: \$${result.formattedPrice}" +
                    " | Source: $priceSource" +
                    " | Time: ${result.loadTime}ms")

            switchToProvider(winner.key)

            if (state.isPreload) {
                log("${TAG}: $modeTag ✅ Preload successful! Next ad will use: ${winner.key}")
                log("${TAG}: $modeTag 📦 Ad cached and ready for next show()")
            } else {
                state.callback?.onAdLoadedWithPrice(result.eCPM)
            }

        } else {
            logE("${TAG}: $modeTag ❌ ALL PROVIDERS FAILED")
            if (!state.isPreload) {
                state.callback?.onAdFail(-1, "All bidding providers failed")
            } else {
                logE("${TAG}: $modeTag ⚠️  Preload failed - no ad available for next request")
            }
        }

        if (!state.isPreload) {
            logBiddingDetails(state)
        }
    }

    private fun startBiddingTimeoutMonitor(state: BiddingState) {
        //TODO CHECK 每次都创建Handler
        Handler(Looper.getMainLooper()).postDelayed({
            synchronized(state) {
                if (!state.isFinished) {
                    val modeTag = if (state.isPreload) "[PRELOAD-BIDDING]" else "[BIDDING]"
                    log("${TAG}: $modeTag ⏰ TIMEOUT! (${state.timeout}ms)")
                    log("${TAG}: $modeTag Completed: ${state.completedCount}/${state.totalProviders}")

                    checkBiddingCompletion(state)
                }
            }
        }, state.timeout)
    }

    private fun launchBiddingRequest(
        state: BiddingState,
        index: Int,
        providerType: String,
        provider: IAdProvider,
        config: AdProviderConfig,
        context: Context,
        adType: AdType,
    ) {
        val adId = config.getAdIdByType(adType)  ?: run {
            state.markFailed(providerType, -1, "No ad ID")
            logE("${TAG}: ${TAG} [$index/${state.totalProviders}] ERROR: No ad ID")
            return
        }

        val modeTag = if (state.isPreload) "[PRELOAD-BIDDING]" else "[BIDDING]"

        log("${TAG}: $modeTag [$index/${state.totalProviders}] Requesting: $providerType")

        provider.loadAd(context, adType, adId, object : IAdCallback {

            override fun onAdLoaded() {
                handleBiddingResponse(state, providerType, true, 0.0)
            }

            override fun onAdLoadedWithPrice(eCPM: Double) {
                handleBiddingResponse(state, providerType, true, eCPM)
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                handleBiddingResponse(state, providerType, false, 0.0, errorCode, errorMessage)
            }

            override fun onAdShow() {}
            override fun onAdClick() {}
            override fun onAdDismiss() {}
            override fun onAdRewarded(amount: Int, name: String) {}
        })
    }

    private fun BiddingState.markFailed(
        providerType: String,
        errorCode: Int,
        errorMessage: String
    ) {
        results[providerType] = BiddingEntry(
            success = false,
            errorCode = errorCode,
            errorMessage = errorMessage
        )
        completedCount++

        synchronized(this) {
            if (!isFinished && completedCount >= totalProviders) {
                checkBiddingCompletion(this)
            }
        }
    }

    private fun getBiddingTimeout(providers: List<Pair<String, AdProviderConfig>>): Long {
        if (providers.isEmpty()) return 5000L

        return providers.maxOf { (_, config) ->
            config.biddingTimeout.coerceAtLeast(1000L)
        }
    }

    private fun logBiddingDetails(state: BiddingState) {
        val sb = StringBuilder()
        sb.appendLine("┌─────────────────────────────────────┐")
        sb.appendLine("│       BIDDING RESULTS DETAIL       │")
        sb.appendLine("├──────────┬────────┬────────┬────────┤")
        sb.appendLine("│ Provider │ Status │  eCPM  │  Time  │")
        sb.appendLine("├──────────┼────────┼────────┼────────┤")

        state.results.forEach { (type, entry) ->
            val status = if (entry.success) "✓ WIN" else "✗ FAIL"
            val price = if (entry.success) "$${"%.2f".format(entry.eCPM)}" else "-"
            sb.appendLine("│ $type │ $status │ $price │    -   │")
        }

        sb.appendLine("└──────────┴────────┴────────┴────────┘")
        log(sb.toString())
    }
    
    
}