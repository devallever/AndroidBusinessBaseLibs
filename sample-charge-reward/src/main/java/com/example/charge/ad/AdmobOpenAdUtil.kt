package com.example.charge.ad

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import app.allever.android.lib.core.app.App
import com.example.charge.ui.activity.HotLaunchActivity
import com.example.charge.ui.activity.LaunchActivity
import com.example.charge.utils.HandlerUtil
import com.example.charge.utils.isSelfClass
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager

/**
 * Admob 开屏广告工具类
 * */
object AdmobOpenAdUtil {
    private var inBackgroundTime = 0L
    private var showTime = 0L

    fun onAppBackgrounded(activity: Activity) {
        //进入后台记录时间
        inBackgroundTime = SystemClock.elapsedRealtime()

        if (App.DEBUG) Log.d("launch_tag", "app 进入后台 记录时间")

        //判断有没有缓存
        val isShowSuccess = AdManager.isCanShowAdmobOpenAd()
        if (!isShowSuccess) {
            AdManager.loadAdmobAppOpenAd(activity, "123456")
        }
    }

    fun onAppForegrounded(activity: Activity) {
        val activityName = activity.javaClass.name
        if (activityName == LaunchActivity::class.java.name) {
            if (App.DEBUG) Log.d("launch_tag", "app 返回前台 还在开屏页")
            return
        }
        if (!activity.isSelfClass()) {
            if (App.DEBUG) Log.d(
                "launch_tag",
                "app 返回前台 正在播放广告 ： $activityName"
            )
            return
        }

        val currentRealTime = SystemClock.elapsedRealtime()
        val backgroundTime = currentRealTime - inBackgroundTime
        if (App.DEBUG) Log.d(
            "launch_tag",
            "app 进入后台时间 ： ${backgroundTime / 1000}秒"
        )

        val holdTime = currentRealTime - showTime
        if (App.DEBUG) Log.d(
            "launch_tag",
            "app 距离上一次展示时间 ： ${holdTime / 1000}秒"
        )

        //进入后台时间大于3秒， 距离上次广告展示超60秒， 进行展示开屏
        if (backgroundTime > 3 * 1000 && holdTime >= 60 * 1000) {
            showOpenAd(activity)
        }
    }

    fun updateShowTime() {
        showTime = SystemClock.elapsedRealtime()
    }

    private fun showOpenAd(activity: Activity) {
        //获取开屏广告缓存
        val isShowSuccess = AdManager.isCanShowAdmobOpenAd()
        if (isShowSuccess) {
            //不延迟可能会报展示失败 The ad can not be shown when app is not in foreground.
            HandlerUtil.main().postDelayed({
                activity.startActivity(Intent(activity, HotLaunchActivity::class.java))
            }, 100)

        } else {
            AdManager.loadAdmobAppOpenAd(activity, "123456")
        }
    }
}