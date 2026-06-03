package app.allever.android.sample.permission

import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/permission/main")
class PermissionMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName() = "Permission"

    override fun getSampleFragment() = PermissionMainFragment()
}