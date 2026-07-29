package com.allever.compose.project.compose.ad

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.isVisible
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.core.ext.log
import coil.load
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import z.compose.app.allever.android.sample.compose.project.databinding.ZcpNativeAdBannerBigBinding
import z.compose.app.allever.android.sample.compose.project.databinding.ZcpNativeAdBannerSmallBinding

class AdMobNativeActivity : BaseComposeActivity() {


    @Composable
    override fun ContentPage() {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "原生 Banner广告",
                Modifier.padding(10.dp),
                fontWeight = FontWeight.Bold
            )

            SmallNativeAd(Modifier.padding(horizontal = 10.dp))

            Text(
                text = "原生 Banner广告 Big",
                Modifier.padding(10.dp),
                fontWeight = FontWeight.Bold
            )

            BigNativeAd(Modifier.padding(horizontal = 10.dp))

            Text(
                text = "列表 Banner广告",
                Modifier.padding(10.dp),
                fontWeight = FontWeight.Bold
            )

            val list = mutableListOf<Product>().apply {
                add(Product("Banana"))
                add(Product("", true))
                add(Product("Orange"))
            }
            LazyColumn {
                itemsIndexed(list) { index, item ->
                    if (item.isAd) {
                        SmallNativeAd()
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(Color(0x80ff0000))
                            , horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.name,
                                modifier = Modifier
                            )
                        }

                    }
                }
            }

        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun BigNativeAd(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val adId = Constants.NATIVE_TEST_ID
        var requested = false
        AndroidViewBinding(factory = ZcpNativeAdBannerBigBinding::inflate, modifier = modifier) {
            if (requested) {
                return@AndroidViewBinding
            }

            adNativeView.isVisible = false
            adNativeView.also { adNativeView ->
                adNativeView.bodyView = adBody
                adNativeView.iconView = adIcon
                adNativeView.headlineView = adHeadline
                adNativeView.callToActionView = adCta
                adNativeView.mediaView = adMedia
                adNativeView.storeView = adStore
                adNativeView.priceView = adPrice

                kotlin.runCatching {
                    AdLoader.Builder(this@AdMobNativeActivity, adId)
                        .forNativeAd { nativeAd ->
                            adBody.text = nativeAd.body
                            adHeadline.text = nativeAd.headline
                            adIcon.load(nativeAd.icon?.drawable)
//                            Glide.with(adIcon).load(nativeAd.icon?.drawable)
//                                .transform(CenterInside(), RoundedCorners(8)).into(adIcon)
                            adCta.text = nativeAd.callToAction
                            nativeAd.mediaContent?.let {
                                adMedia.mediaContent = it
                            }
                            adStore.text = nativeAd.store
                            adPrice.text = nativeAd.price
                            adNativeView.setNativeAd(nativeAd)
                        }
                        .withAdListener(object : AdListener() {
                            override fun onAdLoaded() {
                                log("big native ad loaded")
                                adNativeView.isVisible = true
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                log("big native ad fail to load: ${error.message}")
                            }

                            override fun onAdClosed() {

                            }
                        })
                }
                    .onSuccess {
                        it.build().loadAd(AdRequest.Builder().build())
                        requested = true
                    }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun SmallNativeAd(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val adId = Constants.NATIVE_TEST_ID
        var requested = false
        AndroidViewBinding(factory = ZcpNativeAdBannerSmallBinding::inflate, modifier = modifier) {
            if (requested) {
                return@AndroidViewBinding
            }

            adNativeView.isVisible = false
            adNativeView.also { adNativeView ->
                adNativeView.bodyView = adBody
                adNativeView.iconView = adIcon
                adNativeView.headlineView = adHeadline
                adNativeView.callToActionView = adCta
                adNativeView.mediaView = adMedia
                adNativeView.storeView = adStore
                adNativeView.priceView = adPrice

                kotlin.runCatching {
                    AdLoader.Builder(this@AdMobNativeActivity, adId)
                        .forNativeAd { nativeAd ->
                            adBody.text = nativeAd.body
                            adHeadline.text = nativeAd.headline
                            adIcon.load(nativeAd.icon?.drawable)
                            adCta.text = nativeAd.callToAction
                            nativeAd.mediaContent?.let {
                                adMedia.mediaContent = it
                            }
                            adStore.text = nativeAd.store
                            adPrice.text = nativeAd.price
                            adNativeView.setNativeAd(nativeAd)
                        }
                        .withAdListener(object : AdListener() {
                            override fun onAdLoaded() {
                                log("small native ad loaded")
                                adNativeView.isVisible = true
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                log("small native ad fail to load: ${error.message}")
                            }

                            override fun onAdClosed() {

                            }
                        })
                }
                    .onSuccess {
                        it.build().loadAd(AdRequest.Builder().build())
                        requested = true
                    }
            }
        }

    }

    data class Product(val name: String, val isAd: Boolean = false)
}

