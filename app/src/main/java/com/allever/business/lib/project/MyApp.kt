package com.allever.business.lib.project

import app.allever.android.lib.core.app.App
import com.alibaba.android.arouter.launcher.ARouter
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
//            AFHelper.init("JJYLVQRfKZm7qgoUCYAr9V")

        }
    }

    private fun initAdJust() {
        GlobalScope.launch(Dispatchers.IO) {
//            AdJustHelper.init("appToken")
        }
    }
}