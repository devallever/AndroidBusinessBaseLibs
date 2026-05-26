package app.allever.android.sample.ad.core

import android.util.Log
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.provider.pangle.PangleAdProvider
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.util.UIKit.runOnUiThread
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.config.AdIdConstants
import app.allever.android.sample.ad.core.databinding.FragmentPangleBinding

class PangleFragment: BaseFragment<FragmentPangleBinding, BaseViewModel>() {
    override fun inflate() = FragmentPangleBinding.inflate(layoutInflater)

    override fun init() {
        val config = AdProviderConfig(
            appId = AdIdConstants.Pangle.APP_ID,
            interstitialAdId = AdIdConstants.Pangle.INTERSTITIAL_AD_ID,
            rewardVideoAdId = AdIdConstants.Pangle.REWARD_VIDEO_AD_ID,
            bannerAdId = AdIdConstants.Pangle.BANNER_AD_ID
        )

        AdManager.registerProvider(
            providerType = PangleAdProvider.PROVIDER_NAME,
            providerClass = PangleAdProvider::class.java,
            config = config
        )

        mBinding.btnInit.setOnClickListener {
            AdManager.destroy()
            initPangle()
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

    private fun initPangle() {
        updateStatus("Initializing Pangle...")

        AdManager.init(requireContext(), PangleAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                updateStatus("✓ Pangle Initialized")
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