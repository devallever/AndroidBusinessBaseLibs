package z.app.allever.android.sample.safe

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/zsafe/main")
class SafeMainActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "安全"

    override fun getSampleFragment(): Fragment = SafeMainFragment()
}