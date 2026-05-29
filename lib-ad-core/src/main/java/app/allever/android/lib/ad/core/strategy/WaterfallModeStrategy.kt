package app.allever.android.lib.ad.core.strategy

import android.content.Context
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

class WaterfallModeStrategy : BaseModeStrategy() {

    companion object {
        private const val TAG = "WaterfallModeStrategy"
    }

    override fun loadAd(
        context: Context,
        adType: AdType,
        callback: IAdCallback?
    ) {
        executeWaterfall(
            context = context,
            adType = adType,
            callback = callback,
            isPreload = false,
            logPrefix = "[WATERFALL]",
            checkMode = false
        )
    }

    override fun preload(
        context: Context,
        adType: AdType
    ) {
        executeWaterfall(
            context = context,
            adType = adType,
            callback = null,
            isPreload = true,
            logPrefix = "[PRELOAD-WATERFALL]",
            checkMode = true
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

                switchToProvider(providerType)
                callback?.onAdLoaded()

                return true
            }
        }

        log("${TAG}: [CACHE-WATERFALL] No valid cache in any waterfall provider")
        return false
    }

    override fun getProviders(): List<Pair<String, AdProviderConfig>> {
        return AdProviderFactory.getAllConfigs()
            .filter { (_, config) -> config.supportWaterfall }
            .filter { (type, _) -> providerPool.containsKey(type) }
            .toList()
    }

    private fun executeWaterfall(
        context: Context,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean,
        logPrefix: String,
        checkMode: Boolean = false
    ) {
        log("$TAG: $logPrefix Starting ${if (isPreload) "pre-" else ""}waterfall for ${adType.name}")

        if (checkMode && loadMode != LoadMode.WATERFALL) {
            logE("$TAG: $logPrefix ERROR: Current mode is ${loadMode.name}, not WATERFALL")
            return
        }

        val waterfallProviders = getProviders()

        if (waterfallProviders.isEmpty()) {
            logE("$TAG: $logPrefix No providers with waterfall support available")

            if (!isPreload && callback != null) {
                val activeProvider = getActiveProvider()
                if (activeProvider != null) {
                    log("$TAG: $logPrefix Falling back to single provider mode")
                    adManager.strategyPool[LoadMode.SINGLE]?.loadAd(context, adType, callback)
                } else {
                    callback.onAdFail(-1, "No available providers for waterfall")
                }
            }
            return
        }

        val actionWord = if (isPreload) "preload from" else ""
        log("$TAG: $logPrefix Trying to $actionWord ${waterfallProviders.size} providers...")

        if (!isPreload) {
            val waterfallOrder = waterfallProviders.joinToString(" → ") { it.first }
            log("$TAG: $logPrefix Order: $waterfallOrder (${waterfallProviders.size} providers)")
        }

        tryLoadFromWaterfall(
            providers = waterfallProviders,
            currentIndex = 0,
            context = context,
            adType = adType,
            callback = callback,
            isPreload = isPreload,
            logPrefix = logPrefix
        )
    }

    private fun tryLoadFromWaterfall(
        providers: List<Pair<String, AdProviderConfig>>,
        currentIndex: Int,
        context: Context,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean = false,
        logPrefix: String = "[WATERFALL]"
    ) {

        if (currentIndex >= providers.size) {
            logE("$TAG: $logPrefix All ${providers.size} providers failed for ${adType.name}")

            if (!isPreload) {
                callback?.onAdFail(-1, "All waterfall providers failed")
            } else {
                logE("$TAG: $logPrefix ❌ All providers failed")
            }
            return
        }

        val (providerType, config) = providers[currentIndex]
        val provider = providerPool[providerType]
        val adId = config.getAdIdByType(adType)

        if (provider == null) {
            log("$TAG: $logPrefix [$currentIndex] Provider $providerType not in pool, skipping...")
            tryLoadFromWaterfall(
                providers,
                currentIndex + 1,
                context,
                adType,
                callback,
                isPreload,
                logPrefix
            )
            return
        }


        if (adId.isNullOrEmpty()) {
            log("$TAG: $logPrefix [$currentIndex] No ad ID for $providerType/${adType.name}, skipping...")
            tryLoadFromWaterfall(
                providers,
                currentIndex + 1,
                context,
                adType,
                callback,
                isPreload,
                logPrefix
            )
            return
        }

        log("$TAG: $logPrefix [$currentIndex/$providers.size] Trying: $providerType (ID: $adId)")

        provider.loadAd(context, adType, adId, object : IAdCallback {

            override fun onAdLoaded() {
                log("$TAG: $logPrefix ✓ SUCCESS at [$currentIndex]: $providerType")

                switchToProvider(providerType)

                if (isPreload) {
                    log("$TAG: $logPrefix ✅ Preload successful!")
                } else {
                    callback?.onAdLoaded()
                }
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                log("$TAG: $logPrefix ✗ FAILED at [$currentIndex]: $providerType - Error($errorCode): $errorMessage")

                tryLoadFromWaterfall(
                    providers,
                    currentIndex + 1,
                    context,
                    adType,
                    callback,
                    isPreload,
                    logPrefix
                )
            }

            override fun onAdShow() {
                log("$TAG: $logPrefix Ad shown from: $providerType")
                callback?.onAdShow()
            }

            override fun onAdClick() {
                log("$TAG: $logPrefix Ad clicked from: $providerType")
                callback?.onAdClick()
            }

            override fun onAdDismiss() {
                log("$TAG: $logPrefix Ad dismissed from: $providerType")
                callback?.onAdDismiss()
            }

            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                log("$TAG: $logPrefix Rewarded from: $providerType - Amount: $rewardAmount, Name: $rewardName")
                callback?.onAdRewarded(rewardAmount, rewardName)
            }
        })
    }
}
