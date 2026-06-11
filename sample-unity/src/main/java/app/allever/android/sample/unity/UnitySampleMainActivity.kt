package app.allever.android.sample.unity

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/unity/main")
class UnitySampleMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun init() {
        super.init()
        UnityHelper.initUnityPlayer(applicationContext)
    }
    override fun getSampleName(): String = "Unity"

    override fun getSampleFragment(): Fragment = UnitySampleMainFragment()
}