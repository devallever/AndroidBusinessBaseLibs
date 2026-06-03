package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdCore.getActiveProvider
import app.allever.android.lib.ad.core.AdCore.getAdIdByType
import app.allever.android.lib.ad.core.AdCore.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import kotlin.collections.component1
import kotlin.collections.component2

class SingleModeStrategy : BaseModeStrategy() {

    override fun loadAd(
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {
        val provider = getActiveProvider()
        
        if (!requireProvider(provider, callback, isPreload = false)) {
            return
        }

        val actualAdId = getAdIdByType(adType)
        
        if (!requireAdId(actualAdId, adType, callback, isPreload = false)) {
            return
        }

        AdLog.logMessage("${adType.name} from current provider", adType = adType, strategyName = TAG,  action = "Loading")

        provider?.loadAd(context, adType, actualAdId!!, callback)
    }

    override fun preload(
        context: Context,
        adType: AdType
    ) {
        AdLog.logMessage("", strategyName = TAG, adType = adType, providerType = adManager.loadMode.name, action = "Preloading")

        val adId = getAdIdByType(adType)

        if (!requireAdId(adId, adType, null, isPreload = true)) {
            return
        }

        val provider = getActiveProvider()

        if (!requireProvider(provider, null, isPreload = true)) {
            return
        }

        AdLog.logMessage("Starting preload for ${adType.name}", adType = adType, strategyName = TAG, isPreload = true, providerType = adManager.loadMode.name, action = "Preloading")

        provider?.loadAd(context, adType, adId!!, object : IAdCallback {
            override fun onAdLoaded() {
                AdLog.logMessage("${adType.name} preloaded successfully and cached", adType = adType, strategyName = TAG, isPreload = true, success = true)
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                AdLog.logMessage("${adType.name} preload failed", adType = adType, strategyName = TAG, isPreload = true, success = false)
            }
        })
    }

    override fun checkCache(
        adType: AdType,
        callback: IAdCallback?
    ): Boolean {
        val provider = getActiveProvider()

        if (!requireProvider(provider, callback, isPreload = false)) {
            return false
        }

        if (provider!!.isReady(adType)) {
            AdLog.logMessage("Provider ${provider.getProviderType()} has valid cache", strategyName = TAG, adType = adType, providerType = provider.getProviderType(), action = "cache")

            switchToProvider(provider.getProviderType())

            return true
        }

        AdLog.logMessage("No valid cache in current provider", strategyName = TAG, adType = adType, providerType = provider.getProviderType(), action = "cache")
        return false
    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (type, _) -> type == adManager.getCurrentProvider()?.getProviderType() }
            .toList()
    }
}
