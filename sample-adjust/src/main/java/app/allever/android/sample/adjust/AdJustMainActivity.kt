package app.allever.android.sample.adjust

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.adjust.databinding.ActivityAdjustMainBinding
import com.therouter.router.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Route(path = "/adjust/main")
class AdJustMainActivity : BaseActivity<ActivityAdjustMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAdjustMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AdJust")

//        AFHelper.trackEvent(AFInAppEventType.LOGIN) {
//            MutableMap.put(AFInAppEventParameterName.CUSTOMER_USER_ID, "123")
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch(Dispatchers.IO) {
            AdJustHelper.init("appToken")
        }
        super.onCreate(savedInstanceState)
    }
}