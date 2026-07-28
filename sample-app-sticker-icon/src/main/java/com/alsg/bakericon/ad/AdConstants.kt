package com.alsg.bakericon.ad

import com.light.icon.adcore.AdUnitId
import com.alsg.bakericon.Build
import com.alsg.bakericon.R

/**
 *@Description
 *@author: zq
 *@date: 2024/1/22
 */
object AdConstants {
    //TODO 随便填写个id初始化后面接入正式的
    val APP_ID = if (Build.DEBUG) "8025677" else "1234567"
    val APP_ICON = R.mipmap.ic_launcher

    val INTER_AD_ID = if (Build.DEBUG) "ca-app-pub-3940256099942544/1033173712" else "ca-app-pub-3940256099942544/1033173712"
    val NATIVE_AD_ID = if (Build.DEBUG) "980088216" else "123456789"

    val INTER_AD = AdUnitId(INTER_AD_ID)


//    val BANNER_AD = AdUnit("980099802")
//    val REWARD_AD = AdUnit("980088192")
//    val NATIVE_AD = AdUnit("980088216")
}