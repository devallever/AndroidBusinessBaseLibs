package app.allever.android.sample.adjust

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.adjust.databinding.ActivityAdjustMainBinding
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/adjust/main")
class AdJustMainActivity : BaseActivity<ActivityAdjustMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAdjustMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AdJust")

//        AFHelper.trackEvent(AFInAppEventType.LOGIN) {
//            MutableMap.put(AFInAppEventParameterName.CUSTOMER_USER_ID, "123")
//        }
    }
}