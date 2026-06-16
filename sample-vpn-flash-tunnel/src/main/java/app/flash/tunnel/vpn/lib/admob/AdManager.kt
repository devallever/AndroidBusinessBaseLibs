package app.flash.tunnel.vpn.lib.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ComponentActivity
import androidx.core.view.children
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


object AdManager {
    private var mCacheNativeAd = mutableListOf<NativeAd>()

    //tag_adUnit
    private var mShowingNativeAdCache = HashMap<String, NativeAd>()
    private var mInterAdCache: InterstitialAd? = null
    private var mRewardAdCache: RewardedAd? = null
    fun init(context: Context) {
        MobileAds.initialize(context) {
            it.adapterStatusMap.map {
                log("init admob ${it.value.initializationState.name}")
            }
        }
    }

    fun loadBanner(adUnit: String, adContainer: ViewGroup, adCallback: AdCallback?) {
        log("loadBanner: adUnit -> $adUnit")
        destroyBanner(adContainer)

        // Create a new ad view.
        adCallback?.onStart()
        val adView = AdView(adContainer.context)
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            adContainer.context,
            getScreenWidth(adContainer.context)
        )
        adView.setAdSize(adSize)

        adView.adUnitId = adUnit
        adView.onPaidEventListener = OnPaidEventListener {
            log("loadBanner: onPaidEvent")
            adCallback?.onAdPaid(it)
        }
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                log("loadBanner: onAdLoaded")
                adCallback?.onLoaded(adView)
            }

            override fun onAdClicked() {
                log("loadBanner: onAdClicked")
                adCallback?.onClicked()
            }

            override fun onAdClosed() {
                log("loadBanner: onAdClosed")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                log("loadBanner: onAdFailedToLoad -> ${error.code}: ${error.message}")
                adCallback?.onFailedToLoad(error.code, "${error.message}")
            }

            override fun onAdImpression() {
                log("loadBanner: onAdImpression")
                adCallback?.onShow()
            }

            override fun onAdOpened() {
                log("loadBanner: onAdOpened")
            }

        }


        // Create an ad request.
        val adRequest = AdRequest.Builder().build()


        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    fun resumeBanner(adContainer: ViewGroup) {
        adContainer.children.map {
            if (it is AdView) {
                it.resume()
            }
        }
    }

    fun pauseBanner(adContainer: ViewGroup) {
        adContainer.children.map {
            if (it is AdView) {
                it.pause()
            }
        }
    }

    fun destroyBanner(adContainer: ViewGroup) {
        adContainer.children.map {
            if (it is AdView) {
                it.destroy()
            }
        }
        adContainer.removeAllViews()
    }

    @SuppressLint("RestrictedApi")
    fun loadInter(
        adUnit: String,
        activity: ComponentActivity,
        showAlways: Boolean = false,
        adCallback: AdCallback?
    ) {
        log("loadInter: adUnit -> $adUnit")

        adCallback?.onStart()
        InterstitialAd.load(
            activity,
            adUnit,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    log("loadInter: onAdLoaded")
                    mInterAdCache = interstitialAd
                    adCallback?.onLoaded(interstitialAd)
                    interstitialAd.setOnPaidEventListener {
                        log("loadInter: onPaidEvent")
                        adCallback?.onAdPaid(it)
                    }
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                log("loadInter: onAdClicked")
                                adCallback?.onClicked()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                log("loadInter: onAdDismissedFullScreenContent")
                                adCallback?.onDismiss()
                            }

                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                log("loadInter: onAdFailedToShowFullScreenContent: ${error.message}")
                                adCallback?.onShowFailed(error.code, error.message)
                            }

                            override fun onAdImpression() {
                                log("loadInter: onAdImpression")
                                adCallback?.onShow()
                                mInterAdCache = null
                            }

                            override fun onAdShowedFullScreenContent() {
                                log("loadInter: ")
                            }
                        }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    log("loadInter: onAdFailedToLoad -> ${adError.code}: ${adError.message}")
                    adCallback?.onFailedToLoad(adError.code, "${adError.message}")
                }
            })
    }

    @SuppressLint("RestrictedApi")
    fun loadReward(
        adUnit: String,
        activity: ComponentActivity,
        adCallback: AdCallback?
    ) {
        log("loadReward: $adUnit")
        adCallback?.onStart()
        RewardedAd.load(
            activity,
            adUnit,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    log("loadReward: onAdLoaded")
                    mRewardAdCache = ad
                    adCallback?.onLoaded(ad)
                    ad.setOnPaidEventListener {
                        log("loadReward: onPaidEvent")
                        adCallback?.onAdPaid(it)
                    }
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            log("loadReward: onAdClicked")
                            adCallback?.onClicked()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            log("loadReward: onAdDismissedFullScreenContent")
                            adCallback?.onDismiss()
                        }

                        override fun onAdFailedToShowFullScreenContent(aderror: AdError) {
                            log("loadReward: onAdFailedToShowFullScreenContent: ${aderror.message}")
                            adCallback?.onShowFailed(aderror.code, aderror.message)
                        }

                        override fun onAdImpression() {
                            log("loadReward: onAdImpression")
                            adCallback?.onShow()
                            mRewardAdCache = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            log("loadReward: onAdShowedFullScreenContent")
                        }
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    log("loadReward: onAdFailedToLoad -> ${adError.code}: ${adError.message}")
                    adCallback?.onFailedToLoad(adError.code, adError.message)
                }
            })
    }

    fun obtainNative(): NativeAd? {
        if (mCacheNativeAd.isEmpty()) {
            log("obtainNative: no cache")
            return null
        }
        log("obtainNative: has cache ad")
        val nativeAd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            mCacheNativeAd.removeFirst()
        } else {
            mCacheNativeAd.removeAt(0)
        }
        log("obtainNative: cache size after remove ${mCacheNativeAd.size}")
        return nativeAd
    }

    fun loadNative(context: Context, adUnit: String, adCallback: AdCallback?) {
        log("loadNative: $adUnit")

        if (mCacheNativeAd.isNotEmpty()) {
            log("loadNative: hasCache")
            adCallback?.onCache()
            return
        }

        adCallback?.onStart()
        val adLoader = AdLoader.Builder(context, adUnit)
            .forNativeAd { nativeAd: NativeAd ->
                //set from outside
//                nativeAd.setOnPaidEventListener {
//
//                }
                // Show the ad.
                log("loadNative: onNativeAdLoaded")
                mCacheNativeAd.add(nativeAd)
                log("loadNative: cache size = ${mCacheNativeAd.size}")
                adCallback?.onLoaded(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    log("loadNative: onAdFailedToLoad -> ${adError.code}: ${adError.message}")
                    adCallback?.onFailedToLoad(adError.code, adError.message)
                }

                override fun onAdClicked() {
                    log("loadNative: onAdClicked")
                    adCallback?.onClicked()
                }

                override fun onAdClosed() {
                    log("loadNative: onAdClosed")
                    adCallback?.onDismiss()
                }

                override fun onAdImpression() {
                    log("loadNative: onAdImpression")
                    adCallback?.onShow()
                }

            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }


    fun showNativeAd(
        nativeAd: NativeAd,
        adUnit: String,
        tag: String,
        adContainer: ViewGroup,
        adLayout: Int,
        adCallback: AdCallback? = null
    ) {
        destroyNative(adContainer)
        val key = "${tag}_${adUnit}"
        mShowingNativeAdCache.remove(key)

        val context = adContainer.context
        val nativeAdView: NativeAdView =
            LayoutInflater.from(context).inflate(adLayout, null) as NativeAdView

        var primaryView: TextView? = null
        var secondaryView: TextView? = null
        var ratingBar: RatingBar? = null
        var tertiaryView: TextView? = null
        var iconView: ImageView? = null
        var mediaView: MediaView? = null
        var callToActionView: Button? = null
        var background: ConstraintLayout? = null

        //find view by id

        primaryView = nativeAdView.findViewById(R.id.primary);
        secondaryView = nativeAdView.findViewById(R.id.secondary);
        tertiaryView = nativeAdView.findViewById(R.id.body);

        ratingBar = nativeAdView.findViewById(R.id.rating_bar);
        ratingBar.setEnabled(false);

        callToActionView = nativeAdView.findViewById(R.id.cta);
        iconView = nativeAdView.findViewById(R.id.icon);
        mediaView = nativeAdView.findViewById(R.id.media_view);
        background = nativeAdView.findViewById(R.id.background);


        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val starRating = nativeAd.starRating
        val icon = nativeAd.icon
        val secondaryText: String

        nativeAdView.callToActionView = callToActionView
        nativeAdView.headlineView = primaryView
        nativeAdView.mediaView = mediaView
        secondaryView?.visibility = VISIBLE
        if (adHasOnlyStore(nativeAd)) {
            nativeAdView.storeView = secondaryView
            secondaryText = store ?: ""
        } else if (!TextUtils.isEmpty(advertiser)) {
            nativeAdView.advertiserView = secondaryView
            secondaryText = advertiser ?: ""
        } else {
            secondaryText = ""
        }

        primaryView?.setText(headline)
        callToActionView?.setText(cta)

        //  Set the secondary view to be the star rating if available.
        if (starRating != null && starRating > 0) {
            secondaryView?.visibility = GONE
            ratingBar?.visibility = VISIBLE
            ratingBar?.rating = starRating.toFloat()
            nativeAdView.starRatingView = ratingBar
        } else {
            secondaryView?.text = secondaryText
            secondaryView?.visibility = VISIBLE
            ratingBar?.visibility = GONE
        }

        if (icon != null) {
            iconView?.setVisibility(VISIBLE)
            iconView?.setImageDrawable(icon.getDrawable())
        } else {
            iconView?.setVisibility(GONE)
        }

        if (tertiaryView != null) {
            tertiaryView.setText(body)
            nativeAdView.bodyView = tertiaryView
        }

        nativeAdView.setNativeAd(nativeAd)
        adContainer.removeAllViews()
        adContainer.addView(nativeAdView)

        mShowingNativeAdCache[key] = nativeAd

        loadNative(adContainer.context, adUnit, adCallback)
    }

    fun destroyNative(adContainer: ViewGroup, scene: String = "Default") {
        log("destroyNative: $scene")
        adContainer.children.forEach {
            if (it is NativeAdView) {
                it.destroy()
            }
        }
        adContainer.removeAllViews()
    }

    fun destroyNativeCache() {
        mShowingNativeAdCache.map {
            it.value.destroy()
        }
        mShowingNativeAdCache.clear()
        mCacheNativeAd.map {
            it.destroy()
        }
        mCacheNativeAd.clear()
    }

    fun hasInterAdCache() = mInterAdCache != null
    fun hasRewardAdCache() = mRewardAdCache != null

    fun showInterAdCache(activity: Activity, adCallback: AdCallback?) {
        log("showInterAdCache: ")
        mInterAdCache?.let {
            it.setOnPaidEventListener {
                log("showInterAdCache: onPaidEvent")
                adCallback?.onAdPaid(it)
            }
        }
        mInterAdCache?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                log("showInterAdCache: onAdDismissedFullScreenContent")
                adCallback?.onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                log("showInterAdCache: onAdShowedFullScreenContent")
                adCallback?.onShow()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                log("showInterAdCache: onAdFailedToShowFullScreenContent")
                adCallback?.onShowFailed(error.code, error.message)
            }
        }
        mInterAdCache?.show(activity)
        mInterAdCache = null
    }


    fun showRewardAdCache(activity: Activity, adCallback: AdCallback?) {
        log("showRewardAdCache: ")
        mRewardAdCache?.let {
            it.setOnPaidEventListener {
                log("showRewardAdCache: onPaidEvent")
                adCallback?.onAdPaid(it)
            }
        }
        mRewardAdCache?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                log("showRewardAdCache: onAdDismissedFullScreenContent")
                adCallback?.onDismiss()
            }

            override fun onAdShowedFullScreenContent() {
                log("showRewardAdCache: onAdShowedFullScreenContent")
                adCallback?.onShow()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                log("showRewardAdCache: onAdFailedToShowFullScreenContent")
                adCallback?.onShowFailed(error.code, error.message)
            }
        }
        mRewardAdCache?.show(activity) {
            adCallback?.onRewarded()
            log("showRewardAdCache: onUserEarnedReward")
        }
        mRewardAdCache = null
    }

    fun destroyInterAdCache() {
        mInterAdCache = null
    }

    fun destroyRewardAdCache() {
        mRewardAdCache = null
    }


    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser)
    }

    private fun log(msg: String) {
        if (App.DEBUG) {
            Log.d("AdManager", msg)
        }
    }

    @SuppressLint("ServiceCast")
    private fun getScreenWidth(context: Context): Int {
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        val display = windowManager.defaultDisplay
        display?.getMetrics(metrics)
        val density = metrics.density
        return (metrics.widthPixels / density).toInt()
    }
}