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

    enum class LoadMode {
        SINGLE,
        WATERFALL,
        BIDDING
    }

    private var currentProvider: IAdProvider? = null
    private var currentConfig: AdProviderConfig? = null
    private var activeProviderType: String? = null

    /**
     * 缓存每个注册的AdProvider
     */
    private val providerPool = ConcurrentHashMap<String, IAdProvider>()

    @Volatile
    var loadMode: LoadMode = LoadMode.SINGLE
        private set

    @Volatile
    var cacheFirstEnabled: Boolean = true
        set(value) {
            field = value
            log("$TAG: Cache-first mode: ${if (value) "ENABLED" else "DISABLED"}")
        }

    /**
     * 注册AdProvider
     */
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

    /**
     * 初始化AdProvider
     */
    fun init(context: Context, providerType: String, forceReinit: Boolean = false, callback: (() -> Unit)? = null) {
        log("$TAG: Initializing provider: $providerType (forceReinit=$forceReinit)")

        //没有注册provider是不可以初始化的
        if (!AdProviderFactory.isProviderRegistered(providerType)) {
            logE("$TAG: Provider $providerType not registered. Call registerProvider() first.")
            return
        }

        val existingProvider = providerPool[providerType]

        if (existingProvider != null) {
            if (forceReinit) {
                log("$TAG: Force re-initializing provider: $providerType")
                existingProvider.destroy()
                providerPool.remove(providerType)
            } else {
                log("$TAG: Provider $providerType already initialized, switching to it")
                switchToProvider(providerType)
                callback?.invoke()
                return
            }
        }

        val config = AdProviderFactory.getConfig(providerType)
        if (config == null) {
            logE("$TAG: No config found for provider: $providerType")
            return
        }

        val (provider, _) = AdProviderFactory.createProvider(providerType)
        if (provider == null) {
            logE("$TAG: Failed to create provider instance for: $providerType")
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

    /**
     * 切换到指定AdProvider
     */
    fun switchToProvider(providerType: String): Boolean {
        if (!providerPool.containsKey(providerType)) {
            logE("$TAG: Cannot switch to $providerType, not initialized")
            return false
        }

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

    /**
     * 加载广告
     */
    fun loadAd(
        context: Context,
        adType: AdType,
        callback: IAdCallback? = null
    ) {
        if (cacheFirstEnabled) {
            log("$TAG: [CACHE] Checking cache before loading ${adType.name}...")

            val cacheResult = checkAndUseCache(adType, callback)

            if (cacheResult) {
                log("$TAG: [CACHE] ✅ Cache HIT! Using cached ad for ${adType.name}")
                log("$TAG: [CACHE] 📌 Note: Next ad will be preloaded after this one is dismissed")
                return
            }

            log("$TAG: [CACHE] ❌ Cache MISS or expired for ${adType.name}")
            log("$TAG: [CACHE] Proceeding with normal loading process...")
        } else {
            log("$TAG: [CACHE] Cache-first mode DISABLED, skipping cache check")
        }

        when (loadMode) {
            LoadMode.SINGLE -> {
                loadAdSingle(context, adType, callback)
            }
            LoadMode.WATERFALL -> {
                loadAdWithWaterfall(context, adType, callback)
            }
            LoadMode.BIDDING -> {
                loadAdWithBidding(context, adType, callback)
            }
        }
    }

    fun preloadForSingle(
        context: Context,
        adType: AdType,
    ) {
        val adId = getAdIdByType(adType) ?: run {
            log("${TAG}: No cached adId for ${adType.name}, cannot preload")
            return
        }

        log("${TAG}: Preloading ${adType.name} from current provider (mode: ${AdManager.loadMode.name})")

        val provider = getActiveProvider()?: run {
            logE("${TAG}: No active provider, cannot preload")
            return
        }
        provider.loadAd(context, adType, adId, object : IAdCallback {
            override fun onAdLoaded() {
                log("${TAG}: ${adType.name} preloaded successfully and cached")
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                log("${TAG}: ${adType.name} preload failed: $errorMessage")
//                (provider as? BaseAdProvider)?.removeCachedAd(adType)
            }
        })
    }

    fun preloadForWaterfall(context: Context, adType: AdType) {
        log("$TAG: [PRELOAD-WATERFALL] Starting pre-waterfall for ${adType.name}")

        if (loadMode != LoadMode.WATERFALL) {
            logE("$TAG: [PRELOAD-WATERFALL] ERROR: Current mode is not WATERFALL")
            return
        }

        val waterfallProviders = getWaterfallProviders()

        if (waterfallProviders.isEmpty()) {
            logE("$TAG: [PRELOAD-WATERFALL] No waterfall providers available")
            return
        }

        log("$TAG: [PRELOAD-WATERFALL] Trying to preload from ${waterfallProviders.size} providers...")

        tryLoadFromWaterfall(
            providers = waterfallProviders,
            currentIndex = 0,
            context = context,
            adType = adType,
            callback = object : IAdCallback {
                override fun onAdLoaded() {
                    log("$TAG: [PRELOAD-WATERFALL] ✅ Preload successful!")
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    log("$TAG: [PRELOAD-WATERFALL] ❌ All providers failed")
                }
            }
        )
    }

    fun preloadForBidding(context: Context, adType: AdType) {
        log("$TAG: [PRELOAD-BIDDING] Starting pre-bidding for ${adType.name}")
        log("$TAG: [PRELOAD-BIDDING] Purpose: Re-bid after ad dismiss to find new winner")
        log("$TAG: [PRELOAD-BIDDING] Mode: ${loadMode.name} (must be BIDDING)")

        if (loadMode != LoadMode.BIDDING) {
            logE("$TAG: [PRELOAD-BIDDING] ERROR: Current mode is ${loadMode.name}, not BIDDING")
            return
        }

        val biddingProviders = getBiddingProviders()

        if (biddingProviders.isEmpty()) {
            logE("$TAG: [PRELOAD-BIDDING] No bidding providers available")
            return
        }

        log("$TAG: [PRELOAD-BIDDING] Parallel requesting ${biddingProviders.size} providers...")

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
                log("$TAG: [PRELOAD-BIDDING] [$index] $providerType not initialized, skip")
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

    fun showAd(
        activity: Activity,
        adType: AdType,
        container: ViewGroup? = null,
        callback: IAdCallback? = null
    ) {
        val provider = getActiveProvider() ?: return
        provider.showAd(activity, adType, container, callback)
    }

    fun loadAndShow(
        activity: Activity,
        adType: AdType,
        container: ViewGroup? = null,
        callback: IAdCallback? = null
    ) {
        loadAd(activity, adType, object : IAdCallback {
            override fun onAdLoaded() {
                showAd(activity, adType, container, callback)
                callback?.onAdLoaded()
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                callback?.onAdFail(errorCode, errorMessage)
            }
        })
    }

    //TODO CHECK 检查是否有必要
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

    fun isReady(adType: AdType): Boolean {
        return currentProvider?.isReady(adType) ?: false
    }

    //todo check 单凭判断当前初始化状态，是否会误判
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

    fun getCurrentProvider(): IAdProvider? = currentProvider

    fun getCurrentConfig(): AdProviderConfig? = currentConfig

    fun getProvider(providerType: String): IAdProvider? = providerPool[providerType]

    fun getRegisteredProviders(): Set<String> = AdProviderFactory.getRegisteredProviders()

    fun getRegisteredProvidersInfo(): String = AdProviderFactory.getRegisteredProvidersInfo()

    //检查缓存///////////////////////////////////////////////////////////////////////////////////////
    private fun checkAndUseCache(adType: AdType, callback: IAdCallback?): Boolean {
        return when (loadMode) {
            LoadMode.SINGLE -> checkSingleCache(adType, callback)
            LoadMode.WATERFALL -> checkWaterfallCache(adType, callback)
            LoadMode.BIDDING -> checkBiddingCache(adType, callback)
        }
    }

    private fun checkSingleCache(adType: AdType, callback: IAdCallback?): Boolean {
        val provider = getActiveProvider() ?: run {
            logE("$TAG: [CACHE-SINGLE] No active provider, cannot check cache")
            return false
        }

        if (provider.isReady(adType)) {
            log("$TAG: [CACHE-SINGLE] Provider ${provider.getProviderType()} has valid cache")

            //TODO CHECK 没必要切换了吧
            switchToProvider(provider.getProviderType())
            callback?.onAdLoaded()

            return true
        }

        log("$TAG: [CACHE-SINGLE] No valid cache in current provider")
        return false
    }

    private fun checkWaterfallCache(adType: AdType, callback: IAdCallback?): Boolean {
        val waterfallProviders = getWaterfallProviders()

        for ((providerType, _) in waterfallProviders) {
            val provider = providerPool[providerType] ?: continue

            if (provider.isReady(adType)) {
                log("$TAG: [CACHE-WATERFALL] ✅ Found cache in: $providerType (priority order)")

                //这个应该有必要切换
                switchToProvider(providerType)
                callback?.onAdLoaded()

                return true
            }
        }

        log("$TAG: [CACHE-WATERFALL] No valid cache in any waterfall provider")
        return false
    }

    private fun checkBiddingCache(adType: AdType, callback: IAdCallback?): Boolean {
        //这需要竞价成功后，切换provider才行
        val activeProvider = getActiveProvider() ?: return false

        if (activeProvider.isReady(adType)) {
            val providerType = activeProvider.getProviderType()
            log("$TAG: [CACHE-BIDDING] ✅ Using last bidding winner cache: $providerType")
            log("$TAG: [CACHE-BIDDING] Note: This was the winner from previous bidding round")

            callback?.onAdLoaded()

            return true
        }

        log("$TAG: [CACHE-BIDDING] No valid cache from previous bidding winner")
        return false
    }
    //检查缓存///////////////////////////////////////////////////////////////////////////////////////

    //加载广告/////////////////////////////////////////////////////////////////////////////
    private fun loadAdSingle(
        context: Context,
        adType: AdType,
        callback: IAdCallback? = null
    ) {
        val provider = getActiveProvider() ?: return

        val actualAdId = getAdIdByType(adType) ?: run {
            log("$TAG: No ad ID provided for ${adType.name}")
            callback?.onAdFail(-1, "No ad ID provided")
            return
        }

        provider.loadAd(context, adType, actualAdId, callback)
    }

    private fun loadAdWithWaterfall(
        context: Context,
        adType: AdType,
        callback: IAdCallback? = null
    ) {
        log("$TAG: [WATERFALL] Starting waterfall for ${adType.name}")

        val waterfallProviders = getWaterfallProviders()

        if (waterfallProviders.isEmpty()) {
            logE("$TAG: [WATERFALL] No providers with waterfall support available")

            val activeProvider = getActiveProvider()
            if (activeProvider != null) {
                log("$TAG: [WATERFALL] Falling back to single provider mode")
                loadAdSingle(context, adType, callback)
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
            context = context,
            adType = adType,
            callback = callback
        )
    }

    private fun loadAdWithBidding(
        context: Context,
        adType: AdType,
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
            // 降为请求单个广告源
            val activeProvider = getActiveProvider()
            if (activeProvider != null) {
                log("$TAG: [BIDDING] Falling back to single provider mode")
                loadAdSingle(context, adType, callback)
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
    //加载广告/////////////////////////////////////////////////////////////////////////////

    //瀑布流相关/////////////////////////////////////////////////////////////////////////////
    private fun getWaterfallProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportWaterfall }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private fun tryLoadFromWaterfall(
        providers: List<Pair<String, AdProviderConfig>>,
        currentIndex: Int,
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {

        if (currentIndex >= providers.size) {
            logE("$TAG: [WATERFALL] All ${providers.size} providers failed for ${adType.name}")
            callback?.onAdFail(-1, "All waterfall providers failed")
            return
        }

        val (providerType, config) = providers[currentIndex]
        val provider = providerPool[providerType]
        val adId = config.getAdIdByType(adType)

        if (provider == null) {
            log("$TAG: [WATERFALL] [$currentIndex] Provider $providerType not in pool, skipping...")
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback)
            return
        }


        if (adId.isNullOrEmpty()) {
            log("$TAG: [WATERFALL] [$currentIndex] No ad ID for $providerType/${adType.name}, skipping...")
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback)
            return
        }

        log("$TAG: [WATERFALL] [$currentIndex/$providers.size] Trying: $providerType (ID: $adId)")

        provider.loadAd(context, adType, adId, object : IAdCallback {

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
                    context,
                    adType,
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
    //瀑布流相关/////////////////////////////////////////////////////////////////////////////


    //竞价相关/////////////////////////////////////////////////////////////////////////////////////////
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

            log("$TAG: $modeTag Response received: $providerType" +
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

        log("$TAG: $modeTag === ${if (state.isPreload) "PRE-LOAD" else "BIDDING"} COMPLETED ===")
        log("$TAG: $modeTag Time elapsed: ${elapsedTime}ms")
        log("$TAG: $modeTag Total responses: ${state.completedCount}/${state.totalProviders}")

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

            log("$TAG: $modeTag 🏆 $actionLabel: ${result.providerType}" +
                    " | Price: \$${result.formattedPrice}" +
                    " | Source: $priceSource" +
                    " | Time: ${result.loadTime}ms")

            switchToProvider(winner.key)

            if (state.isPreload) {
                log("$TAG: $modeTag ✅ Preload successful! Next ad will use: ${winner.key}")
                log("$TAG: $modeTag 📦 Ad cached and ready for next show()")
            } else {
                state.callback?.onAdLoadedWithPrice(result.eCPM)
            }

        } else {
            logE("$TAG: $modeTag ❌ ALL PROVIDERS FAILED")
            if (!state.isPreload) {
                state.callback?.onAdFail(-1, "All bidding providers failed")
            } else {
                logE("$TAG: $modeTag ⚠️  Preload failed - no ad available for next request")
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
                    log("$TAG: $modeTag ⏰ TIMEOUT! (${state.timeout}ms)")
                    log("$TAG: $modeTag Completed: ${state.completedCount}/${state.totalProviders}")

                    checkBiddingCompletion(state)
                }
            }
        }, state.timeout)
    }

    private fun getBiddingTimeout(providers: List<Pair<String, AdProviderConfig>>): Long {
        if (providers.isEmpty()) return 5000L

        return providers.maxOf { (_, config) ->
            config.biddingTimeout.coerceAtLeast(1000L)
        }
    }

    private fun getBiddingProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportBidding }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
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
            logE("$TAG: $TAG [$index/${state.totalProviders}] ERROR: No ad ID")
            return
        }

        val modeTag = if (state.isPreload) "[PRELOAD-BIDDING]" else "[BIDDING]"

        log("$TAG: $modeTag [$index/${state.totalProviders}] Requesting: $providerType")

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
    //竞价相关/////////////////////////////////////////////////////////////////////////////////////////

    private fun getAdIdByType(adType: AdType): String? {
        return currentConfig?.getAdIdByType(adType)
    }

    private fun getActiveProvider(): IAdProvider? {
        val provider = currentProvider
        if (provider == null) {
            logE("$TAG: No active provider. Call init() first.")
        }
        return provider
    }
}
