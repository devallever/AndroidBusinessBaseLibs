package app.allever.android.sample.ad.core

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.databinding.ActivityAdCoreMainBinding
import app.allever.android.lib.router.annotation.Route

@Route(path = "/adCore/main")
class AdCoreMainActivity : BaseActivity<ActivityAdCoreMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAdCoreMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AdCoreSample")
        FragmentHelper.addToContainer(
            supportFragmentManager,
            AdCoreMainFragment(),
            R.id.fragmentContainer
        )
    }
}