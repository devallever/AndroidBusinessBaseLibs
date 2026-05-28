package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.AdManager.LoadMode
import app.allever.android.lib.ad.core.AdManager.getActiveProvider
import app.allever.android.lib.ad.core.AdManager.loadMode
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

class WaterfallModeStrategy: BaseModeStrategy() {
    
    companion object {
        private const val TAG = "WaterfallModeStrategy"
    }
    override fun loadAd(
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {
        log("$TAG: [WATERFALL] Starting waterfall for ${adType.name}")

        val waterfallProviders = getProviders()

        if (waterfallProviders.isEmpty()) {
            logE("$TAG: [WATERFALL] No providers with waterfall support available")

            val activeProvider = getActiveProvider()
            if (activeProvider != null) {
                log("$TAG: [WATERFALL] Falling back to single provider mode")
                adManager.strategyPool[AdManager.LoadMode.SINGLE]?.loadAd(context, adType, callback)
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
            context = context,
            adType = adType,
            callback = callback
        )
    }

    override fun checkCache(
        adType: AdType,
        callback: IAdCallback?
    ): Boolean {
        val waterfallProviders = getProviders()

        for ((providerType, _) in waterfallProviders) {
            val provider = providerPool[providerType] ?: continue

            if (provider.isReady(adType)) {
                log("${TAG}: [CACHE-WATERFALL] ✅ Found cache in: $providerType (priority order)")

                //这个应该有必要切换
                switchToProvider(providerType)
                callback?.onAdLoaded()

                return true
            }
        }

        log("${TAG}: [CACHE-WATERFALL] No valid cache in any waterfall provider")
        return false
    }

    override fun preload(
        context: Context,
        adType: AdType
    ) {
        log("${TAG}: [PRELOAD-WATERFALL] Starting pre-waterfall for ${adType.name}")

        if (loadMode != LoadMode.WATERFALL) {
            logE("${TAG}: [PRELOAD-WATERFALL] ERROR: Current mode is not WATERFALL")
            return
        }

        val waterfallProviders = getProviders()

        if (waterfallProviders.isEmpty()) {
            logE("${TAG}: [PRELOAD-WATERFALL] No waterfall providers available")
            return
        }

        log("${TAG}: [PRELOAD-WATERFALL] Trying to preload from ${waterfallProviders.size} providers...")

        tryLoadFromWaterfall(
            providers = waterfallProviders,
            currentIndex = 0,
            context = context,
            adType = adType,
            callback = object : IAdCallback {
                override fun onAdLoaded() {
                    log("${TAG}: [PRELOAD-WATERFALL] ✅ Preload successful!")
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    log("${TAG}: [PRELOAD-WATERFALL] ❌ All providers failed")
                }
            }
        )
    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportWaterfall }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private fun tryLoadFromWaterfall(
        providers: List<Pair<String, AdProviderConfig>>,
        currentIndex: Int,
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {

        if (currentIndex >= providers.size) {
            logE("${TAG}: [WATERFALL] All ${providers.size} providers failed for ${adType.name}")
            callback?.onAdFail(-1, "All waterfall providers failed")
            return
        }

        val (providerType, config) = providers[currentIndex]
        val provider = providerPool[providerType]
        val adId = config.getAdIdByType(adType)

        if (provider == null) {
            log("${TAG}: [WATERFALL] [$currentIndex] Provider $providerType not in pool, skipping...")
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback)
            return
        }


        if (adId.isNullOrEmpty()) {
            log("${TAG}: [WATERFALL] [$currentIndex] No ad ID for $providerType/${adType.name}, skipping...")
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback)
            return
        }

        log("${TAG}: [WATERFALL] [$currentIndex/$providers.size] Trying: $providerType (ID: $adId)")

        provider.loadAd(context, adType, adId, object : IAdCallback {

            override fun onAdLoaded() {
                log("${TAG}: [WATERFALL] ✓ SUCCESS at [$currentIndex]: $providerType")

                switchToProvider(providerType)

                callback?.onAdLoaded()
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                log("${TAG}: [WATERFALL] ✗ FAILED at [$currentIndex]: $providerType - Error($errorCode): $errorMessage")

                tryLoadFromWaterfall(
                    providers,
                    currentIndex + 1,
                    context,
                    adType,
                    callback
                )
            }

            override fun onAdShow() {
                log("${TAG}: [WATERFALL] Ad shown from: $providerType")
                callback?.onAdShow()
            }

            override fun onAdClick() {
                log("${TAG}: [WATERFALL] Ad clicked from: $providerType")
                callback?.onAdClick()
            }

            override fun onAdDismiss() {
                log("${TAG}: [WATERFALL] Ad dismissed from: $providerType")
                callback?.onAdDismiss()
            }

            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                log("${TAG}: [WATERFALL] Rewarded from: $providerType - Amount: $rewardAmount, Name: $rewardName")
                callback?.onAdRewarded(rewardAmount, rewardName)
            }
        })
    }
}