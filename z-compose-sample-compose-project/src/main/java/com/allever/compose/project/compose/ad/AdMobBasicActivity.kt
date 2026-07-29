package com.allever.compose.project.compose.ad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.allever.compose.core.log
import com.allever.compose.core.ui.ComposeProjectTheme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobBasicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeProjectTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "内置Banner广告",
                        Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold
                    )

                    AndroidView(modifier = Modifier.fillMaxWidth(), factory = {
                        AdView(it).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = Constants.BANNER_TEST_ID
                            loadAd(AdRequest.Builder().build())
                        }
                    })

                    Text(
                        text = "插屏广告", Modifier.padding(10.dp), fontWeight = FontWeight.Bold
                    )

                    Button(modifier = Modifier.padding(0.dp), onClick = {
                        InterstitialAd.load(this@AdMobBasicActivity,
                            Constants.INTERSTITIAL_TEST_ID,
                            AdRequest.Builder().build(),
                            object : InterstitialAdLoadCallback() {
                                override fun onAdLoaded(interAd: InterstitialAd) {
                                    log("ad loaded")
                                    interAd.show(this@AdMobBasicActivity)
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    log("ad fail to load: ${error.message}")
                                }
                            })
                    }) {
                        Text(text = "加载插屏广告")
                    }

                    Text(
                        text = "激励广告", Modifier.padding(10.dp), fontWeight = FontWeight.Bold
                    )

                    Button(modifier = Modifier.padding(0.dp), onClick = {
                        RewardedAd.load(
                            this@AdMobBasicActivity,
                            Constants.REWARDED_TEST_ID,
                            AdRequest.Builder().build(),
                            object :
                                RewardedAdLoadCallback() {
                                override fun onAdLoaded(rewardAd: RewardedAd) {
                                    log("reward ad loaded: ")
                                    rewardAd.show(this@AdMobBasicActivity) {

                                    }
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    log("reward ad fail to load: ${error.message}")

                                }
                            })
                    }) {
                        Text(text = "加载激励广告")
                    }

                }
            }
        }
    }
}