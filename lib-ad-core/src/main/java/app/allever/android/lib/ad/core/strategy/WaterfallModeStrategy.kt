package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdCore.LoadMode
import app.allever.android.lib.ad.core.AdCore.providerPool
import app.allever.android.lib.ad.core.AdCore.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import kotlin.collections.component1
import kotlin.collections.component2

class WaterfallModeStrategy : BaseModeStrategy() {

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
            checkMode = false
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
                AdLog.logMessage("Found cache in $providerType", strategyName = TAG, adType = adType, providerType = providerType)
                switchToProvider(providerType, adType)
                return true
            }
        }

        AdLog.logMessage("No cache found in any waterfall provider", strategyName = TAG, adType = adType)
        return false
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
            checkMode = true
        )
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
        checkMode: Boolean = false
    ) {
        AdLog.logMessage("Starting ${if (isPreload) "pre-" else ""}waterfall", strategyName = TAG, adType = adType, isPreload = isPreload)

        if (checkMode && !checkLoadMode(LoadMode.WATERFALL, isPreload)) {
            return
        }

        val waterfallProviders = getProviders()

        if (waterfallProviders.isEmpty()) {
            AdLog.logMessage("No providers with waterfall support available", strategyName = TAG, adType = adType, isPreload = isPreload, success = false)

            if (!isPreload) {
                fallbackToSingle(context, adType, callback, false)
            }
            return
        }

        val actionWord = if (isPreload) "preload from" else ""
        AdLog.logMessage("Trying to $actionWord${waterfallProviders.size} providers", strategyName = TAG, adType = adType, isPreload = isPreload)

        if (!isPreload) {
            val waterfallOrder = waterfallProviders.joinToString(" → ") { it.first }
            AdLog.logMessage("Order: $waterfallOrder (${waterfallProviders.size} providers)", strategyName = TAG, adType = adType, isPreload = false)
        }

        tryLoadFromWaterfall(
            providers = waterfallProviders,
            currentIndex = 0,
            context = context,
            adType = adType,
            callback = callback,
            isPreload = isPreload,
        )
    }

    private fun tryLoadFromWaterfall(
        providers: List<Pair<String, AdProviderConfig>>,
        currentIndex: Int,
        context: Context,
        adType: AdType,
        callback: IAdCallback?,
        isPreload: Boolean = false,
    ) {

        if (currentIndex >= providers.size) {
            AdLog.logMessage("All ${providers.size} providers failed for ${adType.name}", strategyName = TAG, adType = adType, isPreload = isPreload, success = false)

            if (!isPreload) {
                callback?.onAdFail(-1, "All waterfall providers failed")
            } else {
                AdLog.logMessage("All providers failed", strategyName = TAG, adType = adType, isPreload = true, success = false)
            }
            return
        }

        val (providerType, config) = providers[currentIndex]
        val provider = providerPool[providerType]
        val adId = config.getAdIdByType(adType)

        if (provider == null) {
            AdLog.logMessage("[$currentIndex] Provider $providerType not in pool, skipping...", strategyName = TAG, adType = adType, isPreload = isPreload, success = false)
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback, isPreload)
            return
        }


        if (adId.isNullOrEmpty()) {
            AdLog.logMessage("[$currentIndex] No ad ID for $providerType/${adType.name}", strategyName = TAG, adType = adType, isPreload = isPreload, success = false)
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback, isPreload)
            return
        }

        AdLog.logMessage("[$currentIndex/$providers.size] Trying: $providerType (ID: $adId)", strategyName = TAG, adType = adType, isPreload = isPreload, success = false)

        provider.loadAd(context, adType, adId, object : IAdCallback {

            override fun onAdLoaded() {
                AdLog.logMessage("SUCCESS at [$currentIndex]: $providerType successful!", strategyName = TAG, adType = adType, isPreload = isPreload, success = true)

                switchToProvider(providerType, adType)

                if (!isPreload) {
                    callback?.onAdLoaded()
                }
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                //ADL
                AdLog.logMessage("FAILED at [$currentIndex]: $providerType - Error($errorCode): $errorMessage", strategyName = TAG, adType = adType, isPreload = isPreload, success = false)

                tryLoadFromWaterfall(
                    providers,
                    currentIndex + 1,
                    context,
                    adType,
                    callback,
                    isPreload
                )
            }

            override fun onAdShow() {
                AdLog.logMessage("Ad shown from: $providerType", strategyName = TAG, adType = adType, isPreload = isPreload)
                callback?.onAdShow()
            }

            override fun onAdClick() {
                AdLog.logMessage("Ad clicked from: $providerType", strategyName = TAG, adType = adType, isPreload = isPreload)
                callback?.onAdClick()
            }

            override fun onAdDismiss() {
                AdLog.logMessage("Ad dismissed from: $providerType", strategyName = TAG, adType = adType, isPreload = isPreload)
                callback?.onAdDismiss()
            }

            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                AdLog.logMessage("Rewarded from: $providerType - Amount: $rewardAmount, Name: $rewardName", strategyName = TAG, adType = adType, isPreload = isPreload)
                callback?.onAdRewarded(rewardAmount, rewardName)
            }
        })
    }
}
