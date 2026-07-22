package z.app.allever.android.sample.ui

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.router.annotation.Route

@Route(path = "/zui/main")
class UIMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "UI"

    override fun getSampleFragment(): Fragment = UIMainFragment()
}