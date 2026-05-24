package app.allever.android.sample.ad.pangle

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
     * https://www.pangleglobal.com/zh/integration/How-to-Test-Pangle-Ads-with-Ad-ID
     */
    fun getAdId(type: Int): String {
        return if (App.DEBUG) {
            when (type) {
                BANNER_AD -> "980088196"
                INTER_AD -> "980088188"
                NATIVE_AD -> "980088216"
                REWARD_AD -> "980088192"
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