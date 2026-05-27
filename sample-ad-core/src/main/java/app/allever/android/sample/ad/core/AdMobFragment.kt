package app.allever.android.sample.ad.core

import android.util.Log
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.provider.admob.AdMobAdProvider
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.util.UIKit.runOnUiThread
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.config.AdIdConstants
import app.allever.android.sample.ad.core.databinding.FragmentAdmobBinding

class AdMobFragment: BaseFragment<FragmentAdmobBinding, BaseViewModel>() {
    override fun inflate() = FragmentAdmobBinding.inflate(layoutInflater)

    override fun init() {
        val config = AdProviderConfig(
            appId = AdIdConstants.AdMob.APP_ID,
            splashAdId = AdIdConstants.AdMob.SPLASH_AD_ID,
            interstitialAdId = AdIdConstants.AdMob.INTERSTITIAL_AD_ID,
            rewardVideoAdId = AdIdConstants.AdMob.REWARD_VIDEO_AD_ID,
            bannerAdId = AdIdConstants.AdMob.BANNER_AD_ID
        )

        AdManager.registerProvider(
            providerType = AdMobAdProvider.PROVIDER_NAME,
            providerClass = AdMobAdProvider::class.java,
            config = config
        )

        mBinding.btnInit.setOnClickListener {
            AdManager.destroy()
            initAdMob()
        }

        mBinding.btnLoadSplash.setOnClickListener {
            showSplash()
        }

        mBinding.btnLoadInter.setOnClickListener {
            showInterstitial()
        }

        mBinding.btnLoadReward.setOnClickListener {
            showRewardVideo()
        }

        mBinding.btnLoadBanner.setOnClickListener {
            showBanner()
        }

    }

    private fun initAdMob() {
        updateStatus("Initializing AdMob...")

        AdManager.init(requireContext(), AdMobAdProvider.PROVIDER_NAME) {

            runOnUiThread {
                updateStatus("✓ AdMob Initialized (Test IDs)")
                AdManager.loadAd(requireActivity(), AdType.SPLASH)
                AdManager.loadAd(requireActivity(), AdType.INTERSTITIAL)
                AdManager.loadAd(requireActivity(), AdType.REWARD_VIDEO)
                AdManager.loadAd(requireActivity(), AdType.BANNER)
            }
        }
    }

    private fun updateStatus(message: String) {
        mBinding.tvStatus.text = message
        Log.d("AdProviderExample", message)
    }

    private fun showSplash() {
        if (!AdManager.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Splash Ad...")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.SPLASH,
            callback = object : IAdCallback {
                override fun onAdLoaded() {
                    updateStatus("✓ Splash loaded")
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    updateStatus("✗ Splash failed: $errorMessage")
                }

                override fun onAdShow() {
                    updateStatus("✓ Splash showing")
                }

                override fun onAdClick() {
                    updateStatus("! Splash clicked")
                }

                override fun onAdDismiss() {
                    updateStatus("✓ Splash dismissed")
                }

                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
            }
        )
    }

    private fun showInterstitial() {
        if (!AdManager.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Interstitial Ad...")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.INTERSTITIAL,
            callback = object : IAdCallback {
                override fun onAdLoaded() {
                    updateStatus("✓ Interstitial loaded")
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    updateStatus("✗ Interstitial failed: $errorMessage")
                }

                override fun onAdShow() {
                    updateStatus("✓ Interstitial showing")
                }

                override fun onAdClick() {
                    updateStatus("! Interstitial clicked")
                }

                override fun onAdDismiss() {
                    updateStatus("✓ Interstitial dismissed")
                }

                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
            }
        )
    }

    private fun showRewardVideo() {
        if (!AdManager.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Reward Video...")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.REWARD_VIDEO,
            callback = object : IAdCallback {
                override fun onAdLoaded() {
                    updateStatus("✓ Reward video loaded")
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    updateStatus("✗ Reward video failed: $errorMessage")
                }

                override fun onAdShow() {
                    updateStatus("✓ Reward video showing")
                }

                override fun onAdClick() {
                    updateStatus("! Reward video clicked")
                }

                override fun onAdDismiss() {
                    updateStatus("✓ Reward video dismissed")
                }

                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                    updateStatus("🎁 Rewarded: $rewardAmount $rewardName")
                }
            }
        )
    }

    private fun showBanner() {
        if (!AdManager.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Banner Ad...")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.BANNER,
            container = mBinding.bannerContainer,
            callback = object : IAdCallback {
                override fun onAdLoaded() {
                    updateStatus("✓ Banner loaded")
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    updateStatus("✗ Banner failed: $errorMessage")
                }

                override fun onAdShow() {
                    updateStatus("✓ Banner showing in container")
                }

                override fun onAdClick() {
                    updateStatus("! Banner clicked")
                }
            }
        )
    }
}