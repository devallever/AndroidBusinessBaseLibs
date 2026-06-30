package com.plinkopro.wincash.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.plinkopro.wincash.utils.AdmobOpenAdUtil
import com.plinkopro.wincash.utils.MusicUtil
import com.plinkopro.wincash.utils.isSelfClass

class AppLifecycleCallback : Application.ActivityLifecycleCallbacks {
    private var activityCount = 0
    private var isInBackground = false

    private var activityStack = ArrayDeque<Activity>()
    companion object {
        var topActivity: Activity? = null
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        activityStack.addFirst(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activityCount == 0) { //应用在前台
            if (isInBackground) { //从后台回到前台
                //判断栈顶的activity是不是自身的类, 过滤广告的类
                val first = activityStack.firstOrNull()
                if (first != null && first.isSelfClass()) {
                    AdmobOpenAdUtil.onAppForegrounded(activity)
                }
            }

            if (activity.isSelfClass()) {
                MusicUtil.play()
            } else {
                MusicUtil.pause()
            }
        }
        activityCount++

    }

    override fun onActivityResumed(activity: Activity) {
        topActivity = activity
        if (activity.isSelfClass()) {
            MusicUtil.play()
        } else {
            MusicUtil.pause()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        if (activityCount == 0) { //在后台
            isInBackground = true
            AdmobOpenAdUtil.onAppBackgrounded(activity)

            MusicUtil.pause()
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        activityStack.remove(activity)
        if (activityStack.isEmpty()) {
            topActivity = null
        }
    }
}