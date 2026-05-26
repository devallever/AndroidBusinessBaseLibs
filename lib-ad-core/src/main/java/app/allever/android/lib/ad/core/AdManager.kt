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
    const val VERSION = "1.0.0"

    private var currentProvider: IAdProvider? = null
    private var currentConfig: AdProviderConfig? = null
    private var activeProviderType: String? = null
    
    private val providerPool = ConcurrentHashMap<String, IAdProvider>()
    private val initContexts = ConcurrentHashMap<String, Context>()

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
            initContexts[providerType] = context.applicationContext
            
            log("$TAG: Provider ${provider.getProviderType()} initialized successfully")
            
            switchToProvider(providerType)
            callback?.invoke()
        }
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
        val provider = getActiveProvider() ?: return
        
        val actualAdId = adId ?: getAdIdByType(adType) ?: run {
            log("$TAG: No ad ID provided for ${adType.name}")
            callback?.onAdFail(-1, "No ad ID provided")
            return
        }

        provider.loadAd(activity, adType, actualAdId, callback)
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
        initContexts.clear()
        
        currentProvider = null
        currentConfig = null
        activeProviderType = null
        
        log("$TAG: All providers destroyed")
    }

    fun destroyProvider(providerType: String) {
        providerPool[providerType]?.destroy()
        providerPool.remove(providerType)
        initContexts.remove(providerType)
        
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
        log("$TAG: Registered provider: $providerType with appId: ${config.appId}")
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
