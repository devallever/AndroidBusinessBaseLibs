package app.allever.android.lib.ad.core

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.base.BaseAdProvider
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

    fun init(context: Context, adConfig: AdProviderConfig, callback: (() -> Unit)? = null) {
        if (isInitialized) {
            log("$TAG: AdManager already initialized")
            callback?.invoke()
            return
        }

        registerDefaultProviders()
        isInitialized = true

        if (adConfig.adProviderType.isEmpty()) {
            log("$TAG: Ad provider type is empty, skip initialization")
            return
        }

        currentConfig = adConfig

        val provider = AdProviderFactory.createProvider(adConfig.adProviderType)
        if (provider == null) {
            logE("$TAG: Failed to create ad provider for type: ${adConfig.adProviderType}")
            return
        }

        currentProvider = provider

        provider.init(context, adConfig) {
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

    fun registerProvider(providerType: String, providerClass: Class<out IAdProvider>) {
        AdProviderFactory.registerProvider(providerType, providerClass)
        log("$TAG: Registered custom provider: $providerType")
    }

    private fun registerDefaultProviders() {
        AdProviderFactory.registerProvider(MockAdProvider.PROVIDER_NAME, MockAdProvider::class.java)
    }

    private fun getProviderOrWarn(): IAdProvider? {
        val provider = currentProvider
        if (provider == null) {
            log("$TAG: Ad not initialized, please call init() first")
        }
        return provider
    }

    private fun getAdIdByType(adType: AdType): String? {
        return when (adType) {
            AdType.SPLASH -> currentConfig?.splashAdId
            AdType.INTERSTITIAL -> currentConfig?.interstitialAdId
            AdType.REWARD_VIDEO -> currentConfig?.rewardVideoAdId
            AdType.BANNER -> currentConfig?.bannerAdId
            AdType.NATIVE -> currentConfig?.nativeAdId
        }
    }
}
