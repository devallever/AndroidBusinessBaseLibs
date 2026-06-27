package com.clean.wood.utils

object Constant {

    const val PRIVACY_URL = "https://www.bing.com/"
    const val ANIMATION_MIN_DURATION = 1 * 1000L
    const val ANIMATION_MAX_DURATION = 3 * 1000L

    object PrefKey {
        const val REFER_CACHE = "refer_cache_key"
        const val AD_CONFIG_CACHE = "ad_config_cache_key"
        const val AD_LIMITED_RECORD_CACHE = "ad_limited_record_cache_key"
    }

    enum class JunkType {
        SystemCache,
        Residual,
        Ad,
        ObsoleteApk,
        Temp,
        Thumb
    }

    class FunType {
        companion object {
            const val JUNK_CLEAN = 1
            const val VPN = 2
            const val CPU_COOLER = 3
            const val BATTERY = 4
            const val APP_MANAGER = 5
            const val PHONE_BOOSTER = 6
        }

    }

    enum class AdPosition {
        SplashInter,
        ScanningInter,
        OptimizingInter,
        EnterInter,
        ExitInter,
        BackupInter,
        HomeNative,
        ScanningNative,
        OptimizingNative,
        ResultNative,
        BackupNative
    }
}