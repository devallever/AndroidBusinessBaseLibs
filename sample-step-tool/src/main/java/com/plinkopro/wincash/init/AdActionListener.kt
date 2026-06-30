package com.plinkopro.wincash.init

import android.Manifest
import android.os.Build
import android.widget.Toast
import com.plinkopro.wincash.R
import com.plinkopro.wincash.BuildConfig
import com.plinkopro.wincash.base.AppLifecycleCallback
import com.plinkopro.wincash.base.BaseApplication
import com.plinkopro.wincash.business.step.StepBusiness
import com.plinkopro.wincash.event.DismissAdEvent
import com.plinkopro.wincash.event.RequestPermissionEvent
import com.plinkopro.wincash.utils.InterAdUtil
import com.plinkopro.wincash.utils.LogUtil
import com.plinkopro.wincash.utils.PermissionUtil
import com.plinkopro.wincash.utils.log
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdShowFailedEvent
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.IAdmobActionListener
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.SourceType
import org.greenrobot.eventbus.EventBus

class AdActionListener : IAdmobActionListener {
    override fun adDismiss(ecpm: Double, adIndex: Int) {
        if (BuildConfig.LOG_OUTPUT) {
            //广告关闭回调, 通过adIndex去判断是哪个地方调用 ecpm:千次价值
            LogUtil.ad("adDismiss ecpm: $ecpm adIndex $adIndex")
        }
        EventBus.getDefault().post(DismissAdEvent(adIndex, ecpm))
        if (adIndex in listOf(
                AdIndex.ADMOB_INTER_INDEX,
                AdIndex.HOME_DOUBLE_INTER_INDEX,
                AdIndex.SCRATCH_DOUBLE_INTER_INDEX,
                AdIndex.LUCKY_WHEEL_DOUBLE_AWARD_INDEX
            )
        ){
            InterAdUtil.lastSeeAdTime = System.currentTimeMillis()
        }

    }

    override fun adShowFailed(code: Int, adIndex: Int) {
        if (BuildConfig.LOG_OUTPUT) {
            LogUtil.ad("adShowFailed code: $code adIndex $adIndex")
        }
        EventBus.getDefault().post(AdShowFailedEvent(adIndex))
        Toast.makeText(
            BaseApplication.instance, BaseApplication.instance.getString(
                when (code) {
                    IAdmobActionListener.CODE_AD_SHOW_FAIL -> R.string.ad_fail_state_1  //广告展示失败
                    IAdmobActionListener.CODE_AD_NOT_READY -> R.string.ad_fail_state_2  //广告未就绪
                    IAdmobActionListener.CODE_AD_OVER_LIMIT -> R.string.ad_fail_state_3 //该广告超过可展示次数上限
                    else -> R.string.ad_fail_state_2
                }
            ), Toast.LENGTH_SHORT
        ).show()
    }

    override fun adLoad(
        ecpm: Double,
        adIndex: Int,
        adType: Int,
        adId: String?,
        netWorkName: String?,
        trackId: String?
    ) {
    }

    override fun adShow(
        ecpm: Double,
        adIndex: Int,
        adType: Int,
        adId: String?,
        netWorkName: String?,
        trackId: String?
    ) {
        //广告展示, 通过adIndex去判断是哪个地方调用 ecpm是千次广告价值
        SdkManager.dot(
            "ad_show",
            getDotAdShowMap(ecpm, adIndex, adType, adId, netWorkName, trackId)
        )
    }

    override fun adClick(
        ecpm: Double,
        adIndex: Int,
        adType: Int,
        adId: String?,
        netWorkName: String?
    ) {
        SdkManager.dot("ad_click", getDotAdShowMap(ecpm, adIndex, adType, adId, netWorkName, null))

        //检查通知权限
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtil.hasPermission(BaseApplication.instance, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }
        if (!hasNotificationPermission) {
            if (BuildConfig.LOG_OUTPUT) {
                log("没有通知权限")
            }
//            EventBus.getDefault().post(RequestPermissionEvent())
            AppLifecycleCallback.topActivity?.let {
                val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                PermissionUtil.requestPermissions(it, permissions, 100)
            }
        }
    }

    override fun canAdPrice(priceType: Int, adIndex: Int): Boolean {
        var canPrice = false

        return canPrice
    }

    override fun initCompete(sourceType: SourceType) {
        //广告sdk 初始化成功回调
        if (BuildConfig.LOG_OUTPUT) {
            LogUtil.ad("initCompete  " + sourceType.name)
        }

        if (sourceType == SourceType.MAX || sourceType == SourceType.KWAI || sourceType == SourceType.BIGO) {
            AdManager.loadAll(BaseApplication.instance, sourceType)
        } else if (sourceType == SourceType.ADMOB) {
            AdManager.loadAdmobAppOpenAd(BaseApplication.instance, Constance.ADMOB_SPLASH_ID)
            AdManager.loadAll(BaseApplication.instance, sourceType)
//            AdManager.loadAdmobInterstitialAd(BaseApplication.instance, Constance.ADMOB_INTER_ID)
//            AdManager.loadAdmobReward(BaseApplication.instance, Constance.ADMOB_REWARD_ID)
        }
    }

    private fun getDotAdShowMap(
        ecpm: Double,
        adIndex: Int,
        adType: Int,
        adId: String?,
        networkName: String?,
        trackId: String?
    ): Map<String, Any?> {
        var dotAdIndex = -1
        AdIndex.apply {
            dotAdIndex = when (adIndex) {
                HOME_GOLD_BUBBLE1_INDEX, HOME_GOLD_BUBBLE2_INDEX -> 1
                HOME_GREEN_BUBBLE1_INDEX, HOME_GREEN_BUBBLE2_INDEX, HOME_GREEN_BUBBLE3_INDEX -> 2
                HOME_GOLD_BUTTON_INDEX -> 3
                WITHDRAW_ACCELERATE_INDEX -> 4
                LUCKY_WHEEL_ADD_CHANCES_INDEX -> 5
                SCRATCH_ADD_CHANCES_INDEX -> 6
                ADMOB_SPLASH_INDEX -> 7
                ADMOB_INTER_INDEX, LUCKY_WHEEL_DOUBLE_INTER_INDEX, HOME_DOUBLE_INTER_INDEX, SCRATCH_DOUBLE_INTER_INDEX -> 8
                HOME_DOUBLE_AWARD_INDEX, LUCKY_WHEEL_DOUBLE_AWARD_INDEX, SCRATCH_DOUBLE_AWARD_INDEX -> 9
                else -> -1
            }
        }
        return mapOf(
            "adtype" to adType,
            "adrevenue" to ecpm,
            "adid" to adId,
            "adindex" to dotAdIndex,
            "network" to networkName
        )
    }

}
