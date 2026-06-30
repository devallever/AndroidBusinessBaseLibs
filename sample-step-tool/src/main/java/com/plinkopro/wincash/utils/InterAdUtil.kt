package com.plinkopro.wincash.utils

import com.plinkopro.wincash.BuildConfig
import kotlin.random.Random

object InterAdUtil {
    var isNewUser = false // 是否是新用户
    var openAppTime = 0L  // 第一次打开APP的时间

    var lastSeeAdTime = 0L  // 上次查看广告时间

    fun showAd(): Boolean {
        val time = System.currentTimeMillis()

        if (BuildConfig.LOG_OUTPUT) {
            LogUtil.ad("开始能否展示插屏判断 isNewUser: $isNewUser openAppTime: $openAppTime lastSeeAdTime: $lastSeeAdTime time: $time")
        }
        val isshow = if (isNewUser) {
            when {
                time - openAppTime < 300000 -> {  // 5分钟内打开的APP
                    if (BuildConfig.LOG_OUTPUT) {
                        LogUtil.ad("新人用户 5分钟内打开的APP，不显示插屏广告")
                    }
                    false
                }

                time - lastSeeAdTime > 60000 -> { // 60秒内没有查看过广告
                    if (BuildConfig.LOG_OUTPUT) {
                        LogUtil.ad("新人用户 打开APP超过5分钟且60秒内没有查看过广告，显示插屏广告")
                    }
                    true
                }
                else -> {
                    if (BuildConfig.LOG_OUTPUT) {
                        LogUtil.ad("新人用户 60秒内看过广告，不显示插屏广告")
                    }
                    false
                }
            }
        } else {
            if (BuildConfig.LOG_OUTPUT) {
                LogUtil.ad("非新人用户 是否满足插屏展示条件：${time - lastSeeAdTime > 60 * 1000}")
            }
            time - lastSeeAdTime > 60 * 1000
        }
        return if (isshow) {
            val show = Random.nextBoolean()
            if (BuildConfig.LOG_OUTPUT) {
                LogUtil.ad("满足展示插屏条件，开始随机(50%概率)是否显示，结果为：$show")
            }
            show
        } else {
            false
        }
    }

}