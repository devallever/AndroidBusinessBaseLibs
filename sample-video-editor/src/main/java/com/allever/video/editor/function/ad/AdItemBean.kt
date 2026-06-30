//package com.qrcode.scanner.ad
//
//import androidx.annotation.LayoutRes
//import android.view.View
//import android.view.ViewGroup
//import com.android.absbase.ui.view.AllinoneAdView
//import com.android.absbase.ui.view.BaseAdView
//
//import com.rice.balls.ad.AdDisplay
//import com.rice.balls.ad.AdManager
//import com.rice.balls.ad.thirdparty.AbstractThirdPartyAd
//import com.allever.video.editor.R
//import com.allever.video.editor.function.ad.AdFlowPosition
//
///**
// *
// */
//
//class AdItemBean {
//
//    private var mNativeAdBean: AbstractThirdPartyAd? = null
//    var adTouchType = 0
//    var needLimitAdHeight = false
//    var adCacheKey: String? = null
//        set(value) {
//            field = value
//            if (value != null) {
//                mNativeAdBean = AdManager.instance.getAd(value) as AbstractThirdPartyAd?
//            }
//        }
//    var isIsShowed = false
//    var loading = false
//
//    var baseNativeView: BaseAdView? = null
//        private set
//
//    var adPosition: AdFlowPosition.Position? = null
//
//    var onAdRefreshListener: OnAdRefreshListener? = null
//
//    var adView: View? = null
//        get() {
//            val parent = field?.parent
//            if (parent != null && parent is ViewGroup) {
//                parent.removeView(field)
//            }
//            return field
//        }
//
//    val isIcon: Boolean
//        get() = adPosition?.isIcon ?: false
//
//    val isBanner: Boolean
//        get() = adPosition?.isBanner ?: true
//
//    constructor() {
//
//    }
//
//    constructor(adCacheKey: String) {
//        this.adCacheKey = adCacheKey
//    }
//
//    fun setAdItem(itemBean: AdItemBean) {
//        this.adTouchType = itemBean.adTouchType
//        this.adCacheKey = itemBean.adCacheKey
//        this.mNativeAdBean = itemBean.get()
//    }
//
//    fun get(): AbstractThirdPartyAd? {
//        return mNativeAdBean
//    }
//
//    fun set(data: AbstractThirdPartyAd) {
//        mNativeAdBean = data
//    }
//
//    fun destroy() {
//        AdManager.instance.destoryAd(adCacheKey)
//        mNativeAdBean = null
//        adTouchType = 0
//        needLimitAdHeight = false
//        adCacheKey = null
//        isIsShowed = false
//        loading = false
//        adView = null
//        baseNativeView?.adViewInterface?.destroy()
//        adPosition = null
//    }
//
//    fun hasAd(): Boolean {
//        return AdManager.instance.hasAd(adCacheKey)
//    }
//
//    fun hasAdAndInvalid(): Boolean {
//        return AdManager.instance.hasAdAndInvalid(adCacheKey)
//    }
//
//    fun createAdView(@LayoutRes layoutId: Int): View {
//        if (mNativeAdBean == null) {
//            mNativeAdBean = AdManager.instance.getAd(adCacheKey) as AbstractThirdPartyAd?
//        }
//        val allinoneAdView = AllinoneAdView.newAllinoneAdView(layoutId, null)
//        val intent = AdDisplay.makeIntent(adCacheKey, mNativeAdBean)
//        intent!!.putExtra(AdDisplay.KEY_DEFAULT_RES_ID, R.drawable.sc_ad_icon_default)
//        intent.putExtra(AdDisplay.KEY_LIMIT_AD_HEIGHT, needLimitAdHeight)
//        intent.putExtra(AdDisplay.KEY_TOUCH_TYPE, adTouchType)
//        allinoneAdView.setData(intent)
//        adView = allinoneAdView.viewAndRemoveParent
//        baseNativeView = allinoneAdView.baseAdView
//        if (baseNativeView != null) {
//            //            TextView adTitleView = mBaseNativeView.getAdTitleView();
//            //            if (adTitleView != null) {
//            //                FontUtil.setCustomFontBold(adTitleView);
//            //            }
//            //            View adActionView = mBaseNativeView.getAdActionView();
//            //            if (adActionView instanceof TextView) {
//            //                FontUtil.setCustomFontBold((TextView) adActionView);
//            //            }
//        }
//        return adView!!
//    }
//
//    fun equals(adItemBean: AdItemBean?): Boolean {
//        var ad: AbstractThirdPartyAd? = null
//        if (adItemBean != null) {
//            ad = adItemBean.get()
//        }
//        return AdManager.equals(ad, mNativeAdBean)
//    }
//
//    fun refreshAd() {
//        onAdRefreshListener?.onAdRefresh()
//    }
//
//    interface OnAdRefreshListener {
//        fun onAdRefresh()
//    }
//}
