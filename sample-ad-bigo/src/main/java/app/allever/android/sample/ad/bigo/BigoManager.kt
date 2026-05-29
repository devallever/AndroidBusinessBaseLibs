package app.allever.android.sample.ad.bigo

import android.app.Activity
import android.app.Application
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import app.allever.android.lib.core.app.App
import sg.bigo.ads.BigoAdSdk
import sg.bigo.ads.ad.banner.BigoAdView
import sg.bigo.ads.api.AdConfig
import sg.bigo.ads.api.AdError
import sg.bigo.ads.api.AdInteractionListener
import sg.bigo.ads.api.AdLoadListener
import sg.bigo.ads.api.AdOptionsView
import sg.bigo.ads.api.AdSize
import sg.bigo.ads.api.AdTag
import sg.bigo.ads.api.BannerAdRequest
import sg.bigo.ads.api.InterstitialAd
import sg.bigo.ads.api.InterstitialAdLoader
import sg.bigo.ads.api.InterstitialAdRequest
import sg.bigo.ads.api.MediaView
import sg.bigo.ads.api.NativeAd
import sg.bigo.ads.api.NativeAdLoader
import sg.bigo.ads.api.NativeAdRequest
import sg.bigo.ads.api.RewardAdInteractionListener
import sg.bigo.ads.api.RewardVideoAd
import sg.bigo.ads.api.RewardVideoAdLoader
import sg.bigo.ads.api.RewardVideoAdRequest


/**
 *@Description: https://www.bigossp.com/guide/sdk/android/document
 *@Author: allever
 *@CreateTime: 2026/5/26 16:09
 */
object BigoManager {
    private val APP_ID = "10182906"
    private var isInit = false

    private var mAdConfig: IAdConfig = TestAdConfig()

    private lateinit var mContext: Application
    private var mInterAdCache: InterstitialAd? = null
    private var mRewardAdCache: RewardVideoAd? = null
    private var mInterAdCacheTime = 0L
    private var mRewardAdCacheTime = 0L
    private const val CACHE_TIME_OUT = 45 * 60 * 1000L

    fun init(adConfig: IAdConfig, block: () -> Unit) {
        mAdConfig = adConfig
        if (isInit) {
            log("bigo sdk has been initialized")
            block()
            return
        }
        val config = AdConfig.Builder().setAppId(APP_ID).setDebug(App.DEBUG).build()
        BigoAdSdk.initialize(App.context, config) {
            log("init bigo sdk success")
            block()
            isInit = true
        }
    }

    fun justLoadInter(adCallback: AdCallback? = null) {
        val cacheTime = System.currentTimeMillis() - mInterAdCacheTime
        if (mInterAdCache != null && cacheTime < CACHE_TIME_OUT) {
            log("使用InterAdCache")
            adCallback?.onAdLoaded()
            return
        }

        mInterAdCache = null

        val interstitialAdRequest = InterstitialAdRequest.Builder()
            .withSlotId(mAdConfig.getAdId(IAdConfig.Companion.INTER_AD)).build()

        val interstitialAdLoader = InterstitialAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<InterstitialAd> {
                override fun onError(adError: AdError) {
                    logE("interAd: 加载失败 -> ${adError.code}")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    log("interAd: 加载成功")
                    mInterAdCache = interstitialAd
                    mInterAdCacheTime = System.currentTimeMillis()
                    log("interAd: 缓存成功")
                }

            }).build()
        interstitialAdLoader.loadAd(interstitialAdRequest)
    }

    fun showInter(activity: Activity, adCallback: AdCallback? = null) {

        if (mInterAdCache == null) {
            justLoadInter()
            log("InterAdCache: 缓存中无广告, 加载广告")
            adCallback?.onAdFailLoad()
            return
        }

        val cacheTime = System.currentTimeMillis() - mInterAdCacheTime
        if (cacheTime > CACHE_TIME_OUT) {
            log("InterAdCache: 缓存已过期，加载广告")
            mInterAdCache = null
            justLoadInter()
            adCallback?.onAdFailLoad()
            return
        }

        log("使用InterAdCache")
        mInterAdCache?.setAdInteractionListener(object : AdInteractionListener {
            override fun onAdError(p0: AdError) {
                log("InterAdCache: 显示失败 ${p0.code} -> ${p0.message}")
                mInterAdCache = null
                adCallback?.onAdFailLoad()
                justLoadInter()
            }

            override fun onAdImpression() {
                log("InterAdCache: 显示")
                adCallback?.onAdShow()
            }

            override fun onAdClicked() {
                log("InterAdCache: 点击")
                adCallback?.onAdClick()
            }

            override fun onAdOpened() {
                log("InterAdCache: 打开")
            }

            override fun onAdClosed() {
                log("InterAdCache: 关闭")
                mInterAdCache = null
                adCallback?.onAdDismiss()
                justLoadInter()
            }

        })
        mInterAdCache?.show(activity)
    }

    fun justLoadReward(adCallback: AdCallback? = null) {
        val cacheTime = System.currentTimeMillis() - mRewardAdCacheTime
        if (mRewardAdCache != null && cacheTime < CACHE_TIME_OUT) {
            log("使用RewardAdCache")
            adCallback?.onAdLoaded()
            return
        }

        mRewardAdCache = null

        val rewardVideoAdAdRequest = RewardVideoAdRequest.Builder()
            .withSlotId(mAdConfig.getAdId(IAdConfig.Companion.REWARD_AD)).build()

        val rewardVideoAdLoader = RewardVideoAdLoader.Builder()
            .withAdLoadListener(object : AdLoadListener<RewardVideoAd> {
                override fun onError(adError: AdError) {
                    logE("rewardAd: 加载失败 -> ${adError.code}: ${adError.message}")
                    adCallback?.onAdFailLoad()
                }

                override fun onAdLoaded(rewardVideoAd: RewardVideoAd) {
                    log("rewardAd: 加载成功")
                    mRewardAdCache = rewardVideoAd
                    mRewardAdCacheTime = System.currentTimeMillis()
                    log("rewardAd: 缓存成功")
                    adCallback?.onAdLoaded()
                }
            }).build()

        rewardVideoAdLoader.loadAd(rewardVideoAdAdRequest)
    }

    fun showReward(activity: Activity, adCallback: AdCallback? = null) {
        if (mRewardAdCache == null) {
            adCallback?.onAdFailLoad()
            justLoadReward(adCallback)
            return
        }

        val cacheTime = System.currentTimeMillis() - mRewardAdCacheTime
        if (cacheTime > CACHE_TIME_OUT) {
            adCallback?.onAdFailLoad()
            justLoadReward(adCallback)
            return
        }

        log("使用RewardAdCache")
        mRewardAdCache?.setAdInteractionListener(object : RewardAdInteractionListener {
            override fun onAdError(p0: AdError) {
                log("RewardAd: 显示失败")
                mRewardAdCache = null
                adCallback?.onAdFailLoad()
                justLoadReward(adCallback)

            }

            override fun onAdImpression() {
                log("RewardAd: 显示")
                adCallback?.onAdShow()
            }

            override fun onAdClicked() {
                log("RewardAd: 点击")
            }

            override fun onAdOpened() {
                log("RewardAd: 打开")
            }

            override fun onAdClosed() {
                log("RewardAd: 关闭")
                mRewardAdCache = null
                adCallback?.onAdDismiss()
                justLoadReward(adCallback)
            }

            override fun onAdRewarded() {
                log("RewardAd: 获取奖励")
                adCallback?.onRewarded()
            }
        })
        mRewardAdCache?.show(activity)
    }


    fun loadBanner(bannerContainer: ViewGroup): BigoAdView {
        val mBannerAd = BigoAdView(bannerContainer.context)
        ViewGroup.LayoutParams.MATCH_PARENT

        val bannerAdRequest =
            BannerAdRequest.Builder().withSlotId(mAdConfig.getAdId(IAdConfig.Companion.BANNER_AD))
                .withAdSizes(AdSize.LARGE_BANNER).build()
        mBannerAd.setAdLoadListener(object : AdLoadListener<BigoAdView> {
            override fun onError(p0: AdError) {
                logE("load banner error: ${p0.code} -> ${p0.message}")
            }

            override fun onAdLoaded(adView: BigoAdView) {
                log("load banner success")
                //自适应横幅
                //AdSize.getAdaptiveAdSize根据传入的宽度值，返回一个自适应的AdSize实例。传入MATCH_PARENT，表示您期望Banner宽度为您的容器宽度
                val adaptiveSize =
                    AdSize.getAdaptiveAdSize(App.context, ViewGroup.LayoutParams.MATCH_PARENT)
                val heightPx = AdSize.dp2px(App.context, adaptiveSize.height)
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val layoutParams = FrameLayout.LayoutParams(width, heightPx) //设置BigoAdView的宽高
                layoutParams.gravity = Gravity.CENTER //可选，在您的布局容器里居中

                adView.setAdInteractionListener(object : AdInteractionListener {
                    override fun onAdError(p0: AdError) {
                        logE("load banner error: ${p0.code} -> ${p0.message}")
                    }

                    override fun onAdImpression() {
                        log("banner show")
                    }

                    override fun onAdClicked() {
                        log("banner click")
                    }

                    override fun onAdOpened() {
                        log("banner open")
                    }

                    override fun onAdClosed() {
                        log("banner close")
                    }

                })
                bannerContainer.addView(adView, layoutParams)
            }

        })
        mBannerAd.loadAd(bannerAdRequest)
        return mBannerAd
    }


    fun destroyBanner(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is BigoAdView) {
                child.destroy()
            }
        }
        viewGroup.removeAllViews()
    }

    private var mNativeBannerCache = mutableMapOf<String, NativeAd>()
    private var mNativeBannerGroup = mutableMapOf<String, ViewGroup>()
    fun loadNativeAd(
        viewGroup: ViewGroup,
        page: String,
        adLayoutId: Int = R.layout.default_bigo_native_banner,
        show: Boolean = true
    ) {
        destroyNativeAd(page)
        mNativeBannerGroup[page] = viewGroup

        val request =
            NativeAdRequest.Builder().withSlotId(mAdConfig.getAdId(IAdConfig.Companion.NATIVE_AD))
                .build()

        val nativeAdLoader =
            NativeAdLoader.Builder().withAdLoadListener(object : AdLoadListener<NativeAd> {
                override fun onError(error: AdError) {
                    logE("nativeBanner加载失败${error.code} -> ${error.message}")
                }

                override fun onAdLoaded(nativeAd: NativeAd) {
                    log("nativeBanner加载成功")
                    log("forNativeAd")
                    mNativeBannerCache[page] = nativeAd

                    nativeAd.setAdInteractionListener(object : AdInteractionListener {
                        override fun onAdError(p0: AdError) {
                            logE("nativeBanner加载失败${p0.code} -> ${p0.message}")
                        }

                        override fun onAdImpression() {
                            log("nativeBanner展示")
                        }

                        override fun onAdClicked() {
                            log("nativeBanner点击")
                        }

                        override fun onAdOpened() {
                            log("nativeBanner打开")
                        }

                        override fun onAdClosed() {
                            log("nativeBanner关闭")
                        }

                    })

                    handleNativeAd(nativeAd, adLayoutId, viewGroup)
                }
            }).build()
        nativeAdLoader.loadAd(request)

    }

    fun handleNativeAd(ad: NativeAd, layoutId: Int, adContainer: ViewGroup) {
        val nativeView = LayoutInflater.from(adContainer.context).inflate(
            layoutId, null
        ) as ViewGroup

        // MediaView
        val mediaView = nativeView.findViewById<MediaView>(R.id.native_media_view)

        // Icon
        val iconView = nativeView.findViewById<ImageView>(R.id.native_icon_view)

        // AdOptionsView
        val optionsView = nativeView.findViewById<AdOptionsView>(R.id.native_option_view)

        // Title
        val titleView = nativeView.findViewById<TextView>(R.id.native_title)
        titleView?.tag = AdTag.TITLE
        titleView.text = ad.title

        // Description
        val descriptionView = nativeView.findViewById<TextView>(R.id.native_description)
        descriptionView.tag = AdTag.DESCRIPTION
        descriptionView.text = ad.description

        // Call to action button
        val ctaButton = nativeView.findViewById<Button>(R.id.native_cta)
        ctaButton.tag = AdTag.CALL_TO_ACTION
        ctaButton.text = ad.callToAction

        // Warning
        val warningView = nativeView.findViewById<TextView>(R.id.native_warning)
        warningView.tag = AdTag.WARNING
        warningView.text = ad.warning

        // Advertiser
        val advertiserView = nativeView.findViewById<TextView>(R.id.native_advertiser)
        advertiserView.text = ad.advertiser

        val clickableViews = arrayOf<View>(titleView, descriptionView, ctaButton)
        // Register all views.
        ad.registerViewForInteraction(
            nativeView,
            mediaView,
            iconView,
            optionsView,
            listOf(*clickableViews)
        )

        adContainer.removeAllViews()
        adContainer.addView(nativeView)
        adContainer.isVisible = true
    }

    fun resumeNativeBanner(page: String) {
        destroyNativeAd(page)
        mNativeBannerGroup[page]?.let {
            loadNativeAd(it, page)
        }
    }

    fun destroyNativeAd(page: String) {
        mNativeBannerCache.remove(page)?.destroy()
        mNativeBannerGroup[page]?.removeAllViews()
    }


    private fun log(msg: String) {
        if (App.DEBUG) {
            Log.d("ILogger", msg)
        }
    }

    private fun logE(msg: String) {
        if (App.DEBUG) {
            Log.e("ILogger", msg)
        }
    }
}