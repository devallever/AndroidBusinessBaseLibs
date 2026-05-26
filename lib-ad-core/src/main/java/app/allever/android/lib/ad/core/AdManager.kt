package app.allever.android.lib.ad.core

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import java.util.concurrent.ConcurrentHashMap

object AdManager {

    private const val TAG = "AdManager"
    const val VERSION = "1.1.0"

    enum class LoadMode {
        SINGLE,
        WATERFALL
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
                " | Waterfall: ${if (config.supportWaterfall) "ON" else "OFF"}")
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
}
