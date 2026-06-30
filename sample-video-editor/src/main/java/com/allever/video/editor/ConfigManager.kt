package com.allever.video.editor

import com.android.absbase.utils.SpUtils
import com.android.absbase.utils.TimeUtils

object ConfigManager {

    /**
     * 当前第一次打开应用的时间
     */
    private val KEY_FIRST_OPEN_APP_TIME_IN_DAY = "kfoatid_dkfalsldf"
    var firstOpenAppTimeInDay: Long
        get() = SpUtils.obtain().get(KEY_FIRST_OPEN_APP_TIME_IN_DAY, 0L)
        private set(value) = SpUtils.obtain().save(KEY_FIRST_OPEN_APP_TIME_IN_DAY, value)

    /**
     * 当天打开应用的次数
     */
    private val KEY_OPEN_APP_COUNT_IN_DAY = "koacid_dkfkks"
    var openAppCountInDay: Int
        get() = SpUtils.obtain().get(KEY_OPEN_APP_COUNT_IN_DAY, 0)
        private set(value) = SpUtils.obtain().save(KEY_OPEN_APP_COUNT_IN_DAY, value)

    const val KEY_FIRST_OPEN_APP_TIME = "kfoat_dkfkdkdf"
    var firstOpenAppTime: Long
        get() = SpUtils.obtain().get(KEY_FIRST_OPEN_APP_TIME, 0L)
        private set(value) = SpUtils.obtain().save(KEY_FIRST_OPEN_APP_TIME, value)

    /**
     * 首次进入app
     */
    const val KEY_FIRST_OPEN_APP = "cm_loat_dkekf"
    var firstOpenApp: Boolean
        get() = SpUtils.obtain().get(KEY_FIRST_OPEN_APP, true)
        set(value) = SpUtils.obtain().save(KEY_FIRST_OPEN_APP, value)

    fun recordOpenApp() {
        val currentTimeMillis = System.currentTimeMillis()
        if (firstOpenAppTime == 0L) {
            firstOpenAppTime = currentTimeMillis
        } else {
            firstOpenApp = false
        }

        val firstTime = SpUtils.obtain().get(KEY_FIRST_OPEN_APP_TIME_IN_DAY, 0L)
        if (!TimeUtils.isSameDayOfMillis(firstTime, currentTimeMillis)) {
            firstOpenAppTimeInDay = currentTimeMillis
            openAppCountInDay = 1
        } else {
            openAppCountInDay++
        }
    }

    private const val KEY_STATIC_EFFECT_DURATION = "ksed_dkekfs"
    var staticEffectDuration: Long
        get() = SpUtils.obtain().get(KEY_STATIC_EFFECT_DURATION, TimeUtils.TimeConstant.ONE_MIN * 3)
        set(value) = SpUtils.obtain().save(KEY_STATIC_EFFECT_DURATION, value)

    private const val KEY_STATIC_IMAGE_DURATION = "kestasd_qwzc"
    var staticImageDuration: Long
        get() = SpUtils.obtain().get(KEY_STATIC_IMAGE_DURATION, TimeUtils.TimeConstant.ONE_SEC * 3)
        set(value) = SpUtils.obtain().save(KEY_STATIC_IMAGE_DURATION, value)

    /**
     * 订阅了年
     */
    const val KEY_PURCHASE_SUB_YEAR = "cm_p_s_y_asd"
    var purchaseSubYear: Boolean
        get() = SpUtils.obtain().get(KEY_PURCHASE_SUB_YEAR, false)
        set(value) = SpUtils.obtain().save(KEY_PURCHASE_SUB_YEAR, value)

    /**
     * 年续订
     */
    const val KEY_SUB_YEAR_RENEW = "cm_zcas_cas_a"
    var subYearAutoRenew: Boolean
        get() = SpUtils.obtain().get(KEY_SUB_YEAR_RENEW, false)
        set(value) = SpUtils.obtain().save(KEY_SUB_YEAR_RENEW, value)

    /**
     * 月续订
     */
    const val KEY_SUB_MONTH_RENEW = "cm_wegr_ksdfj"
    var subMonthAutoRenew: Boolean
        get() = SpUtils.obtain().get(KEY_SUB_MONTH_RENEW, false)
        set(value) = SpUtils.obtain().save(KEY_SUB_MONTH_RENEW, value)


    /**
     * 订阅了月
     */
    const val KEY_PURCHASE_SUB_MONTH = "cm_p_s_m_zxc"
    var purchaseSubMonth: Boolean
        get() = SpUtils.obtain().get(KEY_PURCHASE_SUB_MONTH, false)
        set(value) = SpUtils.obtain().save(KEY_PURCHASE_SUB_MONTH, value)
    /**
     * 记录购买的状态，0无效订阅,-1查询失败，>0有效订阅
     */
    const val KEY_PURCHASE_SUB_SIZE = "cm_pss_awasd"
    var purchaseSubSize: Int = 1
//        get() = SpUtils.obtain().get(KEY_PURCHASE_SUB_SIZE, -1)
        set(value) {
            SpUtils.obtain().save(KEY_PURCHASE_SUB_SIZE, value)
            needAd = value < 1
        }
    /**
     * 记录订阅了月的状态， autoRenewing = true 自动续费
     */
    const val KEY_PURCHASE_SUB_AUTO_RENEWING = "cm_pss_fbhfs"
    var purchaseSubMonthAutoRenewing: Boolean
        get() = SpUtils.obtain().get(KEY_PURCHASE_SUB_AUTO_RENEWING, false)
        set(value) = SpUtils.obtain().save(KEY_PURCHASE_SUB_AUTO_RENEWING, value)
    /**
     * 当天首次进入App的时间,内购
     */
    const val KEY_FIRST_ENTER_APP_OF_DAY = "cm_f_e_a_o_zdas"
    var firstEnterAPPOfDay: Long
        get() = SpUtils.obtain().get(KEY_FIRST_ENTER_APP_OF_DAY, 0L)
        set(value) = SpUtils.obtain().save(KEY_FIRST_ENTER_APP_OF_DAY, value)

    const val KEY_NEED_AD = "cm_najskdkf"
    var needAd: Boolean
        get() = SpUtils.obtain().get(KEY_NEED_AD, true)
        set(value) {
            SpUtils.obtain().save(KEY_NEED_AD, value)
        }

    /**
     * 首次设置隐藏私密相册
     */
    const val KEY_FIRST_HIDE_SECRET_VAULT = "cm_loat_dkekf_dwsdfs"
    var firstHideSecretVault: Boolean
        get() = SpUtils.obtain().get(KEY_FIRST_HIDE_SECRET_VAULT, true)
        set(value) = SpUtils.obtain().save(KEY_FIRST_HIDE_SECRET_VAULT, value)

    /**
     * 当天下载config的重试次数
     */
    const val KEY_DOWNLOAD_CONFIG_RETRY_COUNT = "cm_loat_dkekf_dwsdfs_hrtz"
    var retryCountDownloadCofnig: Int
        get() = SpUtils.obtain().get(KEY_DOWNLOAD_CONFIG_RETRY_COUNT, 3)
        set(value) = SpUtils.obtain().save(KEY_DOWNLOAD_CONFIG_RETRY_COUNT, value)

    /**
     * 当天最后一次重试下载config的时间戳
     */
    const val KEY_LAST_DOWNLOAD_CONFIG_RETRY_COUNT = "cm_loat_dkekf_dwsdfs_hrtz_st"
    var lastRetryDownloadConfig: Long
        get() = SpUtils.obtain()[KEY_LAST_DOWNLOAD_CONFIG_RETRY_COUNT, 0L]
        set(value) = SpUtils.obtain().save(KEY_LAST_DOWNLOAD_CONFIG_RETRY_COUNT, value)

    /**
     * 上一次重试下载Config的时间戳
     */
    const val KEY_LAST_RETRY_DOWNLOAD_CONFIT_TIME = "cm_loat_dkekf_dwsdfs_hrtz_st_sdts"
    var lastRetryDownloadConfigTime: Long
        get() = SpUtils.obtain()[KEY_LAST_RETRY_DOWNLOAD_CONFIT_TIME, 0L]
        set(value) = SpUtils.obtain().save(KEY_LAST_RETRY_DOWNLOAD_CONFIT_TIME, value)

    /**
     * 上一次重试下载Effect的时间戳
     */
    const val KEY_LAST_RETRY_DOWNLOAD_EFFECT_TIME = "cm_loat_dkekf_dwsdfs_hrtz_st_sdts_sx"
    var lastRetryDownloadEffectTime: Long
        get() = SpUtils.obtain()[KEY_LAST_RETRY_DOWNLOAD_EFFECT_TIME, 0L]
        set(value) = SpUtils.obtain().save(KEY_LAST_RETRY_DOWNLOAD_EFFECT_TIME, value)


    /**
     * 首次进入订阅弹挽留窗口的时间
     */
    const val KEY_FIRST_ENTER_GP_SUB_RETAIN_TIME = "cm_f_g_p_s_zzxwdas"
    var firstEnterGpRetainTime: Long
        get() = SpUtils.obtain().get(KEY_FIRST_ENTER_GP_SUB_RETAIN_TIME, 0L)
        set(value) = SpUtils.obtain().save(KEY_FIRST_ENTER_GP_SUB_RETAIN_TIME, value)

    /**
     * 首次进入订阅页记录的样式  1 or 2
     */
    const val KEY_FIRST_ENTER_GP_STYLE_RECORD = "cm_first_style_wdas"
    var firstEnterGpStyle: Int
        get() = SpUtils.obtain().get(KEY_FIRST_ENTER_GP_STYLE_RECORD, 0)
        set(value) = SpUtils.obtain().save(KEY_FIRST_ENTER_GP_STYLE_RECORD, value)

    /**
     * 是否通过挽留弹窗完成订阅
     */
    const val KEY_RETAIN_DIALOG_FINISH_SUBS_OR_NOT = "cm_retain_dialog_finish_subs_or_not"
    var retainDialogFinishSubsOrNot: Boolean
        get() = SpUtils.obtain().get(KEY_RETAIN_DIALOG_FINISH_SUBS_OR_NOT, false)
        set(value) = SpUtils.obtain().save(KEY_RETAIN_DIALOG_FINISH_SUBS_OR_NOT, value)


    /**
     * 上一次显示插屏广告的时间
     */
    const val KEY_PREV_SHOW_FULL_SCREEN_AD_TIME_MILLIS = "cm_kpsatm_dkfksf"
    var prevShowFullScreenAdTimeMillis: Long
        get() = SpUtils.obtain().get(KEY_PREV_SHOW_FULL_SCREEN_AD_TIME_MILLIS, 0L)
        set(value) = SpUtils.obtain().save(KEY_PREV_SHOW_FULL_SCREEN_AD_TIME_MILLIS, value)

    /**
     * 某一个场景显示插屏广告的时间
     */
    private const val KEY_PREV_SHOW_FULL_SCREEN_AD_TIME_MILLIS_BY_SCENE_PREFIX = "cm_kpsatm_bs_dkfksf"

    fun getPrevShowFullSceneAdTimeMillisByScene(scene: String): Long {
        val key = KEY_PREV_SHOW_FULL_SCREEN_AD_TIME_MILLIS_BY_SCENE_PREFIX + scene
        return SpUtils.obtain().get(key, 0L)
    }

    fun setPrevShowFullSceneAdTimeMillisByScene(scene: String, timeMillis: Long) {
        val key = KEY_PREV_SHOW_FULL_SCREEN_AD_TIME_MILLIS_BY_SCENE_PREFIX + scene
        SpUtils.obtain().save(key, timeMillis)
    }

    /**
     * 最多支持合成视频数
     */
    const val KEY_MAX_VIDEO_COUNT = "ket_sadwviduead"
    var maxVideoCount: Int
        get() = SpUtils.obtain().get(KEY_MAX_VIDEO_COUNT, 999)
        set(value) = SpUtils.obtain().save(KEY_MAX_VIDEO_COUNT, value)

    const val KEY_FIRST_SCAN_LOCAL_MUSIC = "cm_kfslm_dkfkksdf"
    var firstScanLocalMusic: Boolean
        get() = SpUtils.obtain().get(KEY_FIRST_SCAN_LOCAL_MUSIC, true)
        set(value) = SpUtils.obtain().save(KEY_FIRST_SCAN_LOCAL_MUSIC, value)

}