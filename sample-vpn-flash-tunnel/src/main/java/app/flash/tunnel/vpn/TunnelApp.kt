package app.flash.tunnel.vpn

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Process
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.helper.EventHelper
import app.flash.tunnel.vpn.helper.FirebaseHelper
import app.flash.tunnel.vpn.helper.ReferrerHelper
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.common.Common
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.page.HomeActivity
import app.flash.tunnel.vpn.page.SplashActivity
//import com.facebook.FacebookSdk
import com.github.shadowsocks.Core
import com.github.shadowsocks.ShadowsSocksConfig
import java.io.File
import java.io.IOException

class TunnelApp : Application(), androidx.work.Configuration.Provider by Core {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        val DEBUG = App.DEBUG

        var alreadyInBackground = false
        private var activityCount = 0
        fun currentInBackground() = activityCount == 0
    }

    override fun onCreate() {
        context = this
        ShadowsSocksConfig.notificationMainClz = HomeActivity::class.java
        ShadowsSocksConfig.notificationIcon = R.mipmap.ic_launcher_foreground
        ShadowsSocksConfig.pkg = "com.allever.business.lib.project"
        ShadowsSocksConfig.autoStopMode = true
        ShadowsSocksConfig.appName = getString(R.string.app_name)
        ShadowsSocksConfig.tickerSuccess = getString(R.string.ticker_success)
        ShadowsSocksConfig.notificationTraffic = R.string.traffic
        ShadowsSocksConfig.notificationSpeed = R.string.speed
        ShadowsSocksConfig.connectTime = Constants.CONNECT_TIME
        Core.init(this, SplashActivity::class)
        super.onCreate()

        if (isInMainProcess(this)) {
            EventHelper.launchTimeStart = System.currentTimeMillis()
            Common.init(this)
            ReferrerHelper.init(this)
            EventHelper.init()
            AdHelper.init()
            FirebaseHelper.init()
            TunnelHelper.init(this)
            registerActivityLifecycleCallback()
            if (DEBUG) {
//                FacebookSdk.setIsDebugEnabled(true)
//                FacebookSdk.setCodelessDebugLogEnabled(true)
            }
        }
    }

    fun executeOnMain(application: Application, task: Runnable) {
        if (isInMainProcess(application)) {
            task.run()
        }
    }

    private fun isInMainProcess(
        application: Application,
        processName: String = application.packageName
    ): Boolean {
        return processName == getProcessName(Process.myPid())
    }

    private fun getProcessName(pid: Int): String {
        val reader = try {
            val file = File("/proc/$pid/cmdline")
            val bufferReader = file.bufferedReader()

            bufferReader
        } catch (e: IOException) {
            return ""
        }
        return try {
            reader.use { it.readLine().trim().substringBefore('\u0000') }
        } catch (e: IOException) {
            ""
        }
    }

    private fun registerActivityLifecycleCallback() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(p0: Activity) {
                //remove from onResume to onStart
                //reason: some case just invoke onStart, suddenly onStop,so onStop invoke many times
                activityCount++
                log("onActivityStarted: ${p0.javaClass.simpleName}, count = $activityCount")
            }

            override fun onActivityResumed(activity: Activity) {
                log("onActivityResumed: ${activity::class.java.simpleName}")
            }

            override fun onActivityPaused(activity: Activity) {
                log("onActivityPaused: ${activity::class.java.simpleName}")
            }

            override fun onActivityStopped(p0: Activity) {
                activityCount--
                log("onActivityStopped: ${p0.javaClass.simpleName}, count = $activityCount")
                alreadyInBackground = activityCount == 0
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                log("onActivityDestroyed: ${activity.javaClass.simpleName}, count = $activityCount")
            }
        })
    }
}