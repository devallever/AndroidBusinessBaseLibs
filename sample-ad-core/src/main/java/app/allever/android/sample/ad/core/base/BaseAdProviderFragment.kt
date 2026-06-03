package app.allever.android.sample.ad.core.base

import android.util.Log
import app.allever.android.lib.ad.core.AdCore
import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.util.UIKit.runOnUiThread
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.databinding.FragmentBaseAdProviderBinding

abstract class BaseAdProviderFragment<DB, VM> :
    BaseFragment<FragmentBaseAdProviderBinding, BaseViewModel>() {

    abstract val providerName: String
    abstract val providerConfig: AdProviderConfig

    override fun inflate() = FragmentBaseAdProviderBinding.inflate(layoutInflater)

    override fun init() {
        AdCore.registerProvider(
            providerType = providerName,
            providerClass = getProviderClass(),
            config = providerConfig,
        )

        setupButtons()
    }

    private fun setupButtons() {
        mBinding.btnInit.setOnClickListener {
            AdCore.destroy()
            initProvider()
        }

        mBinding.btnLoadSplash.setOnClickListener { showSplash() }
        mBinding.btnLoadInter.setOnClickListener { showInterstitial() }
        mBinding.btnLoadReward.setOnClickListener { showRewardVideo() }
        mBinding.btnLoadBanner.setOnClickListener { showBanner() }
    }

    protected abstract fun getProviderClass(): Class<out IAdProvider>

    private fun initProvider() {
        updateStatus("Initializing $providerName...")

        AdCore.init(requireContext(), providerName) {
            runOnUiThread {
                updateStatus("✓ $providerName Initialized")
                onProviderInitialized()
            }
        }
    }

    protected open fun onProviderInitialized() {
        AdCore.loadAd(requireActivity(), AdType.SPLASH)
        AdCore.loadAd(requireActivity(), AdType.INTERSTITIAL)
        AdCore.loadAd(requireActivity(), AdType.REWARD_VIDEO)
        AdCore.loadAd(requireActivity(), AdType.BANNER)
    }

    protected fun updateStatus(message: String) {
        mBinding.tvStatus.text = message
        Log.d("AdProviderExample", message)
    }

    private fun showSplash() {
        if (!AdCore.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Splash Ad...")

        AdCore.showAd(
            activity = requireActivity(),
            adType = AdType.SPLASH,
            callback = createAdCallback("Splash")
        )
    }

    private fun showInterstitial() {
        if (!AdCore.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Interstitial Ad...")

        AdCore.showAd(
            activity = requireActivity(),
            adType = AdType.INTERSTITIAL,
            callback = createAdCallback("Interstitial")
        )
    }

    private fun showRewardVideo() {
        if (!AdCore.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Reward Video...")

        AdCore.showAd(
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
        if (!AdCore.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Banner Ad...")

        AdCore.showAd(
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

    private fun createAdCallback(adTypeName: String): IAdCallback {
        return object : IAdCallback {
            override fun onAdLoaded() {
                updateStatus("✓ $adTypeName loaded")
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                updateStatus("✗ $adTypeName failed: $errorMessage")
            }

            override fun onAdShow() {
                updateStatus("✓ $adTypeName showing")
            }

            override fun onAdClick() {
                updateStatus("! $adTypeName clicked")
            }

            override fun onAdDismiss() {
                updateStatus("✓ $adTypeName dismissed")
            }

            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
        }
    }
}
