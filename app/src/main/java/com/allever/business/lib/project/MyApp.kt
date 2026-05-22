package com.allever.business.lib.project

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.sample.adjust.AdJustHelper
import app.allever.android.sample.appsflyer.AFHelper
import com.alibaba.android.arouter.launcher.ARouter
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyApp: App() {
    override fun init() {
        if (DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)

        initAppsflyer()
        initAdJust()
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