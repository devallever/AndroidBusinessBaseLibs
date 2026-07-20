package app.allever.android.sample.ad.bigo

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.bigo.databinding.ActivityAdBigoMainBinding
import com.therouter.router.Route

@Route(path = "/bigo/main")
class AdBigoMainActivity : BaseActivity<ActivityAdBigoMainBinding, BaseViewModel>() {
    override fun inflateChildBinding() = ActivityAdBigoMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("Bigo")
        BigoManager.init(TestAdConfig()) {
            BigoManager.loadBanner(binding.bannerContainer)
            BigoManager.loadNativeAd(binding.nativeContainer, "native")
            BigoManager.justLoadInter()
            BigoManager.justLoadReward()
        }

        binding.btnLoadInter.setOnClickListener {
            BigoManager.showInter(this)
        }
        binding.btnLoadReward.setOnClickListener {
            BigoManager.showReward(this)
        }
    }


    override fun onPause() {
        super.onPause()
        BigoManager.destroyNativeAd("native")
    }

    override fun onResume() {
        super.onResume()
        BigoManager.resumeNativeBanner("native")
    }

    override fun onDestroy() {
        super.onDestroy()
        BigoManager.destroyBanner(binding.bannerContainer)
        BigoManager.destroyNativeAd("native")
    }
}