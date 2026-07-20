package z.app.allever.android.sample.login

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route

@Route(path = "/zlogin/main")
class LoginMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "登录"

    override fun getSampleFragment(): Fragment = LoginMainFragment()
}