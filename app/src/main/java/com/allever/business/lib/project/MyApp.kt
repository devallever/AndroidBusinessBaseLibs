package com.allever.business.lib.project

import android.content.Context
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.TunnelApp
import com.github.shadowsocks.Core
import com.github.shadowsocks.ShadowsSocksConfig
import com.step.wincash.base.BaseApplication
import com.therouter.TheRouter

class MyApp: App() {

    override fun onCreate() {
        super.onCreate()
        TunnelApp.onCreate()
        BaseApplication.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // TheRouter 推荐在 attachBaseContext 中尽早设置 Debug 模式
        // 框架具备自动初始化能力，无需手动调用 init
        TheRouter.isDebug = (DEBUG)
        base?.let { BaseApplication.attachBaseContext(it) }
    }

    override fun init() {
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