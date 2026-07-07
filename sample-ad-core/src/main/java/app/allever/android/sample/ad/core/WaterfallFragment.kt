package app.allever.android.sample.ad.core

import android.util.Log
import android.widget.TextView
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
        mBinding.btnLoadSplash.setOnClickListener { loadSplashAd() }
        mBinding.btnShowInter.setOnClickListener { showInterstitial() }
        mBinding.btnShowReward.setOnClickListener { showReward() }
        mBinding.btnShowSplash.setOnClickListener { showSplash() }
    }

    private fun registerProviders() {
        appendStatus("Registering providers with waterfall config...")

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
//        AdCore.registerProvider(
//            providerType = AppLovinAdProvider.PROVIDER_NAME,
//            providerClass = AppLovinAdProvider::class.java,
//            config = ProviderConfigConstants.APPLOVIN
//        )

        appendStatus("✓ Registered 3 providers:")
        appendStatus("  [0] ADMOB - Waterfall: ON (Priority 1)")
        appendStatus("  [1] PANGLE - Waterfall: ON (Priority 2)")
        appendStatus("  [2] BIGO - Waterfall: ON (Priority 3)")
        //applovin
        appendStatus("  [3] Applovin - Waterfall: ON (Priority 4)")
        appendStatus("")
    }

    private fun initAllProviders() {
        appendStatus("Initializing all providers (joining pool)...")

        val context = requireContext()

        AdCore.init(context, AdMobAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                appendStatus("✓ ADMOB initialized & in pool")
            }
        }

        AdCore.init(context, PangleAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                appendStatus("✓ PANGLE initialized & in pool")
            }
        }

        AdCore.init(context, BigoAdProvider.PROVIDER_NAME) {
            runOnUiThread {
                appendStatus("✓ BIGO initialized & in pool")
            }
        }

//        AdCore.init(context, AppLovinAdProvider.PROVIDER_NAME) {
//            runOnUiThread {
//                appendStatus("✓ Applovin initialized & in pool")
//                appendStatus("")
//                appendStatus("All providers ready! Now select load mode.")
//                updateCurrentModeDisplay()
//            }
//        }
    }

    private fun setMode(mode: LoadMode) {
        AdCore.setLoadMode(mode)

        val modeName = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE"
            LoadMode.WATERFALL -> "💧 WATERFALL"
            LoadMode.BIDDING -> "⚡️ BIDDING"
        }

        appendStatus("")
        appendStatus("=".repeat(50))
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

            LoadMode.BIDDING -> {
                appendStatus("→ Will try bidding providers in order")
                appendStatus("→ max success wins!")
            }
        }
        appendStatus("=".repeat(50))
        appendStatus("")

        updateCurrentModeDisplay()
    }

    private fun updateCurrentModeDisplay() {
        val mode = AdCore.loadMode
        val modeText = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE (Active Provider Only)"
            LoadMode.WATERFALL -> "💧 WATERFALL (Auto Try All)"
            LoadMode.BIDDING -> "⚡️ BIDDING (Max Success)"
        }

        mBinding.tvCurrentMode.text = "Current Mode: $modeText"
    }

    private fun showWaterfallInfo() {
        val info = AdCore.getWaterfallProvidersInfo()

        appendStatus("")
        appendStatus("─".repeat(50))
        appendStatus(info)
        appendStatus("")
        appendStatus("Active Provider: ${AdCore.getActiveProviderType()}")
        appendStatus("All Initialized: ${AdCore.getInitializedProviders()}")
        appendStatus("─".repeat(50))
        appendStatus("")
    }

    private fun loadInterstitial() {
        if (!AdCore.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        val mode = AdCore.loadMode
        val activeType = AdCore.getActiveProviderType()

        appendStatus("")
        appendStatus("Loading INTERSTITIAL ad...")
        appendStatus("Mode: $mode | Active: $activeType")

        if (mode == LoadMode.WATERFALL) {
            appendStatus("[WATERFALL] Will try providers in order until success...")
        }

        AdCore.loadAd(requireActivity(), AdType.INTERSTITIAL, object : IAdCallback {
            override fun onAdLoadedWithPrice(eCPM: Double) {
                appendStatus("✓ Interstitial LOADED successfully!")
                appendStatus("  Winner: ${AdCore.getActiveProviderType()}")
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
                appendStatus("📺 Interstitial SHOWING from: ${AdCore.getActiveProviderType()}")
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
        if (!AdCore.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        val mode = AdCore.loadMode

        appendStatus("")
        appendStatus("Loading REWARD VIDEO ad...")
        appendStatus("Mode: $mode")

        AdCore.loadAd(requireActivity(), AdType.REWARD_VIDEO, object : IAdCallback {
            override fun onAdLoadedWithPrice(eCPM: Double) {
                appendStatus("✓ Reward Video LOADED!")
                appendStatus("  Winner: ${AdCore.getActiveProviderType()}")
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
        if (!AdCore.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        appendStatus("Showing cached interstitial...")

        AdCore.showAd(
            activity = requireActivity(),
            adType = AdType.INTERSTITIAL,
            callback = object : IAdCallback {
                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    appendStatus("✗ Show failed: $errorMessage")
                }

                override fun onAdShow() {
                    appendStatus("📺 Showing interstitial...")
                }

                override fun onAdClick() {}
                override fun onAdDismiss() {
                    appendStatus("❌ Interstitial dismissed")
                }

                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
            }
        )
    }

    private fun showReward() {
        if (!AdCore.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        appendStatus("Showing cached interstitial...")

        AdCore.showAd(
            activity = requireActivity(),
            adType = AdType.REWARD_VIDEO,
            callback = object : IAdCallback {
                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    appendStatus("✗ Show failed: $errorMessage")
                }

                override fun onAdShow() {
                    appendStatus("📺 Showing interstitial...")
                }

                override fun onAdClick() {}
                override fun onAdDismiss() {
                    appendStatus("❌ Interstitial dismissed")
                }

                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                    appendStatus("🎁 REWARDED: $rewardAmount x $rewardName")
                }
            }
        )
    }

    private fun loadSplashAd() {
        if (!AdCore.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        val mode = AdCore.loadMode
        val activeType = AdCore.getActiveProviderType()

        appendStatus("")
        appendStatus("-".repeat(60))
        appendStatus("Loading SPLASH ad...")
        appendStatus("Mode: $mode | Active: $activeType")

        if (mode == LoadMode.WATERFALL) {
            appendStatus("[WATERFALL] Will try providers in order until success...")
        }

        AdCore.loadAd(requireActivity(), AdType.SPLASH, object : IAdCallback {
            override fun onAdLoadedWithPrice(eCPM: Double) {
                appendStatus("✓ Splash LOADED successfully!")
                appendStatus("  Winner: ${AdCore.getActiveProviderType()}")
                if (mode == LoadMode.WATERFALL) {
                    appendStatus("  [Waterfall stopped at winner]")
                }
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                appendStatus("✗ Splash FAILED: $errorMessage")
                if (mode == LoadMode.WATERFALL) {
                    appendStatus("  [All waterfall providers exhausted]")
                } else {
                    appendStatus("  Provider: $activeType")
                }
            }

            override fun onAdShow() {
                appendStatus("📺 Splash SHOWING from: ${AdCore.getActiveProviderType()}")
            }

            override fun onAdClick() {
                appendStatus("👆 Splash CLICKED")
            }

            override fun onAdDismiss() {
                appendStatus("❌ Splash DISMISSED")
                appendStatus("  [Preloading next splash ad...]")
            }

            override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
        })
    }

    private fun showSplash() {
        if (!AdCore.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        appendStatus("Showing cached splash ad...")

        AdCore.showAd(
            activity = requireActivity(),
            adType = AdType.SPLASH,
            callback = object : IAdCallback {
                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    appendStatus("✗ Show failed: $errorMessage")
                }

                override fun onAdShow() {
                    appendStatus("📺 Showing splash ad...")
                }

                override fun onAdClick() {}
                override fun onAdDismiss() {
                    appendStatus("❌ Splash dismissed")
                }

                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {}
            }
        )
    }

    private fun appendStatus(message: String) {
        val currentText = mBinding.tvStatus.text.toString()
        val newText =
            if (currentText.isEmpty() || currentText == "Ready to start waterfall demo...") {
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
