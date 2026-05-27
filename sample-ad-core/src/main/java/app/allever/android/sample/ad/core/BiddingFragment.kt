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
import app.allever.android.sample.ad.core.config.AdIdConstants
import app.allever.android.sample.ad.core.databinding.FragmentBiddingBinding

class BiddingFragment : BaseFragment<FragmentBiddingBinding, BaseViewModel>() {
    
    override fun inflate() = FragmentBiddingBinding.inflate(layoutInflater)

    override fun init() {
        setupUI()
    }

    private fun setupUI() {
        mBinding.btnRegisterProviders.setOnClickListener { registerProviders() }
        
        mBinding.btnInitAll.setOnClickListener { initAllProviders() }
        
        mBinding.btnModeBidding.setOnClickListener { setMode(LoadMode.BIDDING) }
        
        mBinding.btnShowBiddingInfo.setOnClickListener { showBiddingInfo() }
        
        mBinding.btnLoadInter.setOnClickListener { loadInterstitial() }
        mBinding.btnLoadReward.setOnClickListener { loadRewardVideo() }
        mBinding.btnShowInter.setOnClickListener { showInterstitial() }
        mBinding.btnShowReward.setOnClickListener { showReward() }
    }

    private fun registerProviders() {
        appendStatus("Registering providers with bidding config...")
        appendStatus("")
        appendStatus("=" .repeat(60))
        appendStatus("🎲 BIDDING SIMULATION MODE")
        appendStatus("=" .repeat(60))
        appendStatus("")
        appendStatus("📌 Simulation Rules:")
        appendStatus("  1. ALL providers have supportBidding=true (for testing)")
        appendStatus("  2. Each provider generates RANDOM price for simulation")
        appendStatus("  3. Price ranges (simulated eCPM):")
        appendStatus("     • ADMOB:   $1.00 - $5.00")
        appendStatus("     • PANGLE:  $2.00 - $6.00")
        appendStatus("     • BIGO:    $3.00 - $7.00")
        appendStatus("  4. Winner: HIGHEST random price wins!")
        appendStatus("")
        appendStatus("🔄 AUTO RE-BIDDING:")
        appendStatus("  ✅ When ad is CLOSED → Auto re-bids ALL providers")
        appendStatus("  ✅ New winner selected and cached for next show")
        appendStatus("  ✅ Every ad view may have DIFFERENT winner!")
        appendStatus("")
        appendStatus("⚡ CACHE-FIRST STRATEGY (NEW!):")
        appendStatus("  ✅ Check cache BEFORE network request")
        appendStatus("  ✅ If valid cache exists → INSTANT response (0ms!)")
        appendStatus("  ✅ If cache expired/missing → Normal loading process")
        appendStatus("  📌 Try: Load → Show → Close → Load again (should be instant)")
        appendStatus("")
        appendStatus("🔄 SMART PRELOAD STRATEGY:")
        appendStatus("  ✅ Preload happens ONLY after ad is CLOSED by user")
        appendStatus("  ✅ No wasted requests - we preload only when needed")
        appendStatus("  ✅ Flow: Show Ad → User Close → Auto Preload Next → Cache Ready")
        appendStatus("")
        appendStatus("")
        
        AdManager.registerProvider(
            providerType = AdMobAdProvider.PROVIDER_NAME,
            providerClass = AdMobAdProvider::class.java,
            config = AdProviderConfig(
                appId = AdIdConstants.AdMob.APP_ID,
                interstitialAdId = AdIdConstants.AdMob.INTERSTITIAL_AD_ID,
                rewardVideoAdId = AdIdConstants.AdMob.REWARD_VIDEO_AD_ID,
                supportWaterfall = false,
                supportBidding = true,  
                biddingTimeout = 5000L
            )
        )
        appendStatus("✓ [0] ADMOB - Bidding: ✅ ON")
        appendStatus("    Simulated price range: $1.00 - $5.00")

        AdManager.registerProvider(
            providerType = PangleAdProvider.PROVIDER_NAME,
            providerClass = PangleAdProvider::class.java,
            config = AdProviderConfig(
                appId = AdIdConstants.Pangle.APP_ID,
                interstitialAdId = AdIdConstants.Pangle.INTERSTITIAL_AD_ID,
                rewardVideoAdId = AdIdConstants.Pangle.REWARD_VIDEO_AD_ID,
                supportWaterfall = false,
                supportBidding = true,  
                biddingTimeout = 5000L
            )
        )
        appendStatus("✓ [1] PANGLE - Bidding: ✅ ON")
        appendStatus("    Simulated price range: $2.00 - $6.00")

        AdManager.registerProvider(
            providerType = BigoAdProvider.PROVIDER_NAME,
            providerClass = BigoAdProvider::class.java,
            config = AdProviderConfig(
                appId = AdIdConstants.Bigo.APP_ID,
                interstitialAdId = AdIdConstants.Bigo.INTERSTITIAL_AD_ID,
                rewardVideoAdId = AdIdConstants.Bigo.REWARD_VIDEO_AD_ID,
                supportWaterfall = false,
                supportBidding = true,   
                biddingTimeout = 5000L
            )
        )
        appendStatus("✓ [2] BIGO - Bidding: ✅ ON")
        appendStatus("    Simulated price range: $3.00 - $7.00")
        appendStatus("")
        appendStatus("=" .repeat(60))
        appendStatus("")
    }

    private fun initAllProviders() {
        appendStatus("Initializing all providers for bidding...")
        
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
                appendStatus("All providers ready! Set mode to BIDDING.")
                updateCurrentModeDisplay()
            }
        }
    }

    private fun setMode(mode: LoadMode) {
        AdManager.setLoadMode(mode)
        
        val modeName = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE"
            LoadMode.WATERFALL -> "💧 WATERFALL"
            LoadMode.BIDDING -> "💰 BIDDING (Price-Based)"
        }
        
        appendStatus("")
        appendStatus("=".repeat(60))
        appendStatus("Load Mode Changed: $modeName")
        
        when (mode) {
            LoadMode.SINGLE -> {
                appendStatus("→ Will use ACTIVE provider only")
            }
            LoadMode.WATERFALL -> {
                appendStatus("→ Will try waterfall providers in order")
            }
            LoadMode.BIDDING -> {
                appendStatus("→ Will PARALLEL load ALL providers (3 providers)")
                appendStatus("→ Each generates RANDOM price for simulation")
                appendStatus("→ Winner: HIGHEST random eCPM wins!")
                appendStatus("→ Timeout: 5 seconds max wait time")
                appendStatus("")
                appendStatus("🎲 All 3 providers participate in bidding simulation:")
                appendStatus("   ADMOB ($1-5) vs PANGLE ($2-6) vs BIGO ($3-7)")
            }
        }
        appendStatus("=".repeat(60))
        appendStatus("")
        
        updateCurrentModeDisplay()
    }

    private fun updateCurrentModeDisplay() {
        val mode = AdManager.loadMode
        val modeText = when (mode) {
            LoadMode.SINGLE -> "🎯 SINGLE (Active Provider Only)"
            LoadMode.WATERFALL -> "💧 WATERFALL (Auto Try All)"
            LoadMode.BIDDING -> "💰 BIDDING (Parallel + Best Price)"
        }
        
        mBinding.tvCurrentMode.text = "Current Mode: $modeText"
    }

    private fun showBiddingInfo() {
        val info = AdManager.getBiddingProvidersInfo()
        
        appendStatus("")
        appendStatus("-".repeat(50))
        appendStatus(info)
        appendStatus("")
        appendStatus("Active Provider: ${AdManager.getActiveProviderType()}")
        appendStatus("All Initialized: ${AdManager.getInitializedProviders()}")
        appendStatus("-".repeat(50))
        appendStatus("")
    }

    private fun loadInterstitial() {
        if (!AdManager.isInitialized()) {
            appendStatus("⚠️ No active provider! Initialize first.")
            return
        }

        val mode = AdManager.loadMode
        
        appendStatus("")
        appendStatus("-".repeat(60))
        appendStatus("Loading INTERSTITIAL ad...")
        appendStatus("Mode: $mode")
        appendStatus("Cache-First: ${if (AdManager.cacheFirstEnabled) "✅ ON" else "❌ OFF"}")
        
        if (mode == LoadMode.BIDDING) {
            if (AdManager.cacheFirstEnabled) {
                appendStatus("[BIDDING] Will check cache FIRST before bidding...")
                appendStatus("[BIDDING] If cache HIT → instant response!")
                appendStatus("[BIDDING] If cache MISS → start parallel requests to ALL 3 providers")
            } else {
                appendStatus("[BIDDING] Starting parallel requests to ALL 3 providers...")
                appendStatus("[BIDDING] Each provider will generate RANDOM simulated price")
                appendStatus("[BIDDING] Waiting for responses or timeout...")
            }
        }

        AdManager.loadAd(requireActivity(), AdType.INTERSTITIAL, null, object : IAdCallback {
            
            override fun onAdLoadedWithPrice(eCPM: Double) {
                appendStatus("✅ Interstitial LOADED via BIDDING SIMULATION!")
                appendStatus("  🏆 WINNER: ${AdManager.getActiveProviderType()}")
                appendStatus("  💰 Winning Price: $$${String.format("%.2f", eCPM)} eCPM (SIMULATED)")
                appendStatus("  [Random price generated for testing]")
                appendStatus("-".repeat(60))
            }

            override fun onAdLoaded() {
                appendStatus("✅ Interstitial LOADED successfully!")
                
                if (AdManager.cacheFirstEnabled) {
                    appendStatus("  ⚡ CACHE HIT! Served from cache (instant!)")
                    appendStatus("  Provider: ${AdManager.getActiveProviderType()}")
                    appendStatus("  [No network request needed - using cached ad]")
                } else {
                    appendStatus("  Provider: ${AdManager.getActiveProviderType()}")
                    appendStatus("  (Normal load - no caching)")
                }
                
                appendStatus("-".repeat(60))
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                appendStatus("✗ Interstitial FAILED: $errorMessage")
                if (mode == LoadMode.BIDDING) {
                    appendStatus("  [All bidding providers failed or timed out]")
                    appendStatus("  [Or no bidding-enabled providers available]")
                } else {
                    appendStatus("  Provider: ${AdManager.getActiveProviderType()}")
                }
                appendStatus("-".repeat(60))
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
        appendStatus("-".repeat(60))
        appendStatus("Loading REWARD VIDEO ad...")
        appendStatus("Mode: $mode")

        if (mode == LoadMode.BIDDING) {
            appendStatus("[BIDDING] Starting parallel requests to ALL 3 providers...")
            appendStatus("[BIDDING] Each provider will generate RANDOM simulated price")
        }

        AdManager.loadAd(requireActivity(), AdType.REWARD_VIDEO, null, object : IAdCallback {
            
            override fun onAdLoadedWithPrice(eCPM: Double) {
                appendStatus("✅ Reward Video LOADED via BIDDING SIMULATION!")
                appendStatus("  🏆 WINNER: ${AdManager.getActiveProviderType()}")
                appendStatus("  💰 Winning Price: $$${String.format("%.2f", eCPM)} eCPM (SIMULATED)")
                appendStatus("-".repeat(60))
            }

            override fun onAdLoaded() {
                appendStatus("✓ Reward Video LOADED!")
                appendStatus("  Provider: ${AdManager.getActiveProviderType()}")
                appendStatus("  (No bidding - loaded in single/waterfall mode)")
                appendStatus("-".repeat(60))
            }

            override fun onAdFail(errorCode: Int, errorMessage: String) {
                appendStatus("✗ Reward Video FAILED: $errorMessage")
                if (mode == LoadMode.BIDDING) {
                    appendStatus("  [All bidding providers failed]")
                    appendStatus("  [Or no bidding-enabled providers available]")
                }
                appendStatus("-".repeat(60))
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

        appendStatus("Showing cached reward video...")

        AdManager.showAd(
            activity = requireActivity(),
            adType = AdType.REWARD_VIDEO,
            callback = object : IAdCallback {
                override fun onAdLoaded() {}
                override fun onAdFail(errorCode: Int, errorMessage: String) {
                    appendStatus("✗ Show failed: $errorMessage")
                }
                override fun onAdShow() {
                    appendStatus("📺 Showing reward video...")
                }
                override fun onAdClick() { }
                override fun onAdDismiss() {
                    appendStatus("❌ Reward video dismissed")
                }
                override fun onAdRewarded(rewardAmount: Int, rewardName: String) {
                    appendStatus("🎁 REWARDED: $rewardAmount x $rewardName")
                }
            }
        )
    }

    private fun appendStatus(message: String) {
        val currentText = mBinding.tvStatus.text.toString()
        val newText = if (currentText.isEmpty() || currentText == "Ready to start bidding demo...") {
            message
        } else {
            "$currentText\n$message"
        }
        
        mBinding.tvStatus.text = newText
        Log.d("BiddingDemo", message)
        
        mBinding.tvStatus.post {
            mBinding.scrollView.fullScroll(TextView.FOCUS_DOWN)
        }
    }
}
