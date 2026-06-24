package com.allever.business.lib.project

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.ProcessHelper
import app.flash.tunnel.vpn.TunnelApp
import com.alibaba.android.arouter.launcher.ARouter
import com.github.shadowsocks.Core
import com.github.shadowsocks.ShadowsSocksConfig

class MyApp: App() {

    override fun onCreate() {
        initShadowSocks()
        super.onCreate()
        TunnelApp.onCreate()
    }
    override fun init() {
        if (DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }

        ProcessHelper.executeOnMain(this) {
            ARouter.init(this)
//            CsjDjHelper.init()
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
        ShadowsSocksConfig.connectTime = if (App.DEBUG) 45 * 60 * 1000L else 30 * 60 * 1000L
        Core.init(this, MainActivity::class)
    }

}