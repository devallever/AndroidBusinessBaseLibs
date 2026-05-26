package app.allever.android.lib.ad.core.base

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

object AdProviderFactory {

    private const val TAG = "AdProviderFactory"

    private val providers = ConcurrentHashMap<String, Class<out IAdProvider>>()

    fun registerProvider(providerType: String, providerClass: Class<out IAdProvider>) {
        providers[providerType] = providerClass
        Log.d(TAG, "Registered ad provider: $providerType -> ${providerClass.simpleName}")
    }

    fun createProvider(providerType: String): IAdProvider? {
        val providerClass = providers[providerType] ?: run {
            Log.e(TAG, "No provider registered for type: $providerType")
            return null
        }

        return try {
            val instance = providerClass.getDeclaredConstructor().newInstance()
            Log.d(TAG, "Created ad provider: ${instance.getProviderType()}")
            instance
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create provider $providerType", e)
            null
        }
    }

    fun getRegisteredProviders(): Set<String> = providers.keys.toSet()

    fun isProviderRegistered(providerType: String): Boolean = providers.containsKey(providerType)
}
