package app.allever.android.lib.ad.core.base

import android.util.Log
import app.allever.android.lib.ad.core.config.AdProviderConfig
import java.util.concurrent.ConcurrentHashMap

object AdProviderFactory {

    private const val TAG = "AdProviderFactory"

    data class ProviderEntry(
        val providerClass: Class<out IAdProvider>,
        val config: AdProviderConfig
    )

    private val providers = ConcurrentHashMap<String, ProviderEntry>()

    fun registerProvider(providerType: String, providerClass: Class<out IAdProvider>, config: AdProviderConfig) {
        providers[providerType] = ProviderEntry(providerClass, config)
        Log.d(TAG, "Registered ad provider: $providerType -> ${providerClass.simpleName}")
    }

    fun createProvider(providerType: String): Pair<IAdProvider?, AdProviderConfig?> {
        val entry = providers[providerType] ?: run {
            Log.e(TAG, "No provider registered for type: $providerType")
            return null to null
        }

        val provider = try {
            val instance = entry.providerClass.getDeclaredConstructor().newInstance()
            Log.d(TAG, "Created ad provider: ${instance.getProviderType()}")
            instance
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create provider $providerType", e)
            return null to null
        }

        return provider to entry.config
    }

    fun getRegisteredProviders(): Set<String> = providers.keys.toSet()

    fun getRegisteredProvidersInfo(): String {
        return providers.keys.joinToString(", ") { "$it(${providers[it]?.config?.appId})" }
    }

    fun isProviderRegistered(providerType: String): Boolean = providers.containsKey(providerType)
    
    fun getConfig(providerType: String): AdProviderConfig? = providers[providerType]?.config
    
    fun getAllConfigs(): Map<String, AdProviderConfig> = 
        providers.mapValues { it.value.config }
}
