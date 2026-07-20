package z.app.allever.android.sample.cleaner

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route

/**
 * https://gitee.com/antonyhuang/DeepCleanProject
 */
@Route(path = "/zcleaner/main")
class CleanerMainActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun getSampleName(): String = "清理大师(Demo)"

    override fun getSampleFragment(): Fragment = CleanerMainFragment()

}