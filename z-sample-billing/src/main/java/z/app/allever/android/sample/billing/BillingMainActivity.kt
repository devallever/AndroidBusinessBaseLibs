package z.app.allever.android.sample.billing

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route

@Route(path = "/zbilling/main")
class BillingMainActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun getSampleName(): String = "谷歌内购/订阅/支付"

    override fun getSampleFragment(): Fragment = BillingMainFragment()

}