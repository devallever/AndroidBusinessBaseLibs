package app.allever.android.sample.ad.core

import android.util.Log
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.ad.core.AdManager
import app.allever.android.lib.ad.core.AdManager.LoadMode
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdProviderConfig
import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.ad.provider.admob.AdMobAdProvider
import app.allever.android.lib.ad.provider.bigo.BigoAdProvider
import app.allever.android.lib.ad.provider.pangle.PangleAdProvider
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.util.UIKit.runOnUiThread
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.config.AdIdConstants
import app.allever.android.sample.ad.core.databinding.FragmentMultiProviderBinding
import kotlinx.coroutines.launch

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
        AdManager.registerProvider(
            providerType = AdMobAdProvider.PROVIDER_NAME,
            providerClass = AdMobAdProvider::class.java,
            config = AdProviderConfig(
                appId = AdIdConstants.AdMob.APP_ID,
                splashAdId = AdIdConstants.AdMob.SPLASH_AD_ID,
                interstitialAdId = AdIdConstants.AdMob.INTERSTITIAL_AD_ID,
                rewardVideoAdId = AdIdConstants.AdMob.REWARD_VIDEO_AD_ID,
                bannerAdId = AdIdConstants.AdMob.BANNER_AD_ID,
                supportWaterfall = true
            )
        )

        AdManager.registerProvider(
            providerType = PangleAdProvider.PROVIDER_NAME,
            providerClass = PangleAdProvider::class.java,
            config = AdProviderConfig(
                appId = AdIdConstants.Pangle.APP_ID,
                splashAdId = AdIdConstants.Pangle.SPLASH_AD_ID,
                interstitialAdId = AdIdConstants.Pangle.INTERSTITIAL_AD_ID,
                rewardVideoAdId = AdIdConstants.Pangle.REWARD_VIDEO_AD_ID,
                bannerAdId = AdIdConstants.Pangle.BANNER_AD_ID,
                supportWaterfall = true
            )
        )

        AdManager.registerProvider(
            providerType = BigoAdProvider.PROVIDER_NAME,
            providerClass = BigoAdProvider::class.java,
            config = AdProviderConfig(
                appId = AdIdConstants.Bigo.APP_ID,
                splashAdId = AdIdConstants.Bigo.SPLASH_AD_ID,
                interstitialAdId = AdIdConstants.Bigo.INTERSTITIAL_AD_ID,
                rewardVideoAdId = AdIdConstants.Bigo.REWARD_VIDEO_AD_ID,
                bannerAdId = AdIdConstants.Bigo.BANNER_AD_ID,
                supportWaterfall = false
            )
        )

        updateStatus("✓ All 3 providers registered (ADMOB✓PANGLE✓BIGO○ waterfall)")
        Log.d("MultiProvider", "Registered: ${AdManager.getRegisteredProvidersInfo()}")
    }

    private fun initAllProviders() {
        updateStatus("Initializing all providers...")
        
        val context = requireContext()
        AdManager.init(context, AdMobAdProvider.PROVIDER_NAME) {
            runOnUiThread { 
                updateStatus("✓ AdMob initialized")
                logProviderStatus()
            }
        }

        AdManager.init(context, PangleAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                updateStatus("✓ Pangle initialized")
                logProviderStatus()
            }
        }

        AdManager.init(context, BigoAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                updateStatus("✓ Bigo initialized!")
                logProviderStatus()

                AdManager.loadAd(requireActivity(), AdType.INTERSTITIAL)
                AdManager.loadAd(requireActivity(), AdType.REWARD_VIDEO)
            }
        }
    }

    private fun switchToProvider(providerType: String) {
        val success = AdManager.switchProvider(providerType)
        
        if (success) {
            updateStatus("✓ Switched to: $providerType")
            
            runOnUiThread {
                AdManager.loadAd(requireActivity(), AdType.INTERSTITIAL)
                AdManager.loadAd(requireActivity(), AdType.REWARD_VIDEO)
            }
        } else {
            if (!AdManager.isProviderInitialized(providerType)) {
                updateStatus("⚠️ $providerType not initialized yet")
            } else {
                updateStatus("✗ Failed to switch to: $providerType")
            }
        }
        
        logProviderStatus()
    }

    private fun loadInterstitial() {
        if (!AdManager.isInitialized()) {
            updateStatus("⚠️ No active provider")
            return
        }

        updateStatus("Loading Interstitial from: ${AdManager.getActiveProviderType()}")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.INTERSTITIAL,
            callback = object : IAdCallback {
                override fun onAdLoaded() { updateStatus("✓ Interstitial loaded") }
                override fun onAdFail(errorCode: Int, errorMessage: String) { 
                    updateStatus("✗ Interstitial failed: $errorMessage") 
                }
                override fun onAdShow() { updateStatus("✓ Interstitial showing") }
                override fun onAdClick() { updateStatus("! Interstitial clicked") }
                override fun onAdDismiss() { updateStatus("✓ Interstitial dismissed") }
                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
            }
        )
    }

    private fun loadRewardVideo() {
        if (!AdManager.isInitialized()) {
            updateStatus("⚠️ No active provider")
            return
        }

        updateStatus("Loading Reward from: ${AdManager.getActiveProviderType()}")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.REWARD_VIDEO,
            callback = object : IAdCallback {
                override fun onAdLoaded() { updateStatus("✓ Reward loaded") }
                override fun onAdFail(errorCode: Int, errorMessage: String) { 
                    updateStatus("✗ Reward failed: $errorMessage") 
                }
                override fun onAdShow() { updateStatus("✓ Reward showing") }
                override fun onAdClick() { updateStatus("! Reward clicked") }
                override fun onAdDismiss() { updateStatus("✓ Reward dismissed") }
                override fun onAdRewarded(rewardAmount: Int, rewardName: String) { 
                    updateStatus("🎁 Rewarded: $rewardAmount $rewardName") 
                }
            }
        )
    }

    private fun destroyActiveProvider() {
        val activeType = AdManager.getActiveProviderType()
        if (activeType == null) {
            updateStatus("⚠️ No active provider to destroy")
            return
        }

        AdManager.destroyProvider(activeType)
        updateStatus("✗ Destroyed: $activeType")
        logProviderStatus()
    }

    private fun reinitActiveProvider() {
        val activeType = AdManager.getActiveProviderType()
        if (activeType == null) {
            updateStatus("⚠️ No active provider to reinitialize")
            return
        }

        updateStatus("Re-initializing: $activeType...")
        
        AdManager.init(requireContext(), activeType, forceReinit = true) {
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
        sb.appendLine("Active: ${AdManager.getActiveProviderType()}")
        sb.appendLine("Mode: ${AdManager.loadMode}")
        sb.appendLine("Initialized: ${AdManager.getInitializedProviders()}")
        
        Log.d("MultiProvider", sb.toString())
    }

    private fun setLoadMode(mode: LoadMode) {
        AdManager.setLoadMode(mode)
        
        val modeText = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE"
            LoadMode.WATERFALL -> "💧 WATERFALL"
            LoadMode.BIDDING -> "⚡ BIDDING"
        }
        
        mBinding.tvCurrentMode.text = "Mode: $modeText"
        updateStatus("Load mode changed to: $modeText")
    }

    private fun showWaterfallInfo() {
        val info = AdManager.getWaterfallProvidersInfo()
        updateStatus(info)
    }
}
