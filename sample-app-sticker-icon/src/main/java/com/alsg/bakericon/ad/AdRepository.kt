package com.alsg.bakericon.ad

import android.app.Activity
import com.light.icon.adcore.AdCore
import com.light.icon.adcore.AdStateChangeListener
import com.light.icon.adcore.AdUnitId
import com.allever.lib.base.app.App
import com.alsg.bakericon.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class AdRepository {
    companion object {
        val instance: AdRepository by lazyOf(AdRepository())
    }

    private val interWaitingShowIds = HashSet<AdUnitId>()
    private val nativeAdWaiters = HashSet<NativeAdWaiter>()
    private val interstitialStateListener = object : AdStateChangeListener {
        override fun getAdType(): Int {
            return AdCore.AD_TYPE_INTERSTITIAL
        }

        override fun onStateChange(newState: Int, adUnitId: AdUnitId) {
            AppScope.runOnUiThread {
                when (newState) {
                    AdCore.STATE_LOAD_FAIL -> {
                        interWaitingShowIds.remove(adUnitId)
                    }

                    AdCore.STATE_LOAD_SUCCESS -> {
                        if (interWaitingShowIds.contains(adUnitId)) {
                            interWaitingShowIds.clear()
                            triggerShowInterAd(adUnitId)
                        }
                    }

                    AdCore.STATE_SHOW_FAIL -> {
//                        loadInterAd(adUnitId)
                    }

                    AdCore.STATE_AD_DISMISS -> {
//                        loadInterAd(adUnitId)
                    }
                }
            }
        }
    }

    private val nativeAdStateListener = object : AdStateChangeListener {
        override fun getAdType(): Int {
            return AdCore.AD_TYPE_NATIVE
        }

        override fun onStateChange(newState: Int, adUnitId: AdUnitId) {
            AppScope.runOnUiThread {
                when (newState) {
                    AdCore.STATE_LOAD_SUCCESS -> {
                        for (nativeAdWaiter in nativeAdWaiters) {
                            if (nativeAdWaiter.adUnitId == adUnitId) {
                                triggerShowNative(nativeAdWaiter, false)
                                break
                            }
                        }
                    }

                    AdCore.STATE_CACHE_TIME_OUT -> {
                        AdCore.loadNative(App.context, adUnitId)
                    }
                }
            }
        }
    }

    init {
        AdCore.registerAdStateChangeListener(interstitialStateListener)
        AdCore.registerAdStateChangeListener(nativeAdStateListener)
    }

    private var topActivityWeak: WeakReference<Activity>? = null

    fun registerTopActivity(activity: Activity) {
        topActivityWeak = WeakReference(activity)
    }

    fun unRegisterTopActivity() {
        topActivityWeak = null
    }

    fun triggerShowInterAd(adUnitId: AdUnitId) {
        //disabled all inter
        AppScope.launch(Dispatchers.Main) {
            delay(1000L)
            topActivityWeak?.get()?.let {
                if (AdCore.isInterstitialAdReady(adUnitId)) {
                    AdCore.showInterstitialAd(it, adUnitId)
                } else {
                    interWaitingShowIds.add(adUnitId)
                    loadInterAd(adUnitId)
                }
            }
        }
    }

    fun triggerShowNative(nativeAdWaiter: NativeAdWaiter, addWait: Boolean = true) {
        AppScope.runOnUiThread {
            if (AdCore.isNativeReady(nativeAdWaiter.adUnitId)) {
                nativeAdWaiter.getNativeAdView()?.let {
                    nativeAdWaiter.getNativeAdViewContainer()?.let { adViewContainer->
                        if (AdCore.showNative(nativeAdWaiter.adUnitId, it, adViewContainer)) {
                            nativeAdWaiter.onShowSuccess()
                        } else {
                            nativeAdWaiter.onShowFailed()
                        }
                    }
                }
            } else {
                if (addWait) {
                    nativeAdWaiters.add(nativeAdWaiter)
                }
                loadNative(nativeAdWaiter.adUnitId)
            }
        }
    }

    fun cancelNativeAdWaiter(nativeAdWaiter: NativeAdWaiter) {
        nativeAdWaiters.remove(nativeAdWaiter)
    }

    fun loadInterAd(adUnitId: AdUnitId) {
        // disabled all inter
        AppScope.runOnUiThread {
            AdCore.loadInterstitialAd(App.context, adUnitId)
        }
    }

    fun loadNative(adUnitId: AdUnitId) {
        AppScope.runOnUiThread {
            AdCore.loadNative(App.context, adUnitId)
        }
    }

}