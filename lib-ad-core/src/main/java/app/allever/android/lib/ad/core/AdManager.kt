package app.allever.android.lib.ad.core

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.provider.MockAdProvider
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE

object AdManager {

    private const val TAG = "AdManager"
    const val VERSION = "1.0.0"

    private var currentProvider: IAdProvider? = null
    private var currentConfig: AdProviderConfig? = null
    private var isInitialized = false

    fun init(context: Context, providerType: String, callback: (() -> Unit)? = null) {
        if (isInitialized) {
            log("$TAG: AdManager already initialized")
            callback?.invoke()
            return
        }

        isInitialized = true

        val (provider, config) = AdProviderFactory.createProvider(providerType)
        if (provider == null || config == null) {
            logE("$TAG: Failed to create ad provider for type: $providerType. Make sure to register it first.")
            return
        }

        currentConfig = config
        currentProvider = provider

        provider.init(context, config) {
            log("$TAG: Ad SDK initialized with provider: ${provider.getProviderType()}")
            callback?.invoke()
        }
    }

    fun loadAd(
        activity: Activity,
        adType: AdType,
        adId: String? = null,
        callback: IAdCallback? = null
    ) {
        val provider = getProviderOrWarn() ?: return
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
        val provider = getProviderOrWarn() ?: return
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
        currentProvider?.destroy()
        currentProvider = null
        currentConfig = null
        isInitialized = false
        log("$TAG: AdManager destroyed")
    }

    fun isInitialized(): Boolean = isInitialized

    fun getVersion(): String = VERSION

    fun getCurrentProvider(): IAdProvider? = currentProvider

    fun getCurrentConfig(): AdProviderConfig? = currentConfig

    fun registerProvider(
        providerType: String,
        providerClass: Class<out IAdProvider>,
        config: AdProviderConfig
    ) {
        AdProviderFactory.registerProvider(providerType, providerClass, config)
        log("$TAG: Registered provider: $providerType with appId: ${config.appId}")
    }

    private fun getProviderOrWarn(): IAdProvider? {
        val provider = currentProvider
        if (provider == null) {
            log("$TAG: Ad not initialized, please call init() first")
        }
        return provider
    }

    private fun getAdIdByType(adType: AdType): String? {
        return currentConfig?.getAdIdByType(adType)
    }
}
