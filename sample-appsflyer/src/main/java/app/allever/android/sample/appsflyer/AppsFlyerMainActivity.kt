package app.allever.android.sample.appsflyer

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.appsflyer.databinding.ActivityAppsFlyerMainBinding
import app.allever.android.lib.router.annotation.Route
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Route(path = "/appsflyer/main")
class AppsFlyerMainActivity : BaseActivity<ActivityAppsFlyerMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAppsFlyerMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AppsFlyer")

        AFHelper.trackEvent(AFInAppEventType.LOGIN) {
            put(AFInAppEventParameterName.CUSTOMER_USER_ID, "123")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch(Dispatchers.IO) {
            AFHelper.init("JJYLVQRfKZm7qgoUCYAr9V")
        }
        super.onCreate(savedInstanceState)
    }
}