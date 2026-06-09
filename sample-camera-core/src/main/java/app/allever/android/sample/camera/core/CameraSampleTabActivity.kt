package app.allever.android.sample.camera.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.camera.proxy.camera2.Camera2ProxyImpl
import app.allever.android.lib.camera.proxy.camerax.CameraXProxyImpl
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.TabActivity
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.common.databinding.ActivityTabBinding
import app.allever.android.lib.core.camera.CameraManager
import app.allever.android.lib.core.camera.CameraProxyImpl
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/camera/main")
class CameraSampleTabActivity : SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun getSampleName(): String = "CameraCore"

    override fun getSampleFragment(): Fragment = CameraSampleListFragment()

}