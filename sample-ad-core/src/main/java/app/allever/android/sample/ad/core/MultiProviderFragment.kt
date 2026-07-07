package app.allever.android.sample.ad.core

import android.util.Log
import app.allever.android.lib.ad.core.AdCore
import app.allever.android.lib.ad.core.AdCore.LoadMode
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.provider.admob.AdMobAdProvider
import app.allever.android.lib.ad.provider.bigo.BigoAdProvider
import app.allever.android.lib.ad.provider.pangle.PangleAdProvider
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.util.UIKit.runOnUiThread
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.config.ProviderConfigConstants
import app.allever.android.sample.ad.core.databinding.FragmentMultiProviderBinding

class MultiProviderFragment : BaseFragment<FragmentMultiProviderBinding, BaseViewModel>() {

    override fun inflate() = FragmentMultiProviderBinding.inflate(layoutInflater)

    override fun init() {
        setupUI()
        registerAllProviders()
    }

    private fun setupUI() {
        mBinding.btnInitAll.setOnClickListener { initAllProviders() }

        mBinding.btnSwitchAdMob.setOnClickListener { switchToProvider(AdMobAdProvider.PROVIDER_NAME) }
        mBinding.btnSwitchPangle.setOnClickListener { switchToProvider(PangleAdProvider.PROVIDER_NAME) }
        mBinding.btnSwitchBigo.setOnClickListener { switchToProvider(BigoAdProvider.PROVIDER_NAME) }

        mBinding.btnModeSingle.setOnClickListener { setLoadMode(LoadMode.SINGLE) }
        mBinding.btnModeWaterfall.setOnClickListener { setLoadMode(LoadMode.WATERFALL) }
        mBinding.btnShowWaterfallInfo.setOnClickListener { showWaterfallInfo() }

        mBinding.btnLoadInter.setOnClickListener { loadInterstitial() }
        mBinding.btnLoadReward.setOnClickListener { loadRewardVideo() }

        mBinding.btnDestroyActive.setOnClickListener { destroyActiveProvider() }
        mBinding.btnReinitActive.setOnClickListener { reinitActiveProvider() }
    }

    private fun registerAllProviders() {
        AdCore.registerProvider(
            providerType = AdMobAdProvider.PROVIDER_NAME,
            providerClass = AdMobAdProvider::class.java,
            config = ProviderConfigConstants.ADMOB
        )

        AdCore.registerProvider(
            providerType = PangleAdProvider.PROVIDER_NAME,
            providerClass = PangleAdProvider::class.java,
            config = ProviderConfigConstants.PANGLE
        )

        AdCore.registerProvider(
            providerType = BigoAdProvider.PROVIDER_NAME,
            providerClass = BigoAdProvider::class.java,
            config = ProviderConfigConstants.BIGO
        )

        updateStatus("✓ All 3 providers registered (ADMOB✓PANGLE✓BIGO○ waterfall)")
        Log.d("MultiProvider", "Registered: ${AdCore.getRegisteredProvidersInfo()}")
    }

    private fun initAllProviders() {
        updateStatus("Initializing all providers...")

        val context = requireContext()
        AdCore.init(context, AdMobAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                updateStatus("✓ AdMob initialized")
                logProviderStatus()
            }
        }

        AdCore.init(context, PangleAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                updateStatus("✓ Pangle initialized")
                logProviderStatus()
            }
        }

        AdCore.init(context, BigoAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                updateStatus("✓ Bigo initialized!")
                logProviderStatus()

                AdCore.loadAd(requireContext(), AdType.INTERSTITIAL)
                AdCore.loadAd(requireContext(), AdType.REWARD_VIDEO)
            }
        }
    }

    private fun switchToProvider(providerType: String) {
        val success = AdCore.switchToProvider(providerType)

        if (success) {
            updateStatus("✓ Switched to: $providerType")

            runOnUiThread {
                AdCore.loadAd(requireContext(), AdType.INTERSTITIAL)
                AdCore.loadAd(requireContext(), AdType.REWARD_VIDEO)
            }
        } else {
            if (!AdCore.isProviderInitialized(providerType)) {
                updateStatus("⚠️ $providerType not initialized yet")
            } else {
                updateStatus("✗ Failed to switch to: $providerType")
            }
        }

        logProviderStatus()
    }

    private fun loadInterstitial() {
        if (!AdCore.isInitialized()) {
            updateStatus("⚠️ No active provider")
            return
        }

        updateStatus("Loading Interstitial from: ${AdCore.getActiveProviderType()}")

        AdCore.showAd(
            activity = requireActivity(),
            adType = AdType.INTERSTITIAL,
            callback = object : IAdCallback {
                override fun onAdLoadedWithPrice(eCPM: Double) {
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

    private fun loadRewardVideo() {
        if (!AdCore.isInitialized()) {
            updateStatus("⚠️ No active provider")
            return
        }

        updateStatus("Loading Reward from: ${AdCore.getActiveProviderType()}")

        AdCore.showAd(
            activity = requireActivity(),
            adType = AdType.REWARD_VIDEO,
            callback = object : IAdCallback {
                override fun onAdLoadedWithPrice(eCPM: Double) {
                    updateStatus("✓ Reward loaded")
                }

                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    updateStatus("✗ Reward failed: $errorMessage")
                }

                override fun onAdShow() {
                    updateStatus("✓ Reward showing")
                }

                override fun onAdClick() {
                    updateStatus("! Reward clicked")
                }

                override fun onAdDismiss() {
                    updateStatus("✓ Reward dismissed")
                }

                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                    updateStatus("🎁 Rewarded: $rewardAmount $rewardName")
                }
            }
        )
    }

    private fun destroyActiveProvider() {
        val activeType = AdCore.getActiveProviderType()
        if (activeType == null) {
            updateStatus("⚠️ No active provider to destroy")
            return
        }

        AdCore.destroyProvider(activeType)
        updateStatus("✗ Destroyed: $activeType")
        logProviderStatus()
    }

    private fun reinitActiveProvider() {
        val activeType = AdCore.getActiveProviderType()
        if (activeType == null) {
            updateStatus("⚠️ No active provider to reinitialize")
            return
        }

        updateStatus("Re-initializing: $activeType...")

        AdCore.init(requireContext(), activeType, forceReinit = true) {
            runOnUiThread {
                updateStatus("✓ Re-initialized: $activeType")
                logProviderStatus()
            }
        }
    }

    private fun updateStatus(message: String) {
        mBinding.tvStatus.text = message
        Log.d("MultiProvider", message)
    }

    private fun logProviderStatus() {
        val sb = StringBuilder()
        sb.appendLine("=== Provider Status ===")
        sb.appendLine("Active: ${AdCore.getActiveProviderType()}")
        sb.appendLine("Mode: ${AdCore.loadMode}")
        sb.appendLine("Initialized: ${AdCore.getInitializedProviders()}")

        Log.d("MultiProvider", sb.toString())
    }

    private fun setLoadMode(mode: LoadMode) {
        AdCore.setLoadMode(mode)

        val modeText = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE"
            LoadMode.WATERFALL -> "💧 WATERFALL"
            LoadMode.BIDDING -> "⚡ BIDDING"
        }

        mBinding.tvCurrentMode.text = "Mode: $modeText"
        updateStatus("Load mode changed to: $modeText")
    }

    private fun showWaterfallInfo() {
        val info = AdCore.getWaterfallProvidersInfo()
        updateStatus(info)
    }
}
