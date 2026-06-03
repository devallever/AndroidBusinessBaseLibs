package app.allever.android.lib.common

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.databinding.ActivitySampleMainBinding
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel

abstract class SampleMainActivity<VB, VM> :
    BaseActivity<ActivitySampleMainBinding, BaseViewModel>() {

    override fun inflateChildBinding() = ActivitySampleMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar(getSampleName())
        FragmentHelper.addToContainer(
            supportFragmentManager, getSampleFragment(), R.id.fragmentContainer
        )
    }

    abstract fun getSampleName(): String
    abstract fun getSampleFragment(): Fragment

}