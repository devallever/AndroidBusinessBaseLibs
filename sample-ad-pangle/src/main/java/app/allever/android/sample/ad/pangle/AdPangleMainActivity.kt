package app.allever.android.sample.ad.pangle

import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.pangle.databinding.ActivityAdPangleMainBinding
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/pangle/main")
class AdPangleMainActivity : BaseActivity<ActivityAdPangleMainBinding, BaseViewModel>() {


    override fun inflateChildBinding() = ActivityAdPangleMainBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("Pangle")

        PangleManager.init(TestAdConfig()) {
            PangleManager.loadBanner(binding.bannerContainer)
            PangleManager.loadNativeAd(
                binding.nativeContainer,
                "native",
                R.layout.default_ad_native_fragment_pangle
            );
            PangleManager.justLoadInter()
            PangleManager.justLoadReward()
        }

        binding.btnLoadInter.setOnClickListener {
            PangleManager.showInter(this)
        }
        binding.btnLoadReward.setOnClickListener {
            PangleManager.showReward(this)
        }
    }

    override fun onResume() {
        super.onResume()
//        PangleManager.resumeBanner(binding.bannerContainer)
        PangleManager.resumeNativeBanner("native")
    }

    override fun onPause() {
        super.onPause()
//        AdMobManager.pauseBanner(binding.bannerContainer)
        PangleManager.destroyNativeAd("native")
    }

    override fun onDestroy() {
        super.onDestroy()
        PangleManager.destroyBanner(binding.bannerContainer)
        PangleManager.destroyNativeAd("native")
    }
}