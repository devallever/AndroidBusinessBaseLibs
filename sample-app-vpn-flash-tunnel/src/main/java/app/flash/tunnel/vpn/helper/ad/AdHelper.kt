package app.flash.tunnel.vpn.helper.ad

import android.app.Activity
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.data.AdId
import app.flash.tunnel.vpn.helper.EventHelper
import app.flash.tunnel.vpn.helper.LogScene
import app.flash.tunnel.vpn.helper.PaidEventHelper
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.lib.admob.AdCallback
import app.flash.tunnel.vpn.lib.admob.AdManager
import app.flash.tunnel.vpn.lib.common.util.log
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardedAd

object AdHelper {
    private const val NATIVE_SCENE_DEFAULT = "NATIVE_SCENE_DEFAULT"
    private const val NATIVE_SCENE_HOME = "NATIVE_SCENE_HOME_FT"
    private const val NATIVE_SCENE_CONNECT_SUCCESS = "NATIVE_SCENE_CONNECT_SUCCESS_FT"
    private const val NATIVE_SCENE_DISCONNECT = "NATIVE_SCENE_DISCONNECT_FT"
    private const val NATIVE_SCENE_REWARD_DIALOG = "NATIVE_SCENE_REWARD_DIALOG_FT"
    private const val NATIVE_SCENE_NODE_LIST = "NATIVE_SCENE_NODE_LIST_FT"

    private val AD_PRE_STRING =
        if (App.DEBUG) "ca-app-pub-3940256099942544/" else "ca-app-pub-9616944826628432/"

    private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTER = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_REWARD = "ca-app-pub-3940256099942544/5224354917"

    private var mAdUnit: AdId? = null

    fun init() {
        AdManager.init(TunnelApp.context)
    }

    @Synchronized
    fun updateAdUnit(adUnit: AdId?) {
        mAdUnit = adUnit
    }

    private fun parseAdUnit(adUnitConfig: String?): String {
        adUnitConfig ?: return ""

        if (adUnitConfig.isEmpty()) {
            return ""
        }


        if (adUnitConfig.contains("/")) {
            return adUnitConfig
        }

        return "$AD_PRE_STRING${adUnitConfig}"
    }

    fun bannerId() = if (App.DEBUG) {
        TEST_BANNER
    } else {
        parseAdUnit(mAdUnit?.bannerId)
    }

    private fun nativeId() = if (App.DEBUG) {
        TEST_NATIVE
    } else {
        parseAdUnit(mAdUnit?.nativeId)
    }

    private fun rewardId() = if (App.DEBUG) {
        TEST_REWARD
    } else {
        parseAdUnit(mAdUnit?.rewardId)
    }

    private fun beforeInterId() = if (App.DEBUG) {
        TEST_INTER
    } else {
        parseAdUnit(mAdUnit?.beforeInterId)
    }

    private fun interId(): String {
        return if (App.DEBUG) {
            TEST_INTER
        } else {
            val nodeAdUnit = getConnectNodeAdUnitConfig()
            return if (nodeAdUnit.isEmpty()) {
                log("interId: use config adUnit")
                parseAdUnit(mAdUnit?.interId)
            } else {
                log("interId: use node adUnit")
                parseAdUnit(nodeAdUnit)
            }
        }
    }

    private fun getConnectNodeAdUnitConfig() = TunnelHelper.getConnectedNodeItem()?.adIdUnit ?: ""

    fun loadBanner(adContainer: ViewGroup, scene: String = LogScene.DEFAULT) {
        log("loadBanner: $scene")
        if (!canLoadBanner()) {
            return
        }
        val adType = PaidEventHelper.AdType.BANNER
        AdManager.loadBanner(bannerId(), adContainer, object : AdCallback {
            override fun onLoaded(adObj: Any) {

                if (canLoadBanner()) {
                    adContainer.addView(adObj as AdView)
                }
            }


            override fun onAdPaid(adValue: AdValue) {
                PaidEventHelper.logAdPaid(adType, adValue.valueMicros)
            }
        })
    }

    fun resumeBanner(adContainer: ViewGroup, scene: String = LogScene.DEFAULT) {
        log("resumeBanner: $scene")
        if (!canLoadBanner()) {
            return
        }

        AdManager.resumeBanner(adContainer)
    }

    fun pauseBanner(adContainer: ViewGroup, scene: String = LogScene.DEFAULT) {
        log("pauseBanner: $scene")
//        if (!canLoadBanner()) {
//            return
//        }

        AdManager.pauseBanner(adContainer)
    }

    private fun canLoadBanner() = TunnelHelper.isServiceStopped()

    fun destroyBanner(adContainer: ViewGroup) = AdManager.destroyBanner(adContainer)

    private fun loadInterInternal(
        adPosition: Int,
        adUnit: String,
        activity: ComponentActivity,
        check: () -> Boolean,
        adCallback: AdCallback?,
    ) {
        EventHelper.iLTimeStart = System.currentTimeMillis()
        val adType = PaidEventHelper.AdType.INTER
        EventHelper.logTriggerLoadInterAd(adPosition)


        AdManager.loadInter(adUnit, activity, adCallback = object : AdCallback {
            override fun onStart() {
                adCallback?.onStart()
            }

            override fun onLoaded(adObj: Any) {
                val usedTime = System.currentTimeMillis() - EventHelper.iLTimeStart
                EventHelper.logLoadInterAd(adPosition, usedTime, EventHelper.AdResultValue.SUCCESS)
                adCallback?.onLoaded(adObj)

                if (!TunnelHelper.isServiceConnected()) {
                    destroyInterAdCache()
                    return
                }

                log("admanager activity is isDestroyed = ${activity.isDestroyed}")
                log("admanager activity is isFinishing = ${activity.isFinishing}")
                log("admanager activity is lifecycle state = ${activity.lifecycle.currentState.name}")
                if (check.invoke() && !activity.isDestroyed && activity.lifecycle.currentState == Lifecycle.State.RESUMED && !activity.isFinishing) {
                    if (TunnelApp.alreadyInBackground) {
                        return
                    }
                    (adObj as InterstitialAd).show(activity)
                }
            }

            override fun onFailedToLoad(code: Int, err: String) {
                val usedTime = System.currentTimeMillis() - EventHelper.iLTimeStart
                EventHelper.logLoadInterAd(adPosition, usedTime, code)
                adCallback?.onFailedToLoad(code, err)
            }

            override fun onShow() {
                val usedTime = System.currentTimeMillis() - EventHelper.iLTimeStart
                EventHelper.logShowInterAd(adPosition, usedTime)
                adCallback?.onShow()
            }

            override fun onShowFailed(code: Int, err: String) {
                adCallback?.onShowFailed(code, err)
            }

            override fun onDismiss() {
                adCallback?.onDismiss()
            }

            override fun onClicked() {
                adCallback?.onClicked()
            }

            override fun onAdPaid(adValue: AdValue) {
                PaidEventHelper.logAdPaid(adType, adValue.valueMicros)
                adCallback?.onAdPaid(adValue)
            }
        })
    }

    fun loadBeforeConnectInter(
        activity: AppCompatActivity,
        adCallback: AdCallback?
    ) {
        val canLoad = false
        if (!canLoad) {
            return
        }
        loadInterInternal(-1, beforeInterId(), activity, check = {
            return@loadInterInternal canLoad
        }, adCallback = adCallback)
    }

    fun loadDisconnectInter(activity: AppCompatActivity, adCallback: AdCallback?) {
        if (!TunnelHelper.isServiceConnected()) {
            return
        }
        loadInterInternal(EventHelper.AdPositionValue.DISCONNECT_INTER, interId(), activity, check = {
            return@loadInterInternal TunnelHelper.isServiceConnected()
        }, adCallback)
    }

    fun loadReturnAppInter(activity: AppCompatActivity, adCallback: AdCallback) {
        if (!TunnelHelper.isServiceConnected()) {
            return
        }

        loadInterInternal(EventHelper.AdPositionValue.RETURN_APP_INTER, interId(), activity, check = {
            return@loadInterInternal TunnelHelper.isServiceConnected()
        }, adCallback)
    }

    fun loadConnectSuccessInter(
        activity: ComponentActivity,
        adCallback: AdCallback?
    ) {
        log("loadConnectSuccessInter: ")

        if (!TunnelHelper.isServiceConnected()) {
            log("loadConnectSuccessInter: not connect")
            adCallback?.onFailedToLoad(-1, "")
            return
        }

        loadInterInternal(EventHelper.AdPositionValue.CONNECT_SUCCESS_INTER, interId(), activity, check = {
            return@loadInterInternal TunnelHelper.isServiceConnected()
        }, adCallback)
    }

    fun loadReward(
        activity: AppCompatActivity,
        scene: String = LogScene.DEFAULT,
        adCallback: AdCallback?
    ) {
        log("loadReward: $scene")

        if (!TunnelHelper.isServiceConnected()) {
            return
        }

        val adType = PaidEventHelper.AdType.REWARD
        AdManager.loadReward(rewardId(), activity, adCallback = object : AdCallback {
            override fun onStart() {
                adCallback?.onStart()
            }

            override fun onLoaded(adObj: Any) {
                adCallback?.onLoaded(adObj)

                if (!TunnelHelper.isServiceConnected()) {
                    destroyRewardAdCache()
                    return
                }

                if (TunnelHelper.isServiceConnected() && !activity.isDestroyed && activity.lifecycle.currentState == Lifecycle.State.RESUMED && !activity.isFinishing) {
                    if (TunnelApp.alreadyInBackground) {
                        return
                    }
                    (adObj as RewardedAd).show(activity) {
                        log("loadReward: onUserEarnedReward")
                        adCallback?.onRewarded()
                    }
                }

            }

            override fun onFailedToLoad(code: Int, err: String) {
                adCallback?.onFailedToLoad(code, err)
            }

            override fun onShow() {
                adCallback?.onShow()
            }

            override fun onShowFailed(code: Int, err: String) {
                adCallback?.onShowFailed(code, err)
            }

            override fun onDismiss() {
                adCallback?.onDismiss()
            }

            override fun onClicked() {
                adCallback?.onClicked()
            }

            override fun onAdPaid(adValue: AdValue) {
                PaidEventHelper.logAdPaid(adType, adValue.valueMicros)
                adCallback?.onAdPaid(adValue)
            }

            override fun onRewarded() {
                log("loadReward: onUserEarnedReward")
                adCallback?.onRewarded()
            }
        })
    }

    fun loadHomeNative(adContainer: ViewGroup) {

        if (!TunnelHelper.isServiceConnected()) {
            return
        }

        loadNativeInternal(
            adContainer,
            R.layout.ad_native_small,
            NATIVE_SCENE_HOME
        ) {
            TunnelHelper.isServiceConnected()
        }
    }

    fun loadConnectSuccessNative(adContainer: ViewGroup) {
        if (!TunnelHelper.isServiceConnected()) {
            return
        }

        loadNativeInternal(
            adContainer,
            R.layout.ad_native_medium,
            NATIVE_SCENE_CONNECT_SUCCESS
        ) {
            TunnelHelper.isServiceConnected()
        }
    }

    fun loadDisconnectNative(adContainer: ViewGroup) {
        if (!TunnelHelper.isServiceConnected()) {
            return
        }

        loadNativeInternal(
            adContainer,
            R.layout.ad_native_medium,
            NATIVE_SCENE_DISCONNECT
        ) {
            TunnelHelper.isServiceConnected()
        }
    }

    fun loadRewardDialogNative(adContainer: ViewGroup) {
        if (!TunnelHelper.isServiceConnected()) {
            return
        }

        loadNativeInternal(
            adContainer,
            R.layout.ad_native_dialog,
            NATIVE_SCENE_REWARD_DIALOG
        ) {
            TunnelHelper.isServiceConnected()
        }
    }

    fun loadNodeListNative(
        adContainer: ViewGroup,
        success: () -> Unit = {},
        fail: () -> Unit = {}
    ) {
        if (!TunnelHelper.isServiceConnected()) {
            fail.invoke()
            return
        }

        loadNativeInternal(
            adContainer,
            R.layout.ad_native_node_list_2,
            NATIVE_SCENE_NODE_LIST, success, fail
        ) {
            TunnelHelper.isServiceConnected()
        }
    }

    private fun loadNativeInternal(
        adContainer: ViewGroup,
        adLayout: Int,
        scene: String,
        success: () -> Unit = {}, fail: () -> Unit = {},
        checkCanShow: () -> Boolean,
    ) {
        log("loadNativeInternal: $scene")
        val adType = PaidEventHelper.AdType.NATIVE
        val ad = AdManager.obtainNative()

        val adCallback = object : AdCallback {


            override fun onLoaded(adObj: Any) {
                if (!checkCanShow()) {
                    if (!TunnelHelper.isServiceConnected()) {
                        destroyNativeCache()
                        destroyNative(adContainer)
                    }
                    fail.invoke()
                    return
                }

                //when has cache case, pass callback to load next ad, ignore loaded callback
                if (ad != null) {
                    return
                }

                AdManager.obtainNative()?.let {
                    it.setOnPaidEventListener {
                        PaidEventHelper.logAdPaid(adType, it.valueMicros)
                    }
                    success.invoke()
                    AdManager.showNativeAd(
                        it,
                        nativeId(),
                        scene,
                        adContainer,
                        adLayout,
                    )
                }
            }

        }

        if (ad == null) {
            if (checkCanShow()) {
                AdManager.loadNative(adContainer.context, nativeId(), adCallback)
            }
        } else {
            //load success

            ad.setOnPaidEventListener {
                PaidEventHelper.logAdPaid(adType, it.valueMicros)
            }

            if (!checkCanShow()) {
                if (!TunnelHelper.isServiceConnected()) {
                    destroyNativeCache()
                    destroyNative(adContainer)
                }
                fail.invoke()
                return
            }


            success.invoke()
            AdManager.showNativeAd(
                ad,
                nativeId(),
                scene,
                adContainer,
                adLayout,
                adCallback
            )
        }
    }

    fun destroyNative(adContainer: ViewGroup, scene: String = LogScene.DEFAULT) {
        AdManager.destroyNative(adContainer, scene)
    }

    fun destroyNativeCache() {
        AdManager.destroyNativeCache()
    }

    fun hasInterAdCache() = AdManager.hasInterAdCache()

    fun hasRewardAdCache() = AdManager.hasRewardAdCache()

    fun showInterAdCache(activity: Activity, adCallback: AdCallback? = null) {
        val adType = PaidEventHelper.AdType.INTER
        AdManager.showInterAdCache(activity, object : AdCallback {
            override fun onDismiss() {
                adCallback?.onDismiss()
            }

            override fun onShow() {
                adCallback?.onShow()
            }

            override fun onShowFailed(code: Int, err: String) {
                adCallback?.onShowFailed(code, err)
            }

            override fun onAdPaid(adValue: AdValue) {
                PaidEventHelper.logAdPaid(adType, adValue.valueMicros)
                adCallback?.onAdPaid(adValue)
            }
        })
    }

    fun showRewardAdCache(activity: Activity, adCallback: AdCallback? = null) {
        val adType = PaidEventHelper.AdType.REWARD
        AdManager.showRewardAdCache(activity, object : AdCallback {
            override fun onRewarded() {
                log("showRewardAdCache: onRewarded")
                adCallback?.onRewarded()
            }

            override fun onDismiss() {
                adCallback?.onDismiss()
            }

            override fun onShow() {
                adCallback?.onShow()
            }

            override fun onShowFailed(code: Int, err: String) {
                adCallback?.onShowFailed(code, err)
            }

            override fun onAdPaid(adValue: AdValue) {
                PaidEventHelper.logAdPaid(adType, adValue.valueMicros)
                adCallback?.onAdPaid(adValue)
            }
        })
    }


    fun destroyInterAdCache() = AdManager.destroyInterAdCache()

    fun destroyRewardAdCache() = AdManager.destroyRewardAdCache()
}