package com.allever.business.lib.project

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import com.alibaba.android.arouter.launcher.ARouter
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener

class MyApp: App() {
    override fun init() {
        if (DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)

        initAppsflyer()
    }

    private fun initAppsflyer() {
        val listener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(p0: Map<String?, Any?>?) {

            }

            override fun onConversionDataFail(p0: String?) {
            }

            override fun onAppOpenAttribution(p0: Map<String?, String?>?) {
            }

            override fun onAttributionFailure(p0: String?) {
            }

        }
        AppsFlyerLib.getInstance().init("JJYLVQRfKZm7qgoUCYAr9V", listener, context)
    }
}