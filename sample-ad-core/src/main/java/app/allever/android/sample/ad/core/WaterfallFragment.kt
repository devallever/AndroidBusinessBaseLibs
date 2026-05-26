package app.allever.android.sample.ad.core

import android.util.Log
import android.widget.TextView
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
import app.allever.android.sample.ad.core.databinding.FragmentWaterfallBinding

class WaterfallFragment : BaseFragment<FragmentWaterfallBinding, BaseViewModel>() {
    
    override fun inflate() = FragmentWaterfallBinding.inflate(layoutInflater)

    override fun init() {
        setupUI()
    }

    private fun setupUI() {
        mBinding.btnRegisterProviders.setOnClickListener { registerProviders() }
        
        mBinding.btnInitAll.setOnClickListener { initAllProviders() }
        
        mBinding.btnModeSingle.setOnClickListener { setMode(LoadMode.SINGLE) }
        mBinding.btnModeWaterfall.setOnClickListener { setMode(LoadMode.WATERFALL) }
        
        mBinding.btnShowWaterfallInfo.setOnClickListener { showWaterfallInfo() }
        
        mBinding.btnLoadInter.setOnClickListener { loadInterstitial() }
        mBinding.btnLoadReward.setOnClickListener { loadRewardVideo() }
        mBinding.btnShowInter.setOnClickListener { showInterstitial() }
        mBinding.btnShowReward.setOnClickListener { showReward() }
    }

    private fun registerProviders() {
        appendStatus("Registering providers with waterfall config...")
        
        AdManager.registerProvider(
            providerType = AdMobAdProvider.PROVIDER_NAME,
            providerClass = AdMobAdProvider::class.java,
            config = AdProviderConfig(
                appId = "ca-app-pub-3940256099942544~3347511713",
                interstitialAdId = "ca-app-pub-3940256099942544/1033173712",
                rewardVideoAdId = "ca-app-pub-3940256099942544/5224354917",
                supportWaterfall = false  // ✅ 参与瀑布流（第1优先级）
            )
        )

        AdManager.registerProvider(
            providerType = PangleAdProvider.PROVIDER_NAME,
            providerClass = PangleAdProvider::class.java,
            config = AdProviderConfig(
                appId = "8025677",
                interstitialAdId = "980088188",
                rewardVideoAdId = "980088192",
                supportWaterfall = true  // ✅ 参与瀑布流（第2优先级）
            )
        )

        AdManager.registerProvider(
            providerType = BigoAdProvider.PROVIDER_NAME,
            providerClass = BigoAdProvider::class.java,
            config = AdProviderConfig(
                appId = "10182906",
                interstitialAdId = "10182906-10158798",
                rewardVideoAdId = "10182906-10001431",
                supportWaterfall = true  // ❌ 不参与瀑布流（手动切换）
            )
        )

        appendStatus("✓ Registered 3 providers:")
        appendStatus("  [0] ADMOB - Waterfall: ON (Priority 1)")
        appendStatus("  [1] PANGLE - Waterfall: ON (Priority 2)")
        appendStatus("  [2] BIGO - Waterfall: OFF (Manual only)")
        appendStatus("")
    }

    private fun initAllProviders() {
        appendStatus("Initializing all providers (joining pool)...")
        
        val context = requireContext()
        
        AdManager.init(context, AdMobAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                appendStatus("✓ ADMOB initialized & in pool")
            }
        }

        AdManager.init(context, PangleAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                appendStatus("✓ PANGLE initialized & in pool")
            }
        }

        AdManager.init(context, BigoAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                appendStatus("✓ BIGO initialized & in pool")
                appendStatus("")
                appendStatus("All providers ready! Now select load mode.")
                updateCurrentModeDisplay()
            }
        }
    }

    private fun setMode(mode: LoadMode) {
        AdManager.setLoadMode(mode)
        
        val modeName = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE"
            LoadMode.WATERFALL -> "💧 WATERFALL"
        }
        
        appendStatus("")
        appendStatus("=" .repeat(50))
        appendStatus("Load Mode Changed: $modeName")
        
        when (mode) {
            LoadMode.SINGLE -> {
                appendStatus("→ Will use ACTIVE provider only")
                appendStatus("→ Switch with switchProvider()")
            }
            LoadMode.WATERFALL -> {
                appendStatus("→ Will try waterfall providers in order")
                appendStatus("→ First success wins!")
            }
        }
        appendStatus("=" .repeat(50))
        appendStatus("")
        
        updateCurrentModeDisplay()
    }

    private fun updateCurrentModeDisplay() {
        val mode = AdManager.loadMode
        val modeText = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE (Active Provider Only)"
            LoadMode.WATERFALL -> "💧 WATERFALL (Auto Try All)"
        }
        
        mBinding.tvCurrentMode.text = "Current Mode: $modeText"
    }

    private fun showWaterfallInfo() {
        val info = AdManager.getWaterfallProvidersInfo()
        
        appendStatus("")
        appendStatus("─".repeat(50))
        appendStatus(info)
        appendStatus("")
        appendStatus("Active Provider: ${AdManager.getActiveProviderType()}")
        appendStatus("All Initialized: ${AdManager.getInitializedProviders()}")
        appendStatus("─".repeat(50))
        appendStatus("")
    }

    private fun loadInterstitial() {
        if (!AdManager.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        val mode = AdManager.loadMode
        val activeType = AdManager.getActiveProviderType()
        
        appendStatus("")
        appendStatus("Loading INTERSTITIAL ad...")
        appendStatus("Mode: $mode | Active: $activeType")
        
        if (mode == LoadMode.WATERFALL) {
            appendStatus("[WATERFALL] Will try providers in order until success...")
        }

        AdManager.loadAd(requireActivity(), AdType.INTERSTITIAL, null, object : IAdCallback {
            override fun onAdLoaded() {
                appendStatus("✓ Interstitial LOADED successfully!")
                appendStatus("  Winner: ${AdManager.getActiveProviderType()}")
                if (mode == LoadMode.WATERFALL) {
                    appendStatus("  [Waterfall stopped at winner]")
                }
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                appendStatus("✗ Interstitial FAILED: $errorMessage")
                if (mode == LoadMode.WATERFALL) {
                    appendStatus("  [All waterfall providers exhausted]")
                } else {
                    appendStatus("  Provider: $activeType")
                }
            }

            override fun onAdShow() { 
                appendStatus("📺 Interstitial SHOWING from: ${AdManager.getActiveProviderType()}") 
            }
            
            override fun onAdClick() { 
                appendStatus("👆 Interstitial CLICKED") 
            }
            
            override fun onAdDismiss() { 
                appendStatus("❌ Interstitial DISMISSED") 
            }
            
            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
        })
    }

    private fun loadRewardVideo() {
        if (!AdManager.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        val mode = AdManager.loadMode
        
        appendStatus("")
        appendStatus("Loading REWARD VIDEO ad...")
        appendStatus("Mode: $mode")

        AdManager.loadAd(requireActivity(), AdType.REWARD_VIDEO, null,object : IAdCallback {
            override fun onAdLoaded() {
                appendStatus("✓ Reward Video LOADED!")
                appendStatus("  Winner: ${AdManager.getActiveProviderType()}")
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                appendStatus("✗ Reward Video FAILED: $errorMessage")
            }

            override fun onAdShow() { 
                appendStatus("📺 Reward Video SHOWING") 
            }
            
            override fun onAdClick() { 
                appendStatus("👆 Reward Video CLICKED") 
            }
            
            override fun onAdDismiss() { 
                appendStatus("❌ Reward Video DISMISSED") 
            }
            
            override fun onAdRewarded(rewardAmount: Int, rewardName: String) { 
                appendStatus("🎁 REWARDED: $rewardAmount x $rewardName") 
            }
        })
    }

    private fun showInterstitial() {
        if (!AdManager.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        appendStatus("Showing cached interstitial...")
        
        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.INTERSTITIAL,
            callback = object : IAdCallback {
                override fun onAdLoaded() {}
                override fun onAdFail(errorCode: Int, errorMessage: String) { 
                    appendStatus("✗ Show failed: $errorMessage") 
                }
                override fun onAdShow() { 
                    appendStatus("📺 Showing interstitial...") 
                }
                override fun onAdClick() { }
                override fun onAdDismiss() { 
                    appendStatus("❌ Interstitial dismissed") 
                }
                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
            }
        )
    }

    private fun showReward() {
        if (!AdManager.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        appendStatus("Showing cached interstitial...")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.REWARD_VIDEO,
            callback = object : IAdCallback {
                override fun onAdLoaded() {}
                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    appendStatus("✗ Show failed: $errorMessage")
                }
                override fun onAdShow() {
                    appendStatus("📺 Showing interstitial...")
                }
                override fun onAdClick() { }
                override fun onAdDismiss() {
                    appendStatus("❌ Interstitial dismissed")
                }
                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                    appendStatus("🎁 REWARDED: $rewardAmount x $rewardName")
                }
            }
        )
    }

    private fun appendStatus(message: String) {
        val currentText = mBinding.tvStatus.text.toString()
        val newText = if (currentText.isEmpty() || currentText == "Ready to start waterfall demo...") {
            message
        } else {
            "$currentText\n$message"
        }
        
        mBinding.tvStatus.text = newText
        Log.d("WaterfallDemo", message)
        
        mBinding.tvStatus.post {
            mBinding.scrollView.fullScroll(TextView.FOCUS_DOWN)
        }
    }
}
