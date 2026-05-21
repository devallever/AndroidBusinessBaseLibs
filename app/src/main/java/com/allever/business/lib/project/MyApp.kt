package com.allever.business.lib.project

import app.allever.android.lib.core.app.App
import com.alibaba.android.arouter.launcher.ARouter

class MyApp: App() {
    override fun init() {
        if (DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }

}