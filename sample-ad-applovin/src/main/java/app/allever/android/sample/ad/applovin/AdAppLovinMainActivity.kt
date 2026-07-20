package app.allever.android.sample.ad.applovin

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.applovin.databinding.ActivityAdApplovinMainBinding
import com.therouter.router.Route

@Route(path = "/applovin/main")
class AdAppLovinMainActivity : BaseActivity<ActivityAdApplovinMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAdApplovinMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AppLovin")
        AppLovinManager.init(TestAdConfig()) {
            log("AppLovinManager init success")
            AppLovinManager.justLoadInter()
            AppLovinManager.justLoadReward()
        }

        binding.btnLoadInter.setOnClickListener {
            AppLovinManager.showInter(this, null)
        }

        binding.btnLoadReward.setOnClickListener {
            AppLovinManager.showReward(this, null)
        }
    }
}