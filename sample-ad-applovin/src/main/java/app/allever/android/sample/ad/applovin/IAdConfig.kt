package app.allever.android.sample.ad.applovin

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
     *
     */
    fun getAdId(type: Int): String {
        return if (App.DEBUG) {
            when (type) {
                BANNER_AD -> ""
                INTER_AD -> ""
                NATIVE_AD -> ""
                REWARD_AD -> ""
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