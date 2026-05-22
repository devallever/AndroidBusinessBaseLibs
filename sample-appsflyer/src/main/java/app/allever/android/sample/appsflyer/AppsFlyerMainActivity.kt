package app.allever.android.sample.appsflyer

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.appsflyer.databinding.ActivityAppsFlyerMainBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerAdNetworkEventType

@Route(path = "/sample/appsflyer/main")
class AppsFlyerMainActivity: BaseActivity<ActivityAppsFlyerMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAppsFlyerMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AppsFlyer")

        AFHelper.trackEvent(AFInAppEventType.LOGIN) {
            put(AFInAppEventParameterName.CUSTOMER_USER_ID, "123")
        }
    }
}