package app.allever.android.lib.ad.core

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.strategy.BiddingModeStrategy
import app.allever.android.lib.ad.core.strategy.ILoadModeStrategy
import app.allever.android.lib.ad.core.strategy.SingleModeStrategy
import app.allever.android.lib.ad.core.strategy.WaterfallModeStrategy
import app.allever.android.lib.ad.core.type.AdType
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

    @Volatile
    private var currentProvider: IAdProvider? = null
    @Volatile
    private var currentConfig: AdProviderConfig? = null
    private var activeProviderType: String? = null

    /**
     * 缓存每个注册的AdProvider
     */
    internal val providerPool = ConcurrentHashMap<String, IAdProvider>()

    @Volatile
    internal var currentStrategy: ILoadModeStrategy = SingleModeStrategy()
        private set

    internal val strategyPool = ConcurrentHashMap<LoadMode, ILoadModeStrategy>()

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
        currentStrategy = when (mode) {
            LoadMode.SINGLE -> {
                strategyPool[mode] ?: SingleModeStrategy()
            }

            LoadMode.WATERFALL -> {
                strategyPool[mode] ?: WaterfallModeStrategy()
            }

            LoadMode.BIDDING -> {
                strategyPool[mode] ?: BiddingModeStrategy()
            }
        }

        strategyPool[mode] = currentStrategy
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
                callback?.onAdLoaded()
                return
            }

            log("$TAG: [CACHE] ❌ Cache MISS or expired for ${adType.name}")
            log("$TAG: [CACHE] Proceeding with normal loading process...")
        } else {
            log("$TAG: [CACHE] Cache-first mode DISABLED, skipping cache check")
        }

        currentStrategy.loadAd(context, adType, callback)
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
        val waterfallProviders = currentStrategy.getProviders()
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
        val biddingProviders = strategyPool[LoadMode.BIDDING]?.getProviders()?:return ""
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

    fun getRegisteredProvidersInfo(): String = AdProviderFactory.getRegisteredProvidersInfo()

    private fun checkAndUseCache(adType: AdType, callback: IAdCallback?): Boolean {
        return currentStrategy.checkCache(adType, callback)
    }

    fun getAdIdByType(adType: AdType): String? {
        return currentConfig?.getAdIdByType(adType)
    }

    fun getActiveProvider(): IAdProvider? {
        val provider = currentProvider
        if (provider == null) {
            logE("$TAG: No active provider. Call init() first.")
        }
        return provider
    }
}
