package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdManager.getActiveProvider
import app.allever.android.lib.ad.core.AdManager.getAdIdByType
import app.allever.android.lib.ad.core.AdManager.providerPool
import app.allever.android.lib.ad.core.AdManager.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import kotlin.collections.component1
import kotlin.collections.component2

class SingleModeStrategy : BaseModeStrategy() {

    override fun loadAd(
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {
        val provider = getActiveProvider()
        
        if (!requireProvider(provider, callback, isPreload = false, prefix = AdLog.PREFIX_SINGLE)) {
            return
        }

        val actualAdId = getAdIdByType(adType)
        
        if (!requireAdId(actualAdId, adType, callback, isPreload = false, prefix = AdLog.PREFIX_SINGLE)) {
            return
        }

        logAction(AdLog.PREFIX_SINGLE, "Loading", "${adType.name} from current provider")

        provider?.loadAd(context, adType, actualAdId!!, callback)
    }

    override fun preload(
        context: Context,
        adType: AdType
    ) {
        logAction(AdLog.PREFIX_PRELOAD, "Starting", "pre-${adType.name}", isPreload = true)

        val adId = getAdIdByType(adType)

        if (!requireAdId(adId, adType, null, isPreload = true, prefix = AdLog.PREFIX_SINGLE)) {
            return
        }

        val provider = getActiveProvider()

        if (!requireProvider(provider, null, isPreload = true, prefix = AdLog.PREFIX_SINGLE)) {
            return
        }

        logAction(AdLog.PREFIX_SINGLE, "Preloading", "${adType.name} from current provider (mode: ${adManager.loadMode.name})", isPreload = true)

        provider?.loadAd(context, adType, adId!!, object : IAdCallback {
            override fun onAdLoaded() {
                logSuccess(AdLog.PREFIX_SINGLE, "${adType.name} preloaded successfully and cached", isPreload = true)
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                logError(AdLog.PREFIX_SINGLE, "${adType.name} preload failed: $errorMessage", isPreload = true)
            }
        })
    }

    override fun checkCache(
        adType: AdType,
        callback: IAdCallback?
    ): Boolean {
        val provider = getActiveProvider()

        if (!requireProvider(provider, callback, isPreload = false, prefix = AdLog.PREFIX_CACHE)) {
            return false
        }

        if (provider!!.isReady(adType)) {
            logAction(AdLog.PREFIX_CACHE, "Provider ${provider.getProviderType()} has valid cache")

            switchToProvider(provider.getProviderType())

            return true
        }

        log(AdLog.format(TAG, AdLog.PREFIX_CACHE, "No valid cache in current provider"))
        return false
    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (type, _) -> type == adManager.getCurrentProvider()?.getProviderType() }
            .toList()
    }
}
