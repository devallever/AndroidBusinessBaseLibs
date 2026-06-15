package com.allever.business.lib.project

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.ProcessHelper
import app.allever.android.sample.adjust.AdJustHelper
import app.allever.android.sample.appsflyer.AFHelper
//import app.allever.android.sample.dj.csj.CsjDjHelper
import com.alibaba.android.arouter.launcher.ARouter
import com.example.charge.ChargeApp
import com.github.shadowsocks.Core
import com.github.shadowsocks.ShadowsSocksConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApp: App() {

    override fun onCreate() {
        initShadowSocks()
        super.onCreate()
    }
    override fun init() {
        if (DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }

        ProcessHelper.executeOnMain(this) {
            ARouter.init(this)

            initAppsflyer()
            initAdJust()
//            CsjDjHelper.init()
            ChargeApp.init()
        }

    }

    @Suppress("MISSING_DEPENDENCY_SUPERCLASS_WARNING")
    private fun initShadowSocks() {
        ShadowsSocksConfig.notificationMainClz = MainActivity::class.java
        ShadowsSocksConfig.notificationIcon = R.mipmap.ic_launcher_round
        ShadowsSocksConfig.pkg = BuildConfig.APPLICATION_ID
        ShadowsSocksConfig.autoStopMode = true
        ShadowsSocksConfig.appName = getString(R.string.app_name)
        ShadowsSocksConfig.tickerSuccess = getString(R.string.ticker_success)
        ShadowsSocksConfig.notificationTraffic = R.string.traffic
        ShadowsSocksConfig.notificationSpeed = R.string.speed
        ShadowsSocksConfig.connectTime = if (BuildConfig.DEBUG) 45 * 60 * 1000L else 30 * 60 * 1000L
        Core.init(this, MainActivity::class)
    }

    private fun initAppsflyer() {
        GlobalScope.launch(Dispatchers.IO) {
            AFHelper.init("JJYLVQRfKZm7qgoUCYAr9V")

        }
    }

    private fun initAdJust() {
        GlobalScope.launch(Dispatchers.IO) {
            AdJustHelper.init("appToken")
        }
    }
}