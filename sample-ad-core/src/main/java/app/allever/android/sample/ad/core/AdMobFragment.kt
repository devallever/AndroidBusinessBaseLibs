package app.allever.android.sample.ad.core

import android.util.Log
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdConfig
import app.allever.android.lib.ad.core.config.AdProviderType
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.provider.admob.AdMobAdProvider
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.util.UIKit.runOnUiThread
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.databinding.FragmentAdmobBinding

class AdMobFragment: BaseFragment<FragmentAdmobBinding, BaseViewModel>() {
    override fun inflate() = FragmentAdmobBinding.inflate(layoutInflater)

    override fun init() {
        AdManager.registerProvider(AdMobAdProvider.PROVIDER_NAME, AdMobAdProvider::class.java)

        mBinding.btnInit.setOnClickListener {
            AdManager.destroy()
            initAdMob()
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

        val config = AdConfig(
            adProviderType = AdProviderType.ADMOB,
            appId = "ca-app-pub-3940256099942544~3347511713",
            interstitialAdId = "ca-app-pub-3940256099942544/1033173712",
            rewardVideoAdId = "ca-app-pub-3940256099942544/5224354917",
            bannerAdId = "ca-app-pub-3940256099942544/6300978111"
        )

        AdManager.init(requireContext(), config) {

            runOnUiThread {
                updateStatus("✓ AdMob Initialized (Test IDs)")
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

        AdManager.loadAndShow(
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

        AdManager.loadAndShow(
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

        AdManager.loadAndShow(
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