package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.AdManager.getActiveProvider
import app.allever.android.lib.ad.core.AdManager.getAdIdByType
import app.allever.android.lib.ad.core.AdManager.providerPool
import app.allever.android.lib.ad.core.AdManager.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import kotlin.collections.component1
import kotlin.collections.component2

class SingleModeStrategy: BaseModeStrategy() {
    
    companion object {
        private const val TAG = "SingleModeStrategy"
    }
    override fun loadAd(
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {
        val provider = adManager.getActiveProvider() ?: return

        val actualAdId = adManager.getAdIdByType(adType) ?: run {
            log("$TAG: No ad ID provided for ${adType.name}")
            callback?.onAdFail(-1, "No ad ID provided")
            return
        }

        provider.loadAd(context, adType, actualAdId, callback)
    }

    override fun preload(
        context: Context,
        adType: AdType
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

    override fun checkCache(
        adType: AdType,
        callback: IAdCallback?
    ): Boolean {
        val provider = getActiveProvider() ?: run {
            logE("${TAG}: [CACHE-SINGLE] No active provider, cannot check cache")
            return false
        }

        if (provider.isReady(adType)) {
            log("${TAG}: [CACHE-SINGLE] Provider ${provider.getProviderType()} has valid cache")

            //TODO CHECK 没必要切换了吧
            switchToProvider(provider.getProviderType())
            callback?.onAdLoaded()

            return true
        }

        log("${TAG}: [CACHE-SINGLE] No valid cache in current provider")
        return false
    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (type, _) -> type == adManager.getCurrentProvider()?.getProviderType() }
            .toList()
    }
}