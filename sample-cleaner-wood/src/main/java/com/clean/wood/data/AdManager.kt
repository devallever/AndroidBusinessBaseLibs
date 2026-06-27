package com.clean.wood.data

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.children
import com.clean.wood.WoodApp
import com.clean.wood.data.model.AdCache
import com.clean.wood.data.model.AdConfig
import com.clean.wood.data.model.AdLimitedRecord
import com.clean.wood.data.model.AdPositionConfig
import com.clean.wood.data.model.AdPositionLimitedRecord
import com.clean.wood.databinding.AdLargeBinding
import com.clean.wood.databinding.AdLargePlaceholderBinding
import com.clean.wood.databinding.AdSmallBinding
import com.clean.wood.databinding.AdSmallPlaceholderBinding
import com.clean.wood.utils.Constant
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AdManager private constructor() {
    companion object {
        val ins by lazy {
            AdManager()
        }
        private const val DEFAULT_AD_CONFIG = """
{
    "fuliter1": {
        "onlyu": "ca-app-pub-3940256099942544/1033173712",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "fuliter2": {
        "onlyu": "ca-app-pub-3940256099942544/1033173712",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "fuliter3": {
        "onlyu": "ca-app-pub-3940256099942544/1033173712",
        "sawd": 10,
        "tapd": 2,
        "sours": "refer"
    },
    "fuliter4": {
        "onlyu": "ca-app-pub-3940256099942544/1033173712",
        "sawd": 10,
        "tapd": 2,
        "sours": "refer"
    },
    "fuliter5": {
        "onlyu": "ca-app-pub-3940256099942544/1033173712",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "fuliterbak": {
        "onlyu": "ca-app-pub-3940256099942544/1033173712",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "scrnav1": {
        "onlyu": "ca-app-pub-3940256099942544/2247696110",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "scrnav2": {
        "onlyu": "ca-app-pub-3940256099942544/2247696110",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "scrnav3": {
        "onlyu": "ca-app-pub-3940256099942544/2247696110",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "scrnav4": {
        "onlyu": "ca-app-pub-3940256099942544/2247696110",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "scrnavbak": {
        "onlyu": "ca-app-pub-3940256099942544/2247696110",
        "sawd": 15,
        "tapd": 3,
        "sours": "refer"
    },
    "genbu": {
        "sawd": 60,
        "tapd": 7
    }
}
  """
        private const val MAX_CACHED_DURATION = 40 //minutes
        private const val MAX_LOADING_TIME = 20 * 1000
    }

    private lateinit var adConfig: AdConfig
    private lateinit var adLimitedRecord: AdLimitedRecord
    private val adCaches = HashMap<Constant.AdPosition, AdCache>()
    private val waitingNativeContainers = HashMap<Constant.AdPosition, WeakReference<ViewGroup>>()
    private val showingNativeContainers = HashMap<Constant.AdPosition, WeakReference<ViewGroup>>()
    private val adsLoadStartTime = HashMap<Constant.AdPosition, Long>()
    private val backupNativeMap = HashMap<Long, Constant.AdPosition>()
    private var showingInterPosition: Constant.AdPosition? = null
    private val gson = Gson()

    fun init(context: Context) {
        MobileAds.initialize(context)
        initLimitedRecord()
        initAdConfig()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initLimitedRecord() {
        val adLimitedRecordJson =
            KvManager.ins.getString(Constant.PrefKey.AD_LIMITED_RECORD_CACHE, "{}")
        adLimitedRecord = gson.fromJson(adLimitedRecordJson, AdLimitedRecord::class.java)
        if (!adLimitedRecord.recordDate.isEqual(LocalDate.now())) {
            adLimitedRecord = AdLimitedRecord()
            updateLimitedRecord()
        }
    }

    private fun initAdConfig() {
        val adConfigJson =
            KvManager.ins.getString(Constant.PrefKey.AD_CONFIG_CACHE, DEFAULT_AD_CONFIG)
        adConfig = gson.fromJson(adConfigJson, AdConfig::class.java)
    }

    fun updateConfig(configString: String) {
        try {
            adConfig = gson.fromJson(configString, AdConfig::class.java)
            KvManager.ins.putString(Constant.PrefKey.AD_CONFIG_CACHE, configString)
        } catch (_: Exception) {
        }
    }

    private fun updateLimitedRecord() {
        KvManager.ins.putString(
            Constant.PrefKey.AD_LIMITED_RECORD_CACHE,
            gson.toJson(adLimitedRecord)
        )
    }

    private fun isAdSumOverLimited(): Boolean {
        return adLimitedRecord.allClickLimited >= adConfig.allLimits.allClickLimited || adLimitedRecord.allShowLimited >= adConfig.allLimits.allShowLimited
    }

    private fun isAdAllowed(adPosition: Constant.AdPosition): Boolean {
        return isAdSwitchOn(adPosition) && !isAdOverLimited(adPosition)
    }

    private fun isAdSwitchOn(adPosition: Constant.AdPosition): Boolean {
        return getAdPositionConfig(adPosition).adSwitch == "on" ||
                (getAdPositionConfig(adPosition).adSwitch == "refer" && ReferManager.ins.isReferUser())
    }

    private fun isAdOverLimited(adPosition: Constant.AdPosition): Boolean {
        if (adPosition == Constant.AdPosition.BackupNative || adPosition == Constant.AdPosition.BackupInter) {
            return isAdSumOverLimited()
        }
        val adPositionConfig = getAdPositionConfig(adPosition)
        val positionLimitedRecord = getAdPositionLimitedRecord(adPosition)
        return positionLimitedRecord.showLimited >= adPositionConfig.showLimited ||
                positionLimitedRecord.clickLimited >= adPositionConfig.clickLimited ||
                isAdSumOverLimited()
    }

    private fun getAdPositionConfig(adPosition: Constant.AdPosition): AdPositionConfig {
        return when (adPosition) {
            Constant.AdPosition.SplashInter -> adConfig.inter1
            Constant.AdPosition.ScanningInter -> adConfig.inter2
            Constant.AdPosition.OptimizingInter -> adConfig.inter3
            Constant.AdPosition.EnterInter -> adConfig.inter4
            Constant.AdPosition.ExitInter -> adConfig.inter5
            Constant.AdPosition.BackupInter -> adConfig.interBak
            Constant.AdPosition.HomeNative -> adConfig.native1
            Constant.AdPosition.ScanningNative -> adConfig.native2
            Constant.AdPosition.OptimizingNative -> adConfig.native3
            Constant.AdPosition.ResultNative -> adConfig.native4
            Constant.AdPosition.BackupNative -> adConfig.nativeBak
        }
    }

    private fun getAdPositionLimitedRecord(adPosition: Constant.AdPosition): AdPositionLimitedRecord {
        return when (adPosition) {
            Constant.AdPosition.SplashInter -> adLimitedRecord.inter1
            Constant.AdPosition.ScanningInter -> adLimitedRecord.inter2
            Constant.AdPosition.OptimizingInter -> adLimitedRecord.inter3
            Constant.AdPosition.EnterInter -> adLimitedRecord.inter4
            Constant.AdPosition.ExitInter -> adLimitedRecord.inter5
            Constant.AdPosition.BackupInter -> adLimitedRecord.bakPlaceHoler
            Constant.AdPosition.HomeNative -> adLimitedRecord.native1
            Constant.AdPosition.ScanningNative -> adLimitedRecord.native2
            Constant.AdPosition.OptimizingNative -> adLimitedRecord.native3
            Constant.AdPosition.ResultNative -> adLimitedRecord.native4
            Constant.AdPosition.BackupNative -> adLimitedRecord.bakPlaceHoler
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAd(position: Constant.AdPosition, retryTimes: Int = 0) {
        if (!isAdAllowed(position)) {
            return
        }
        adCaches[position]?.let {
            if (Duration.between(it.cachedTime, LocalDateTime.now())
                    .toMinutes() < MAX_CACHED_DURATION
            ) {
                return
            }
        }
        destroyAd(adCaches.remove(position)?.adRes)
        loadAd(position, retryTimes)
    }

    private fun destroyAd(adRes: Any?) {
        if (adRes is NativeAd) {
            adRes.destroy()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadAd(position: Constant.AdPosition, retryTimes: Int = 0) {
        val current = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (current - (adsLoadStartTime[position] ?: 0) < MAX_LOADING_TIME) {
            return
        }
        val adPositionConfig = getAdPositionConfig(position)
        if (adPositionConfig.adId.isEmpty()) {
            ConfigManager.ins.fetchConfig()
            return
        }
        adsLoadStartTime[position] = current
        if (isNative(position)) {
            loadNative(position, current, retryTimes)
        } else {
            loadInter(position, current, retryTimes)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadNative(
        position: Constant.AdPosition,
        loadStartTime: Long,
        retryTimes: Int = 0
    ) {
        val adPositionConfig = getAdPositionConfig(position)
        val adLoader = AdLoader.Builder(WoodApp.context, adPositionConfig.adId)
            .forNativeAd { ad ->
                adsLoadStartTime[position] = 0
                adCaches[position] = AdCache(
                    position,
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(loadStartTime),
                        ZoneId.systemDefault()
                    ),
                    ad
                )
                waitingNativeContainers.remove(position)?.get()?.let { container ->
                    showNative(position, container)
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    if (adsLoadStartTime[position] == loadStartTime) {
                        adsLoadStartTime[position] = 0
                        if (retryTimes < 1) {
                            checkAd(position, retryTimes + 1)
                        }
                    }
                }

                override fun onAdClicked() {
                    val realPosition = getNativeRealPosition(loadStartTime, position)
                    val positionLimitedRecord = getAdPositionLimitedRecord(realPosition)
                    positionLimitedRecord.clickLimited++
                    adLimitedRecord.allClickLimited++
                    updateLimitedRecord()
                    val container = showingNativeContainers[realPosition]?.get()
                    cancelNativeShow(realPosition)
                    if (container != null) {
                        showNative(realPosition, container)
                    }
                }

                override fun onAdImpression() {
                    val realPosition = getNativeRealPosition(loadStartTime, position)
                    val positionLimitedRecord = getAdPositionLimitedRecord(realPosition)
                    positionLimitedRecord.showLimited++
                    adLimitedRecord.allShowLimited++
                    updateLimitedRecord()
                    if (needReload(position)) {
                        checkAd(position)
                    }
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun getNativeRealPosition(
        startTime: Long,
        position: Constant.AdPosition
    ): Constant.AdPosition {
        if (position == Constant.AdPosition.BackupNative) {
            return backupNativeMap[startTime] ?: position
        }
        return position
    }

    private fun isNative(position: Constant.AdPosition): Boolean {
        return when (position) {
            Constant.AdPosition.HomeNative,
            Constant.AdPosition.ScanningNative,
            Constant.AdPosition.OptimizingNative,
            Constant.AdPosition.ResultNative,
            Constant.AdPosition.BackupNative -> true

            else -> false
        }
    }

    private fun isLargeNative(position: Constant.AdPosition): Boolean {
        return position == Constant.AdPosition.ResultNative
    }

    /**
     * return true if Ad load failed, succeed or even switch off, whatever, just start the next job.
     */
    fun isAdReadyNext(position: Constant.AdPosition): Boolean {
        return !isAdAllowed(position) ||
                adCaches[position] != null ||
                adsLoadStartTime[position] == 0L
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNative(position: Constant.AdPosition, container: ViewGroup) {
        if (!isAdAllowed(position)) {
            destroyAd(adCaches.remove(position)?.adRes)
            return
        }
        checkAd(position)
        val positionCache = adCaches.remove(position)
        val finalAdCache = if (positionCache == null) {
            val bakCache = adCaches.remove(Constant.AdPosition.BackupNative)
            if (bakCache != null) {
                val bakStartTime = bakCache.cachedTime.atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli()
                backupNativeMap[bakStartTime] = position
            }
            checkAd(Constant.AdPosition.BackupNative)
            bakCache
        } else {
            positionCache
        }
        if (finalAdCache != null) {
            showingNativeContainers[position] = WeakReference(container)
            val nativeAd = finalAdCache.adRes as NativeAd
            container.removeAllViews()
            container.visibility = View.VISIBLE
            if (!isLargeNative(position)) {
                val binding = AdSmallBinding.inflate(
                    LayoutInflater.from(container.context),
                    container,
                    true
                )
                binding.root.bodyView = binding.adContent
                binding.root.iconView = binding.adIcon
                binding.root.headlineView = binding.adTitle
                binding.root.callToActionView = binding.adCallButton

                binding.adContent.text = nativeAd.body
                binding.adTitle.text = nativeAd.headline
                binding.adCallButton.text = nativeAd.callToAction
                if (nativeAd.icon?.drawable != null) {
                    binding.adIcon.visibility = View.VISIBLE
                    binding.adIcon.setImageDrawable(nativeAd.icon?.drawable)
                } else {
                    binding.adIcon.visibility = View.GONE
                }
                binding.root.setNativeAd(nativeAd)
            } else {
                val binding = AdLargeBinding.inflate(
                    LayoutInflater.from(container.context),
                    container,
                    true
                )
                binding.root.bodyView = binding.adContent
                binding.root.iconView = binding.adIcon
                binding.root.headlineView = binding.adTitle
                binding.root.callToActionView = binding.adAction
                binding.root.mediaView = binding.adMedia

                binding.adContent.text = nativeAd.body
                binding.adTitle.text = nativeAd.headline
                binding.adAction.text = nativeAd.callToAction
                if (nativeAd.icon?.drawable != null) {
                    binding.adIcon.visibility = View.VISIBLE
                    binding.adIcon.setImageDrawable(nativeAd.icon?.drawable)
                } else {
                    binding.adIcon.visibility = View.GONE
                }
                binding.adMedia.mediaContent = nativeAd.mediaContent
                binding.root.setNativeAd(nativeAd)
            }
        } else {
            if (isLargeNative(position)) {
                AdLargePlaceholderBinding.inflate(
                    LayoutInflater.from(container.context),
                    container,
                    true
                )
            } else {
                AdSmallPlaceholderBinding.inflate(
                    LayoutInflater.from(container.context),
                    container,
                    true
                )
            }
            waitingNativeContainers[position] = WeakReference(container)
        }
    }

    fun cancelNativeShow(position: Constant.AdPosition) {
        waitingNativeContainers.remove(position)?.get()?.let { container ->
            container.children.forEach {
                if (it is NativeAdView) {
                    it.destroy()
                }
            }
            container.removeAllViews()
        }
        showingNativeContainers.remove(position)?.get()?.let { container ->
            container.children.forEach {
                if (it is NativeAdView) {
                    it.destroy()
                }
            }
            container.removeAllViews()
        }
    }

    private fun loadInter(
        position: Constant.AdPosition,
        loadStartTime: Long,
        retryTimes: Int = 0
    ) {
        val adPositionConfig = getAdPositionConfig(position)
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(WoodApp.context,
            adPositionConfig.adId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    if (adsLoadStartTime[position] == loadStartTime) {
                        adsLoadStartTime[position] = 0
                        if (retryTimes < 1) {
                            checkAd(position, retryTimes + 1)
                        }
                    }
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onAdLoaded(ad: InterstitialAd) {
                    adsLoadStartTime[position] = 0
                    adCaches[position] = AdCache(position, LocalDateTime.now(), ad)
                }
            })
    }

    /**
     * Will return directly if Ad is not ready.
     * If Ad ready to show, return when Ad show failed or Ad dismissed.
     */
    suspend fun showInterAd(position: Constant.AdPosition) = withContext(Dispatchers.Main) {
        if (!isAdAllowed(position)) {
            destroyAd(adCaches.remove(position)?.adRes)
            return@withContext
        }
        val activityStack = WoodApp.activityStack
        val topActivity =
            if (activityStack.isNotEmpty()) activityStack.peek().get() else null
        if (topActivity == null || showingInterPosition != null) {
            return@withContext
        }
        val positionLimitedRecord = getAdPositionLimitedRecord(position)
        val positionCache = adCaches.remove(position)
        val useBak: Boolean
        val finalAdRes = if (positionCache == null) {
            useBak = true
            val bakCache = adCaches.remove(Constant.AdPosition.BackupInter)
            bakCache?.adRes
        } else {
            useBak = false
            positionCache.adRes
        }
        if (finalAdRes is InterstitialAd) {
            showingInterPosition = position
            suspendCoroutine { showContinuation ->
                finalAdRes.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdClicked() {
                        positionLimitedRecord.clickLimited++
                        adLimitedRecord.allClickLimited++
                        updateLimitedRecord()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        showingInterPosition = null
                        showContinuation.resume(Unit)
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        showingInterPosition = null
                        showContinuation.resume(Unit)
                        if (useBak) {
                            checkAd(Constant.AdPosition.BackupInter)
                        } else if (needReload(position)) {
                            checkAd(position)
                        }
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onAdShowedFullScreenContent() {
                        positionLimitedRecord.showLimited++
                        adLimitedRecord.allShowLimited++
                        updateLimitedRecord()
                        if (useBak) {
                            checkAd(Constant.AdPosition.BackupInter)
                        } else if (needReload(position)) {
                            checkAd(position)
                        }
                    }
                }
                finalAdRes.show(topActivity)
            }
        }
    }

    /**
     * Constant.AdPosition.BackupInter need reload but not use this method
     */
    private fun needReload(position: Constant.AdPosition): Boolean {
        return position == Constant.AdPosition.EnterInter ||
                position == Constant.AdPosition.ExitInter ||
                position == Constant.AdPosition.OptimizingNative ||
                position == Constant.AdPosition.ScanningNative ||
                position == Constant.AdPosition.BackupNative
    }
}