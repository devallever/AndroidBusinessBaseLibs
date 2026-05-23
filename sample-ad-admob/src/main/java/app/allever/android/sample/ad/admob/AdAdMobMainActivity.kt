package app.allever.android.sample.ad.admob

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.app.App
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.admob.databinding.ActivityAdAdmobMainBinding
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/admob/main")
class AdAdMobMainActivity : BaseActivity<ActivityAdAdmobMainBinding, BaseViewModel>() {


    override fun inflateChildBinding() = ActivityAdAdmobMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AdMob")

        AdManager.init(TestAdConfig(), App.app) {
            AdManager.loadBanner(binding.bannerContainer)
            AdManager.loadNativeAd(binding.nativeContainer, "native", R.layout.ad_native_small);
            AdManager.justLoadInter()
            AdManager.justLoadReward()
        }

        binding.btnLoadInter.setOnClickListener {
            AdManager.showInter(this)
        }
        binding.btnLoadReward.setOnClickListener {
            AdManager.showReward(this)
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.resumeBanner(binding.bannerContainer)
        AdManager.resumeNativeBanner("native")
    }

    override fun onPause() {
        super.onPause()
        AdManager.pauseBanner(binding.bannerContainer)
        AdManager.destroyNativeAd("native")
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroyBanner(binding.bannerContainer)
        AdManager.destroyNativeAd("native")
    }
}