//package com.videoeditor.function.ad
//
//import com.rice.balls.utils.EncryptConstant
//import com.allever.video.editor.BuildConfig
//
///**
// * 常量配置
// */
//object AdConstant {
//
//    const val FLURRY_APP_KEY = BuildConfig.FLURRY_APPKEY
//    const val APPSFLYER_DEV_KEY = BuildConfig.APPSFLYER_APPKEY
//    const val ADMOB_APPKEY = BuildConfig.ADMOB_APPKEY
//    const val APPLOVIN_APPKEY = BuildConfig.APPLOVIN_APPKEY
//    const val VUNGLE_APPKEY = BuildConfig.VUNGLE_APPKEY
//    const val ADCOLONY_APPKEY = BuildConfig.ADCOLONY_APPKEY
//    const val DISPLAYIO_APPKEY = BuildConfig.DISPLAYIO_APPKEY
//    const val OGURY_APPKEY = BuildConfig.OGURY_APPKEY
//    const val IRONSOURCE_APPKEY = BuildConfig.IRONSOURCE_APPKEY
//    const val TAPJOY_SDKKEY = ""
//    const val AMAZON_APPKEY = BuildConfig.AMAZON_APPKEY
//
//
//    val AD_CONFIG_FILE_HOST: String//http://www.allinai.global:23456/xxxxxx/%s
//    val AD_UNIT_PRIORITY_URL: String
//    val AD_STRATEGY_URL: String
//
//    init {
//        AD_CONFIG_FILE_HOST = EncryptConstant.decodeBase64(BuildConfig.ADCONFIG_HOST_ENCRYPT)
//        AD_UNIT_PRIORITY_URL = String.format(AD_CONFIG_FILE_HOST, BuildConfig.AD_UNIT_PRIORITY_FILE_NAME)
//        AD_STRATEGY_URL = String.format(AD_CONFIG_FILE_HOST, BuildConfig.AD_STRATEGY_FILE_NAME)
//    }
//
//
//    class StrategyConstant : com.rice.balls.strategy.StrategyConstant
//
//    /**
//     * 广告位id
//     */
//    object AdVirtualUnitID {
//
//        // 解锁后全屏
//        val PLACEMENTID_NATIVE_UNLOCK = "ad1"
//        // 礼盒广告位
//        val PLACEMENTID_NATIVE_GIFT_BOX = ""
//        // 应用退出
//        val PLACEMENTID_NATIVE_APPEXIT = "ad2"
//        // 悬浮球广告
//        val PLACEMENTID_NATIVE_FLOATING = "ad3"
//
////        解锁广告：ad1
////        切换广告：ad2
////        悬浮球广告：ad3
//
////        合成完成：ad4
////        相册页广告：ad5
////        分享页：ad6
////        合成等待页广告：ad7
//
//        val UNITID_SAVE = "ad4"
//        val UNITID_ALBUM_FLOW = "ad5"
//        val UNITID_SHARE = "ad6"
//        val UNITID_SAVE_WATING = "ad7"
//
//    }
//}
