package com.alsg.bakericon.ad

import android.view.ViewGroup
import com.light.icon.adcore.AdUnitId

interface NativeAdWaiter {
    val adUnitId: AdUnitId
    fun getNativeAdView(): ViewGroup?
    fun onShowSuccess()
    fun onShowFailed()
    fun getNativeAdViewContainer(): ViewGroup?
}