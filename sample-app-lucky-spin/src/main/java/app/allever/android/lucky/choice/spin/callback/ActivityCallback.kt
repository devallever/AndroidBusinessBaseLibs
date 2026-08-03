package app.allever.android.lucky.choice.spin.callback

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import app.allever.android.lucky.choice.spin.log

open class ActivityCallback(
    private val onAppGoesToForeground: (activity: Activity) -> Unit,
): ActivityLifecycleCallbacks {

    private var activityCount = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        log("onActivityStarted: ${activity.javaClass.simpleName}")
        log("onActivityStarted: activityCount = ${activityCount}")
        if (activityCount == 0) {
            // App comes to foreground
            onAppGoesToForeground(activity)
            log("AppGoesToForeground")
        }
        activityCount++
    }

    override fun onActivityResumed(activity: Activity) {
        log("onActivityResumed: ${activity.javaClass.simpleName}")
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        log("onActivityStopped: ${activity.javaClass.simpleName}")
        activityCount--
        if (activityCount == 0) {
            // App goes to background
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {
        log("onActivityDestroyed: ${activity.javaClass.simpleName}")
    }
}