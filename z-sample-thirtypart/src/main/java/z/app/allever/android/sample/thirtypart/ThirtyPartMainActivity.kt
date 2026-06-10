package z.app.allever.android.sample.thirtypart

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/zthirtypart/main")
class ThirtyPartMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "第三方"

    override fun getSampleFragment(): Fragment = ThirtyPartMainFragment()
}