package z.app.allever.android.sample.material.design

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/zmaterialdesign/main")
class MaterialDesignMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "Material Design"

    override fun getSampleFragment(): Fragment = MaterialDesignMainListFragment()
}