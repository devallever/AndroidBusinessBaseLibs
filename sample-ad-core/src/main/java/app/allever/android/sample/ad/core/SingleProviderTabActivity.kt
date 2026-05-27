package app.allever.android.sample.ad.core

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.databinding.ActivityProviderTabBinding

class SingleProviderTabActivity: BaseActivity<ActivityProviderTabBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityProviderTabBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AdCoreSample")
        FragmentHelper.addToContainer(
            supportFragmentManager,
            SingleProviderTabFragment(),
            R.id.fragmentContainer
        )
    }
}