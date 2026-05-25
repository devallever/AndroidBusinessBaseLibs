package app.allever.android.sample.ad.bigo

import app.allever.android.lib.core.app.App


/**
 *@Description
 *@author: zq
 *@date: 2023/10/30
 */
interface IAdConfig {

    companion object {
        const val BANNER_AD = 0
        const val INTER_AD = 1
        const val NATIVE_AD = 2
        const val REWARD_AD = 3
    }

    fun bannerAdId(): String
    fun interAdId(): String
    fun rewardAdId(): String
    fun nativeAdId(): String

    /***
     * 测试设备：建议使用安装有Google Play Services服务的设备
     * 测试应用 Id：10182906
     * 测试 Slot Id
     * 广告类型	测试 Slot Id
     * 横幅广告 300x250	10182906-10151323
     * 横幅广告 320x50	10182906-10156618
     * 原生-图文广告	10182906-10087503
     * 原生-视频广告	10182906-10071993
     * 插屏广告	10182906-10158798
     * 激励视频广告	10182906-10001431
     * 开屏广告 - 全屏样式	10182906-10129310
     * 开屏广告 - 半屏样式	10182906-10090598
     * 弹窗广告	10182906-10873783
     */
    fun getAdId(type: Int): String {
        return if (App.DEBUG) {
            when (type) {
                BANNER_AD -> "10182906-10156618"
                INTER_AD -> "10182906-10158798"
                NATIVE_AD -> "10182906-10087503"
                REWARD_AD -> "10182906-10001431"
                else -> ""
            }
        } else {
            when (type) {
                BANNER_AD -> bannerAdId()
                INTER_AD -> interAdId()
                NATIVE_AD -> nativeAdId()
                REWARD_AD -> rewardAdId()
                else -> ""
            }
        }
    }
}