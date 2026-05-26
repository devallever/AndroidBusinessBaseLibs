package app.allever.android.lib.ad.core

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.allever.android.lib.ad.core.callback.IAdCallback
import app.allever.android.lib.ad.core.config.AdConfig
import app.allever.android.lib.ad.core.config.AdProviderType
import app.allever.android.lib.ad.core.type.AdType

class AdProviderExampleActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var adContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frameLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        tvStatus = TextView(this).apply {
            text = "Ready - Select Ad Provider"
            textSize = 16f
            setPadding(16, 16, 16, 16)
        }
        frameLayout.addView(tvStatus)

        adContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                200
            ).also { it.topMargin = 200 }
        }
        frameLayout.addView(adContainer)

        val btnContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = 450 }
        }
        frameLayout.addView(btnContainer)

        val buttons = listOf(
            "Init AdMob" to { initAdMob() },
            "Init Pangle" to { initPangle() },
            "Init Bigo" to { initBigo() },
            "Show Interstitial" to { showInterstitial() },
            "Show Reward Video" to { showRewardVideo() },
            "Show Banner" to { showBanner() },
            "Destroy" to { destroyAd() }
        )

        buttons.forEachIndexed { index, (text, action) ->
            Button(this).apply {
                this.text = text
                setOnClickListener { action() }
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.leftMargin = (index % 2) * 450
                    it.topMargin = (index / 2) * 150
                }
            }.let { btnContainer.addView(it) }
        }

        setContentView(frameLayout)
    }

    private fun updateStatus(message: String) {
        tvStatus.text = message
        Log.d("AdProviderExample", message)
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

        AdManager.init(this, config) {
            runOnUiThread {
                updateStatus("✓ AdMob Initialized (Test IDs)")
            }
        }
    }

    private fun initPangle() {
        updateStatus("Initializing Pangle...")

        val config = AdConfig(
            adProviderType = AdProviderType.PANGLE,
            appId = "8025677",
            interstitialAdId = "980088188",
            rewardVideoAdId = "980088192",
            bannerAdId = "980088196"
        )

        AdManager.init(this, config) {
            runOnUiThread {
                updateStatus("✓ Pangle Initialized")
            }
        }
    }

    private fun initBigo() {
        updateStatus("Initializing Bigo...")

        val config = AdConfig(
            adProviderType = AdProviderType.BIGO,
            appId = "10182906",
            interstitialAdId = "10182906-10158798",
            rewardVideoAdId = "10182906-10001431",
            bannerAdId = "10182906-10156618"
        )

        AdManager.init(this, config) {
            runOnUiThread {
                updateStatus("✓ Bigo Initialized")
            }
        }
    }

    private fun showInterstitial() {
        if (!AdManager.isInitialized()) {
            updateStatus("⚠️ Please initialize an ad provider first")
            return
        }

        updateStatus("Loading Interstitial Ad...")
        
        AdManager.loadAndShow(
            activity = this,
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
            activity = this,
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
            activity = this,
            adType = AdType.BANNER,
            container = adContainer,
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

    private fun destroyAd() {
        AdManager.destroy()
        updateStatus("✓ Ad Manager destroyed")
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroy()
    }
}
