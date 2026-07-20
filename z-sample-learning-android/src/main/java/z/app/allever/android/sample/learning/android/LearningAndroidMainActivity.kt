package z.app.allever.android.sample.learning.android

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route

@Route(path = "/zlearningandroid/main")
class LearningAndroidMainActivity :
    SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "学习Android"

    override fun getSampleFragment(): Fragment = LearningAndroidMainFragment()
}