package app.allever.android.sample.ad.pangle

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import app.allever.android.lib.core.app.App
import com.bumptech.glide.Glide
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerSize
import com.bytedance.sdk.openadsdk.api.init.PAGConfig
import com.bytedance.sdk.openadsdk.api.init.PAGSdk
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAd
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdData
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeAdLoadListener
import com.bytedance.sdk.openadsdk.api.nativeAd.PAGNativeRequest
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardItem
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAd
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdLoadListener
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedRequest

/**
 * https://www.pangleglobal.com/zh/integration/integrate-pangle-sdk-for-android
 * https://www.pangleglobal.com/publisher/application
 */
object PangleManager {
    private val APP_ID = if (App.DEBUG) "8025677" else ""

    private var mAdConfig: IAdConfig = TestAdConfig()

    private lateinit var mContext: Application
    private var mInterAdCache: PAGInterstitialAd? = null
    private var mRewardAdCache: PAGRewardedAd? = null
    private var mInterAdCacheTime = 0L
    private var mRewardAdCacheTime = 0L
    private const val CACHE_TIME_OUT = 45 * 60 * 1000L

    fun init(adConfig: IAdConfig, block: () -> Unit = {}) {
        mAdConfig = adConfig

        PAGSdk.init(App.app, buildNewConfig(), object : PAGSdk.PAGInitCallback {
            override fun success() {
                log("init success")
                block()
            }

            override fun fail(p0: Int, p1: String?) {
                logE("init fail: $p0 -> $p1")
            }

        })
    }

    private fun buildNewConfig(): PAGConfig? {
        return PAGConfig.Builder().appId(APP_ID)
            //如果您使用Open Ad格式，则需要设置应用程序的图标
//            .appIcon(mAdConfig.appIcon())
            .debugLog(App.DEBUG)
            //如果您的应用程序是多进程应用程序，请将此值设置为 true
            .supportMultiProcess(false)
            //如果使用AAB功能，并将SDK配置到功能模块，则需要设置功能模块的包名.请用 .分隔基础包名称和模块名称。
            //例如，如果您的基础包名称是 com.test.123，模块名称是 456，那么您应该填写空白，com.test.123.456
//            .setPackageName(""), 在模块里面不需要设置
            .build()
    }

    fun justLoadInter(adCallback: AdCallback? = null) {
        val cacheTime = System.currentTimeMillis() - mInterAdCacheTime
        if (mInterAdCache != null && cacheTime < CACHE_TIME_OUT) {
            log("使用InterAdCache")
            adCallback?.onAdLoaded()
            return
        }

        mInterAdCache = null

        val request = PAGInterstitialRequest()
        PAGInterstitialAd.loadAd(
            mAdConfig.getAdId(IAdConfig.Companion.INTER_AD),
            request,
            object : PAGInterstitialAdLoadListener {
                override fun onError(code: Int, message: String) {
                    logE("interAd: 加载失败 -> $code -> $message")
                    adCallback?.onAdFailLoad()
                }

                override fun onAdLoaded(interstitialAd: PAGInterstitialAd) {
                    log("interAd: 加载成功")
                    mInterAdCache = interstitialAd
                    mInterAdCacheTime = System.currentTimeMillis()
                    log("interAd: 缓存成功")
                    adCallback?.onAdLoaded()
                }
            })
    }

    fun justLoadReward(adCallback: AdCallback? = null) {
        val cacheTime = System.currentTimeMillis() - mRewardAdCacheTime
        if (mRewardAdCache != null && cacheTime < CACHE_TIME_OUT) {
            log("使用RewardAdCache")
            adCallback?.onAdLoaded()
            return
        }

        mRewardAdCache = null

        val request = PAGRewardedRequest()
        PAGRewardedAd.loadAd(
            mAdConfig.getAdId(IAdConfig.Companion.REWARD_AD),
            request,
            object : PAGRewardedAdLoadListener {
                override fun onError(code: Int, message: String) {
                    logE("rewardAd: 加载失败 -> ${code}: $message")
                    adCallback?.onAdFailLoad()
                }

                override fun onAdLoaded(rewardedAd: PAGRewardedAd) {
                    log("rewardAd: 加载成功")
                    mRewardAdCache = rewardedAd
                    mRewardAdCacheTime = System.currentTimeMillis()
                    log("rewardAd: 缓存成功")
                    adCallback?.onAdLoaded()
                }
            })

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

        mInterAdCache?.setAdInteractionCallback(object : PAGInterstitialAdInteractionCallback() {
            override fun onAdShowed() {
                log("InterAdCache: 显示")
                adCallback?.onAdShow()
            }

            override fun onAdClicked() {
                log("InterAdCache: 点击")
                adCallback?.onAdClick()
            }

            override fun onAdDismissed() {
                log("InterAdCache: 关闭")
                mInterAdCache = null
                adCallback?.onAdDismiss()
                justLoadInter()
            }

            override fun onAdShowFailed(pagErrorModel: PAGErrorModel) {
                log("InterAdCache: 显示失败")
                mInterAdCache = null
                adCallback?.onAdFailLoad()
                justLoadInter()
            }

        })
        mInterAdCache?.show(activity)
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

        mRewardAdCache?.setAdInteractionCallback(object : PAGRewardedAdInteractionCallback() {
            override fun onAdClicked() {
                log("RewardAd: 点击")
                adCallback?.onAdClick()
            }

            override fun onAdDismissed() {
                log("RewardAd: 关闭")
                mRewardAdCache = null
                adCallback?.onAdDismiss()
                justLoadReward(adCallback)
            }

            override fun onUserEarnedReward(item: PAGRewardItem?) {
                log("RewardAd: 获取奖励")
                adCallback?.onRewarded()
            }
        })

        mRewardAdCache?.show(activity)
    }

    fun loadBanner(bannerContainer: ViewGroup) {

        val bannerSize = PAGBannerSize.BANNER_W_320_H_50
        val bannerRequest = PAGBannerRequest(bannerSize)

        PAGBannerAd.loadAd(
            mAdConfig.getAdId(IAdConfig.Companion.BANNER_AD),
            bannerRequest,
            object : PAGBannerAdLoadListener {
                override fun onAdLoaded(bannerAd: PAGBannerAd) {
                    log("BannerAd: 加载成功")
                    bannerAd.setAdInteractionCallback(object : PAGBannerAdInteractionCallback() {
                        override fun onAdClicked() {
                            log("BannerAd: 点击")
                        }

                        override fun onAdDismissed() {
                            log("BannerAd: 关闭")
                        }

                        override fun onAdShowFailed(pagErrorModel: PAGErrorModel) {
                            log("BannerAd: 显示失败")
                        }

                        override fun onAdShowed() {
                            log("BannerAd: 显示")
                        }
                    })

                    bannerContainer.addView(bannerAd.bannerView)
                }

                override fun onError(p0: Int, p1: String?) {

                }
            })


        return
    }

//    fun resumeBanner(viewGroup: ViewGroup) {
//        for (i in 0 until  viewGroup.childCount) {
//            val child = viewGroup.getChildAt(i)
//            if (child is AdView) {
//                child.resume()
//            }
//        }
//    }

//    fun pauseBanner(viewGroup: ViewGroup) {
//        for (i in 0 until  viewGroup.childCount) {
//            val child = viewGroup.getChildAt(i)
//            if (child is AdView) {
//                child.pause()
//            }
//        }
//    }

    fun destroyBanner(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is PAGBannerAd) {
                child.destroy()
            }
        }
        viewGroup.removeAllViews()
    }

    //
//    fun loadInter(block: (interstitialAd: InterstitialAd) -> Unit) {
//        val cacheTime = System.currentTimeMillis() - mInterAdCacheTime
//        if (mInterAdCache != null && cacheTime <= CACHE_TIME_OUT) {
//            block.invoke(mInterAdCache!!)
//            return
//        }
//        mInterAdCache = null
//        val adRequest = AdRequest.Builder().build()
//
//        InterstitialAd.load(
//            mContext,
//            mAdConfig.getAdId(IAdConfig.Companion.INTER_AD),
//            adRequest,
//            object : InterstitialAdLoadCallback() {
//                override fun onAdFailedToLoad(adError: LoadAdError) {
//                    logE("interAd: 加载失败 -> ${adError.code}")
//                }
//
//                override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                    log("interAd: 加载成功")
//                    mInterAdCache = interstitialAd
//                    block.invoke(interstitialAd)
//                }
//            })
//    }
//
    private var mNativeBannerCache = mutableMapOf<String, PAGNativeAd>()
    private var mNativeBannerGroup = mutableMapOf<String, ViewGroup>()
    fun loadNativeAd(
        viewGroup: ViewGroup,
        page: String,
        adLayoutId: Int = R.layout.default_ad_native_fragment_pangle,
        show: Boolean = true
    ) {
        destroyNativeAd(page)
        mNativeBannerGroup[page] = viewGroup


        val request = PAGNativeRequest()
        PAGNativeAd.loadAd(
            mAdConfig.getAdId(IAdConfig.Companion.NATIVE_AD),
            request,
            object : PAGNativeAdLoadListener {
                override fun onError(p0: Int, p1: String?) {
                    logE("nativeBanner加载失败${p0} -> $p1")
                }

                override fun onAdLoaded(pagNativeAd: PAGNativeAd) {
                    log("nativeBanner: 加载成功")

                    log("forNativeAd")
                    mNativeBannerCache[page] = pagNativeAd
                    val adView = LayoutInflater.from(viewGroup.context)
                        .inflate(adLayoutId, null) as ViewGroup
                    showNative(adView, pagNativeAd, viewGroup, null)
                }
            }
        )

    }

    private fun showNative(
        adView: ViewGroup,
        nativeAd: PAGNativeAd,
        adViewContainer: ViewGroup,
        adCallback: AdCallback?
    ) {
        val adData = nativeAd.nativeAdData

        val tvTitle = adView.findViewById<TextView>(R.id.ad_headline)
        val tvDesc = adView.findViewById<TextView>(R.id.ad_body)
        val logo = adView.findViewById<ImageView>(R.id.ad_icon)
//        val mediaContainer = adView.findViewById<ViewGroup>(R.id.ad_media)
        val image = adView.findViewById<ImageView>(R.id.ad_image)
        val videoContainer = adView.findViewById<ViewGroup>(R.id.ad_video)
        val btnView = adView.findViewById<Button>(R.id.ad_cta)

        tvTitle.text = adData.title
        tvDesc.text = adData.description
        Glide.with(adView.context).load(adData.icon.imageUrl).into(logo)
        //图片或视频
        videoContainer.removeAllViews()
        videoContainer.addView(adData.mediaView)
        videoContainer.isVisible = true

        val mediaType = adData.mediaType
        log("mediaType = ${mediaType.name}")
        val isImage =
            mediaType == PAGNativeAdData.PAGNativeMediaType.PAGNativeMediaTypeImage
        image.isVisible = isImage
        videoContainer.isVisible = !isImage
        if (isImage) {
            log("mediaType = Image")
        } else {
            log("mediaType = Video")
        }

        btnView.text = adData.buttonText

        nativeAd.registerViewForInteraction(
            adViewContainer,
            listOf(adViewContainer),
            listOf(btnView),
            null,
            object :
                PAGNativeAdInteractionCallback() {
                override fun onAdShowed() {
                    log("native banner ad onAdShowed")
                    adCallback?.onAdShow()
                }

                override fun onAdClicked() {
                    log("native banner ad onAdClicked")
                    adCallback?.onAdClick()
                }

                override fun onAdDismissed() {
                    log("native banner ad onAdDismissed")
                    adCallback?.onAdDismiss()
                }
            })

        adViewContainer.removeAllViews()
        adViewContainer.addView(adView)
    }

    fun resumeNativeBanner(page: String) {
        destroyNativeAd(page)
        mNativeBannerGroup[page]?.let {
            loadNativeAd(it, page)
        }
    }

    fun destroyNativeAd(page: String) {
        mNativeBannerCache.remove(page)
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

    private fun getScreenWidth(context: Context): Int {
        val display =
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val outMetrics = DisplayMetrics()
        display?.getMetrics(outMetrics)
        val density = outMetrics.density
        return (outMetrics.widthPixels / density).toInt()
    }

}