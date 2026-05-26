package app.allever.android.sample.ad.core

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.databinding.ActivityAdCoreMainBinding
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/adCore/main")
class AdCoreMainActivity: BaseActivity<ActivityAdCoreMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAdCoreMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AdCoreSample")
    }
}