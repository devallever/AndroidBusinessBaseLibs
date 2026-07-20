package app.allever.android.sample.ad.admob

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.app.App
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.admob.databinding.ActivityAdAdmobMainBinding
import com.therouter.router.Route

@Route(path = "/admob/main")
class AdAdMobMainActivity : BaseActivity<ActivityAdAdmobMainBinding, BaseViewModel>() {


    override fun inflateChildBinding() = ActivityAdAdmobMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("AdMob")

        AdMobManager.init(TestAdConfig(), App.app) {
            AdMobManager.loadBanner(binding.bannerContainer)
            AdMobManager.loadNativeAd(binding.nativeContainer, "native", R.layout.ad_native_small);
            AdMobManager.justLoadInter()
            AdMobManager.justLoadReward()
        }

        binding.btnLoadInter.setOnClickListener {
            AdMobManager.showInter(this)
        }
        binding.btnLoadReward.setOnClickListener {
            AdMobManager.showReward(this)
        }
    }

    override fun onResume() {
        super.onResume()
        AdMobManager.resumeBanner(binding.bannerContainer)
        AdMobManager.resumeNativeBanner("native")
    }

    override fun onPause() {
        super.onPause()
        AdMobManager.pauseBanner(binding.bannerContainer)
        AdMobManager.destroyNativeAd("native")
    }

    override fun onDestroy() {
        super.onDestroy()
        AdMobManager.destroyBanner(binding.bannerContainer)
        AdMobManager.destroyNativeAd("native")
    }
}