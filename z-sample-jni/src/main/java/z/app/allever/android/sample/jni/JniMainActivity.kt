package z.app.allever.android.sample.jni

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route

@Route(path = "/zjni/main")
class JniMainActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "Jni"

    override fun getSampleFragment(): Fragment = JniMainFragment()
}