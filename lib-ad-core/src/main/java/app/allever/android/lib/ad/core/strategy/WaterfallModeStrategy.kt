package app.allever.android.lib.ad.core.strategy

import android.content.Context
import app.allever.android.lib.ad.core.AdManager.LoadMode
import app.allever.android.lib.ad.core.AdManager.getActiveProvider
import app.allever.android.lib.ad.core.AdManager.providerPool
import app.allever.android.lib.ad.core.AdManager.switchToProvider
import app.allever.android.lib.ad.core.base.AdProviderFactory
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
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
            logPrefix = AdLog.PREFIX_WATERFALL,
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
                logAction(AdLog.PREFIX_CACHE, "Found cache in", providerType)
                switchToProvider(providerType)
                callback?.onAdLoaded()
                return true
            }
        }

        log(AdLog.format(TAG, AdLog.PREFIX_CACHE, "No valid cache in any waterfall provider"))
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
            logPrefix = AdLog.PREFIX_WATERFALL,
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
        logPrefix: String,
        checkMode: Boolean = false
    ) {
        logAction(logPrefix, "Starting ${if (isPreload) "pre-" else ""}waterfall", adType.name, isPreload)

        if (checkMode && !checkLoadMode(LoadMode.WATERFALL, logPrefix, isPreload)) {
            return
        }

        val waterfallProviders = getProviders()

        if (waterfallProviders.isEmpty()) {
            logError(logPrefix, "No providers with waterfall support available", isPreload)

            if (!isPreload) {
                fallbackToSingle(context, adType, callback, logPrefix, isPreload)
            }
            return
        }

        val actionWord = if (isPreload) "preload from" else ""
        logAction(logPrefix, "Trying to $actionWord", "${waterfallProviders.size} providers", isPreload)

        if (!isPreload) {
            val waterfallOrder = waterfallProviders.joinToString(" → ") { it.first }
            log(AdLog.format(TAG, logPrefix, "Order: $waterfallOrder (${waterfallProviders.size} providers)"))
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
        logPrefix: String = AdLog.PREFIX_WATERFALL
    ) {

        if (currentIndex >= providers.size) {
            logError(logPrefix, "All ${providers.size} providers failed for ${adType.name}", isPreload)

            if (!isPreload) {
                callback?.onAdFail(-1, "All waterfall providers failed")
            } else {
                logError(logPrefix, "All providers failed", isPreload)
            }
            return
        }

        val (providerType, config) = providers[currentIndex]
        val provider = providerPool[providerType]
        val adId = config.getAdIdByType(adType)

        if (provider == null) {
            log(AdLog.format(TAG, logPrefix, "[$currentIndex] Provider $providerType not in pool, skipping..."))
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback, isPreload, logPrefix)
            return
        }


        if (adId.isNullOrEmpty()) {
            log(AdLog.format(TAG, logPrefix, "[$currentIndex] No ad ID for $providerType/${adType.name}, skipping..."))
            tryLoadFromWaterfall(providers, currentIndex + 1, context, adType, callback, isPreload, logPrefix)
            return
        }

        log(AdLog.format(TAG, logPrefix, "[$currentIndex/$providers.size] Trying: $providerType (ID: $adId)"))

        provider.loadAd(context, adType, adId, object : IAdCallback {

            override fun onAdLoaded() {
                log(AdLog.formatSuccess(TAG, logPrefix, "SUCCESS at [$currentIndex]: $providerType"))

                switchToProvider(providerType)

                if (isPreload) {
                    logSuccess(logPrefix, "Preload successful!", isPreload)
                } else {
                    callback?.onAdLoaded()
                }
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                log(AdLog.formatError(TAG, logPrefix, "FAILED at [$currentIndex]: $providerType - Error($errorCode): $errorMessage"))

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
                log(AdLog.format(TAG, logPrefix, "Ad shown from: $providerType"))
                callback?.onAdShow()
            }

            override fun onAdClick() {
                log(AdLog.format(TAG, logPrefix, "Ad clicked from: $providerType"))
                callback?.onAdClick()
            }

            override fun onAdDismiss() {
                log(AdLog.format(TAG, logPrefix, "Ad dismissed from: $providerType"))
                callback?.onAdDismiss()
            }

            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                log(AdLog.format(TAG, logPrefix, "Rewarded from: $providerType - Amount: $rewardAmount, Name: $rewardName"))
                callback?.onAdRewarded(rewardAmount, rewardName)
            }
        })
    }
}
