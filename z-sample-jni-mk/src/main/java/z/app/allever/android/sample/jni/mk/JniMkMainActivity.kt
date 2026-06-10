package z.app.allever.android.sample.jni.mk

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/zjnimk/main")
class JniMkMainActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "JniMk"

    override fun getSampleFragment(): Fragment = JniMkMainFragment()
}