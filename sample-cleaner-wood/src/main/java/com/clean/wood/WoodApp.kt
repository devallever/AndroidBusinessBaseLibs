package com.clean.wood

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import app.allever.android.lib.core.app.App
import com.clean.wood.data.AdManager
import com.clean.wood.data.ReferManager
import com.clean.wood.data.model.AppItem
import java.lang.ref.WeakReference
import java.util.Stack

@SuppressLint("StaticFieldLeak")
object WoodApp {
    val activityStack: Stack<WeakReference<Activity>> = Stack()

    lateinit var context: Context
    val junkSize by lazy { MutableLiveData<Double>() }
    val appInfoLost by lazy {
        mutableListOf<AppItem>()
    }
    var alreadyInBackground = false
    private var activityCount = 0
    fun currentInBackground() = activityCount == 0

    fun init() {
        context = App.context
        ReferManager.ins.checkRefer()
        AdManager.ins.init(App.context)
        registerActivityLifecycleCallback()
    }

    private fun registerActivityLifecycleCallback() {
        App.app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(p0: Activity) {
                activityStack.push(WeakReference(p0))
                //remove from onResume to onStart
                //reason: some case just invoke onStart, suddenly onStop,so onStop invoke many times
                activityCount++
//                log("onActivityStarted: ${p0.javaClass.simpleName}${p0.hashCode()}, count = $activityCount")
            }

            override fun onActivityResumed(activity: Activity) {
//                log("onActivityResumed: ${activity::class.java.simpleName}${activity.hashCode()}")
            }

            override fun onActivityPaused(activity: Activity) {
//                log("onActivityPaused: ${activity::class.java.simpleName}${activity.hashCode()}")
            }

            override fun onActivityStopped(p0: Activity) {
                if (activityStack.isNotEmpty()) {
                    activityStack.pop()
                }
                activityCount--
//                log("onActivityStopped: ${p0.javaClass.simpleName}${p0.hashCode()}, count = $activityCount")
                alreadyInBackground = activityCount == 0
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
//                log("onActivityDestroyed: ${activity.javaClass.simpleName}${activity.hashCode()}, count = $activityCount")
            }
        })
    }
}