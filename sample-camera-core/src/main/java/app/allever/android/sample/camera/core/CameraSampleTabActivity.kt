package app.allever.android.sample.camera.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.therouter.router.Route

@Route(path = "/camera/main")
class CameraSampleTabActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun getSampleName(): String = "CameraCore"

    override fun getSampleFragment(): Fragment = CameraSampleListFragment()

}