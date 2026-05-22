package app.allever.android.sample.appsflyer

import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.appsflyer.databinding.ActivityAppsFlyerMainBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener

@Route(path = "/sample/appsflyer/main")
class AppsFlyerMainActivity: BaseActivity<ActivityAppsFlyerMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAppsFlyerMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AppsFlyer")

        initFlyer()
    }

    private fun initFlyer() {


        AppsFlyerLib.getInstance().start(this@AppsFlyerMainActivity, "JJYLVQRfKZm7qgoUCYAr9V", object : AppsFlyerRequestListener {
            override fun onSuccess() {
                log(" AppsFlyer start success ")

            }

            override fun onError(p0: Int, p1: String) {
                logE(" AppsFlyer start error $p0 -> $p1")
            }

        })

    }
}