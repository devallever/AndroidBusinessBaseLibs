package app.allever.android.lib.ad.core

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.core.type.BiddingResult
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import java.util.concurrent.ConcurrentHashMap

object AdManager {

    private const val TAG = "AdManager"
    const val VERSION = "1.1.0"

    enum class LoadMode {
        SINGLE,
        WATERFALL,
        BIDDING
    }

    private var currentProvider: IAdProvider? = null
    private var currentConfig: AdProviderConfig? = null
    private var activeProviderType: String? = null
    
    private val providerPool = ConcurrentHashMap<String, IAdProvider>()

    @Volatile
    var loadMode: LoadMode = LoadMode.SINGLE
        private set

    fun init(context: Context, providerType: String, forceReinit: Boolean = false, callback: (() -> Unit)? = null) {
        log("$TAG: Initializing provider: $providerType (forceReinit=$forceReinit)")

        if (!AdProviderFactory.isProviderRegistered(providerType)) {
            logE("$TAG: Provider $providerType not registered. Call registerProvider() first.")
            callback?.invoke()
            return
        }

        val existingProvider = providerPool[providerType]
        
        if (existingProvider != null && !forceReinit) {
            log("$TAG: Provider $providerType already initialized, switching to it")
            switchToProvider(providerType)
            callback?.invoke()
            return
        }

        if (existingProvider != null && forceReinit) {
            log("$TAG: Force re-initializing provider: $providerType")
            existingProvider.destroy()
            providerPool.remove(providerType)
        }

        val config = AdProviderFactory.getConfig(providerType)
        if (config == null) {
            logE("$TAG: No config found for provider: $providerType")
            callback?.invoke()
            return
        }

        val (provider, _) = AdProviderFactory.createProvider(providerType)
        if (provider == null) {
            logE("$TAG: Failed to create provider instance for: $providerType")
            callback?.invoke()
            return
        }

        provider.init(context, config) { 
            providerPool[providerType] = provider

            log("$TAG: Provider ${provider.getProviderType()} initialized successfully")
            
            switchToProvider(providerType)
            callback?.invoke()
        }
    }

    fun setLoadMode(mode: LoadMode) {
        this.loadMode = mode
        log("$TAG: Load mode changed to: $mode")
    }

    fun switchProvider(providerType: String): Boolean {
        if (!providerPool.containsKey(providerType)) {
            logE("$TAG: Cannot switch to $providerType, not initialized")
            return false
        }
        
        return switchToProvider(providerType)
    }

    private fun switchToProvider(providerType: String): Boolean {
        val provider = providerPool[providerType]
        val config = AdProviderFactory.getConfig(providerType)
        
        if (provider == null || config == null) {
            logE("$TAG: Failed to switch to provider: $providerType")
            return false
        }
        
        currentProvider = provider
        currentConfig = config
        activeProviderType = providerType
        
        log("$TAG: Switched to active provider: $providerType")
        return true
    }

    fun loadAd(
        activity: Activity,
        adType: AdType,
        adId: String? = null,
        callback: IAdCallback? = null
    ) {
        when (loadMode) {
            LoadMode.SINGLE -> {
                loadAdSingle(activity, adType, adId, callback)
            }
            LoadMode.WATERFALL -> {
                loadAdWithWaterfall(activity, adType, adId, callback)
            }
            LoadMode.BIDDING -> {
                loadAdWithBidding(activity, adType, adId, callback)
            }
        }
    }

    private fun loadAdSingle(
        activity: Activity,
        adType: AdType,
        adId: String? = null,
        callback: IAdCallback? = null
    ) {
        val provider = getActiveProvider() ?: return
        
        val actualAdId = adId ?: getAdIdByType(adType) ?: run {
            log("$TAG: No ad ID provided for ${adType.name}")
            callback?.onAdFail(-1, "No ad ID provided")
            return
        }

        provider.loadAd(activity, adType, actualAdId, callback)
    }

    private fun loadAdWithWaterfall(
        activity: Activity,
        adType: AdType,
        adId: String? = null,
        callback: IAdCallback? = null
    ) {
        log("$TAG: [WATERFALL] Starting waterfall for ${adType.name}")
        
        val waterfallProviders = getWaterfallProviders()
        
        if (waterfallProviders.isEmpty()) {
            logE("$TAG: [WATERFALL] No providers with waterfall support available")
            
            val activeProvider = getActiveProvider()
            if (activeProvider != null) {
                log("$TAG: [WATERFALL] Falling back to single provider mode")
                loadAdSingle(activity, adType, adId, callback)
            } else {
                callback?.onAdFail(-1, "No available providers for waterfall")
            }
            return
        }
        
        val waterfallOrder = waterfallProviders.joinToString(" → ") { it.first }
        log("$TAG: [WATERFALL] Order: $waterfallOrder (${waterfallProviders.size} providers)")
        
        tryLoadFromWaterfall(
            providers = waterfallProviders,
            currentIndex = 0,
            activity = activity,
            adType = adType,
            customAdId = adId,
            callback = callback
        )
    }

    private fun getWaterfallProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportWaterfall }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private fun tryLoadFromWaterfall(
        providers: List<Pair<String, AdProviderConfig>>,
        currentIndex: Int,
        activity: Activity,
        adType: AdType,
        customAdId: String?,
        callback: IAdCallback?
    ) {
        if (currentIndex >= providers.size) {
            logE("$TAG: [WATERFALL] All ${providers.size} providers failed for ${adType.name}")
            callback?.onAdFail(-1, "All waterfall providers failed")
            return
        }
        
        val (providerType, config) = providers[currentIndex]
        val provider = providerPool[providerType]
        
        if (provider == null) {
            log("$TAG: [WATERFALL] [$currentIndex] Provider $providerType not in pool, skipping...")
            tryLoadFromWaterfall(providers, currentIndex + 1, activity, adType, customAdId, callback)
            return
        }
        
        val actualAdId = customAdId ?: config.getAdIdByType(adType)
        
        if (actualAdId.isNullOrEmpty()) {
            log("$TAG: [WATERFALL] [$currentIndex] No ad ID for $providerType/${adType.name}, skipping...")
            tryLoadFromWaterfall(providers, currentIndex + 1, activity, adType, customAdId, callback)
            return
        }
        
        log("$TAG: [WATERFALL] [$currentIndex/$providers.size] Trying: $providerType (ID: $actualAdId)")
        
        provider.loadAd(activity, adType, actualAdId, object : IAdCallback {
            
            override fun onAdLoaded() {
                log("$TAG: [WATERFALL] ✓ SUCCESS at [$currentIndex]: $providerType")
                
                switchToProvider(providerType)
                
                callback?.onAdLoaded()
            }
            
            override fun onAdFail(errorCode: Int, errorMessage: String) {
                log("$TAG: [WATERFALL] ✗ FAILED at [$currentIndex]: $providerType - Error($errorCode): $errorMessage")
                
                tryLoadFromWaterfall(
                    providers, 
                    currentIndex + 1, 
                    activity, 
                    adType, 
                    customAdId, 
                    callback
                )
            }
            
            override fun onAdShow() {
                log("$TAG: [WATERFALL] Ad shown from: $providerType")
                callback?.onAdShow()
            }
            
            override fun onAdClick() {
                log("$TAG: [WATERFALL] Ad clicked from: $providerType")
                callback?.onAdClick()
            }
            
            override fun onAdDismiss() {
                log("$TAG: [WATERFALL] Ad dismissed from: $providerType")
                callback?.onAdDismiss()
            }
            
            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                log("$TAG: [WATERFALL] Rewarded from: $providerType - Amount: $rewardAmount, Name: $rewardName")
                callback?.onAdRewarded(rewardAmount, rewardName)
            }
        })
    }

    fun showAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup? = null,
        callback: IAdCallback? = null
    ) {
        val provider = getActiveProvider() ?: return
        provider.showAd(activity, adType, container, callback)
    }

    fun isReady(adType: AdType): Boolean {
        return currentProvider?.isReady(adType) ?: false
    }

    fun loadAndShow(
        activity: Activity,
        adType: AdType,
        adId: String? = null,
        container: ViewGroup? = null,
        callback: IAdCallback? = null
    ) {
        loadAd(activity, adType, adId, object : IAdCallback {
            override fun onAdLoaded() {
                showAd(activity, adType, container, callback)
                callback?.onAdLoaded()
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                callback?.onAdFail(errorCode, errorMessage)
            }
        })
    }

    fun destroy() {
        providerPool.values.forEach { it.destroy() }
        providerPool.clear()

        currentProvider = null
        currentConfig = null
        activeProviderType = null
        loadMode = LoadMode.SINGLE
        
        log("$TAG: All providers destroyed")
    }

    fun destroyProvider(providerType: String) {
        providerPool[providerType]?.destroy()
        providerPool.remove(providerType)

        if (activeProviderType == providerType) {
            currentProvider = null
            currentConfig = null
            activeProviderType = null
        }
        
        log("$TAG: Provider $providerType destroyed")
    }

    fun isInitialized(): Boolean = currentProvider != null

    fun isProviderInitialized(providerType: String): Boolean = 
        providerPool.containsKey(providerType)

    fun getActiveProviderType(): String? = activeProviderType

    fun getInitializedProviders(): Set<String> = providerPool.keys.toSet()

    fun getWaterfallProvidersInfo(): String {
        val waterfallProviders = getWaterfallProviders()
        if (waterfallProviders.isEmpty()) {
            return "No waterfall providers configured"
        }
        
        return buildString {
            appendLine("Waterfall Providers (${waterfallProviders.size}):")
            waterfallProviders.forEachIndexed { index, (type, config) ->
                val status = if (providerPool.containsKey(type)) "✓ Ready" else "○ Not Initialized"
                appendLine("  [$index] $type - $status (AppID: ${config.appId})")
            }
        }.trimEnd()
    }

    fun getBiddingProvidersInfo(): String {
        val biddingProviders = getBiddingProviders()
        if (biddingProviders.isEmpty()) {
            return "No bidding providers configured"
        }
        
        return buildString {
            appendLine("Bidding Providers (${biddingProviders.size}):")
            biddingProviders.forEachIndexed { index, (type, config) ->
                val status = if (providerPool.containsKey(type)) "✓ Ready" else "○ Not Initialized"
                val timeout = config.biddingTimeout
                appendLine("  [$index] $type - $status | Timeout: ${timeout}ms")
            }
        }.trimEnd()
    }

    fun getVersion(): String = VERSION

    fun getCurrentProvider(): IAdProvider? = currentProvider

    fun getCurrentConfig(): AdProviderConfig? = currentConfig

    fun getProvider(providerType: String): IAdProvider? = providerPool[providerType]

    fun registerProvider(
        providerType: String,
        providerClass: Class<out IAdProvider>,
        config: AdProviderConfig
    ) {
        AdProviderFactory.registerProvider(providerType, providerClass, config)
        log("$TAG: Registered provider: $providerType" +
                " | AppID: ${config.appId}" +
                " | Waterfall: ${if (config.supportWaterfall) "ON" else "OFF"}" +
                " | Bidding: ${if (config.supportBidding) "ON" else "OFF"}")
    }

    fun getRegisteredProviders(): Set<String> = AdProviderFactory.getRegisteredProviders()
    
    fun getRegisteredProvidersInfo(): String = AdProviderFactory.getRegisteredProvidersInfo()

    private fun getActiveProvider(): IAdProvider? {
        val provider = currentProvider
        if (provider == null) {
            log("$TAG: No active provider. Call init() first.")
        }
        return provider
    }

    private fun getAdIdByType(adType: AdType): String? {
        return currentConfig?.getAdIdByType(adType)
    }

    private fun loadAdWithBidding(
        activity: Activity,
        adType: AdType,
        adId: String? = null,
        callback: IAdCallback? = null
    ) {
        log("$TAG: [BIDDING] Starting bidding for ${adType.name}")
        
        log("$TAG: [BIDDING] === BIDDING SIMULATION MODE ===")
        log("$TAG: [BIDDING] 1. All providers with supportBidding=true participate")
        log("$TAG: [BIDDING] 2. Each provider generates SIMULATED random price")
        log("$TAG: [BIDDING] 3. Price ranges vary by provider (for testing)")
        log("$TAG: [BIDDING] 4. Winner: provider with HIGHEST simulated eCPM wins")
        log("$TAG: [BIDDING] ===================================")
        
        val biddingProviders = getBiddingProviders()
        
        if (biddingProviders.isEmpty()) {
            logE("$TAG: [BIDDING] No providers with bidding support available")
            
            val activeProvider = getActiveProvider()
            if (activeProvider != null) {
                log("$TAG: [BIDDING] Falling back to single provider mode")
                loadAdSingle(activity, adType, adId, callback)
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
                activity = activity,
                adType = adType,
                customAdId = adId
            )
        }
        
        startBiddingTimeoutMonitor(biddingState)
    }

    private fun getBiddingProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportBidding }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private fun getBiddingTimeout(providers: List<Pair<String, AdProviderConfig>>): Long {
        if (providers.isEmpty()) return 5000L
        
        return providers.maxOf { (_, config) ->
            config.biddingTimeout.coerceAtLeast(1000L)
        }
    }

    private data class BiddingState(
        val totalProviders: Int,
        val startTime: Long,
        val timeout: Long,
        val results: ConcurrentHashMap<String, BiddingEntry>,
        val callback: IAdCallback?,
        val adType: AdType,
        var completedCount: Int = 0,
        var isFinished: Boolean = false
    )

    private data class BiddingEntry(
        val success: Boolean,
        val eCPM: Double = 0.0,
        val errorCode: Int = -1,
        val errorMessage: String = ""
    )

    private fun launchBiddingRequest(
        state: BiddingState,
        index: Int,
        providerType: String,
        provider: IAdProvider,
        config: AdProviderConfig,
        activity: Activity,
        adType: AdType,
        customAdId: String?
    ) {
        val actualAdId = customAdId ?: config.getAdIdByType(adType) ?: run {
            state.markFailed(providerType, -1, "No ad ID")
            return
        }
        
        log("$TAG: [BIDDING] [$index/${state.totalProviders}] Requesting: $providerType")
        
        provider.loadAd(activity, adType, actualAdId, object : IAdCallback {
            
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
            
            val priceInfo = if (success && eCPM > 0) {
                " | eCPM=\$${"%.2f".format(eCPM)} (SIMULATED)"
            } else if (success && eCPM == 0.0) {
                " | eCPM=\$0.00 (No simulation - fallback)"
            } else {
                ""
            }
            
            log("$TAG: [BIDDING] Response received: $providerType" +
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
        
        log("$TAG: [BIDDING] === BIDDING COMPLETED ===")
        log("$TAG: [BIDDING] Time elapsed: ${elapsedTime}ms")
        log("$TAG: [BIDDING] Total responses: ${state.completedCount}/${state.totalProviders}")
        
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
            
            log("$TAG: [BIDDING] 🏆 WINNER: ${result.providerType}" +
                    " | Price: \$${result.formattedPrice}" +
                    " | Source: $priceSource" +
                    " | Time: ${result.loadTime}ms")
            
            switchToProvider(winner.key)
            state.callback?.onAdLoadedWithPrice(result.eCPM)
            
        } else {
            logE("$TAG: [BIDDING] ❌ ALL PROVIDERS FAILED")
            state.callback?.onAdFail(-1, "All bidding providers failed")
        }
        
        logBiddingDetails(state)
    }

    private fun startBiddingTimeoutMonitor(state: BiddingState) {
        Handler(Looper.getMainLooper()).postDelayed({
            synchronized(state) {
                if (!state.isFinished) {
                    logW("$TAG: [BIDDING] ⏰ TIMEOUT! (${state.timeout}ms)")
                    logW("$TAG: [BIDDING] Completed: ${state.completedCount}/${state.totalProviders}")
                    
                    checkBiddingCompletion(state)
                }
            }
        }, state.timeout)
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

    private fun logW(message: String) {
        log(message)
    }
}
