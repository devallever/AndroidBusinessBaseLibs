package z.app.allever.android.sample.designpattern

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route


@Route(path = "/zdesignpattern/main")
class DesignPatternMainActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "设计模式"

    override fun getSampleFragment(): Fragment = DesignPatternMainFragment()


}