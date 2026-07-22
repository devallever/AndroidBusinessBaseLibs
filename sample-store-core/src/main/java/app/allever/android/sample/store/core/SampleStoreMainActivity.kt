package app.allever.android.sample.store.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.SampleMainActivity
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.router.annotation.Route

@Route(path = "/store/main")
class SampleStoreMainActivity: SampleMainActivity<ActivitySampleMainBinding, BaseViewModel>() {
    override fun getSampleName(): String = "存储"

    override fun getSampleFragment(): Fragment = SampleStoreMainFragment()
}