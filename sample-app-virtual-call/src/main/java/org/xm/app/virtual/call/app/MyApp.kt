package org.xm.app.virtual.call.app

import com.allever.app.virtual.call.BuildConfig
import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.common.app.App
import com.allever.lib.umeng.UMeng
import org.xm.app.virtual.call.ad.AdContract
import org.xm.app.virtual.call.ad.AdFactory

class MyApp : App() {

    override fun onCreate() {
        super.onCreate()

        com.android.absbase.App.setContext(this)

        if (!BuildConfig.DEBUG) {
            UMeng.init(this@MyApp)
        }

        AdChainHelper.init(this@MyApp, AdContract.adData, AdFactory())

//        RecommendGlobal.init(UMeng.getChannel())

        Global.initWallPagerData()
    }
}