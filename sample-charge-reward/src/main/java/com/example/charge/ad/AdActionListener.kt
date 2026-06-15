package com.example.charge.ad

import app.allever.android.lib.core.app.App
import com.example.charge.ChargeApp
import com.example.charge.constant.LogTag
import com.example.charge.event.DismissAdEvent
import com.example.charge.utils.LogUtil
import com.example.charge.utils.log
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdType
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.IAdmobActionListener
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.SourceType
import org.greenrobot.eventbus.EventBus

class AdActionListener : IAdmobActionListener {
    override fun adDismiss(ecpm: Double, adIndex: Int) {
        if (App.DEBUG) {
            //广告关闭回调, 通过adIndex去判断是哪个地方调用 ecpm:千次价值
            LogUtil.ad("adDismiss ecpm: $ecpm adIndex $adIndex")
        }
        EventBus.getDefault().post(DismissAdEvent(adIndex, ecpm))

    }

    override fun adShowFailed(code: Int, adIndex: Int) {
        if (App.DEBUG) {
            LogUtil.ad("adShowFailed code: $code adIndex $adIndex")
        }
        //sdk也发了AdShowFailedEvent
//        EventBus.getDefault().post(AdShowFailedEvent(adIndex))
//        Toast.makeText(
//            App.Companion.instance, App.Companion.instance.getString(
//                when (code) {
//                    IAdmobActionListener.CODE_AD_SHOW_FAIL -> R.string.ad_fail_state_1  //广告展示失败
//                    IAdmobActionListener.CODE_AD_NOT_READY -> R.string.ad_fail_state_2  //广告未就绪
//                    IAdmobActionListener.CODE_AD_OVER_LIMIT -> R.string.ad_fail_state_3 //该广告超过可展示次数上限
//                    else -> R.string.ad_fail_state_2
//                }
//            ), Toast.LENGTH_SHORT
//        ).show()
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
    ){
        //广告展示, 通过adIndex去判断是哪个地方调用 ecpm是千次广告价值
        SdkManager.dot(
            "ad_show",
            getDotAdShowMap(ecpm, adIndex, adType, adId, netWorkName, trackId)
        )
        if (adType == AdType.TYPE_REWARD
            || adType == AdType.TYPE_BIGO_REWARD
            || adType == AdType.TYPE_KWAI_REWARD
            || adType == AdType.TYPE_OKSPIN_REWARD) {
            ChargeApp.interAdTimer.reset().start()
            if (App.DEBUG) {
                log(LogTag.INTER_AD_CD, "观看激励视频，刷新cd")
            }
        }
    }

    override fun adClick(
        ecpm: Double,
        adIndex: Int,
        adType: Int,
        adId: String?,
        netWorkName: String?
    ) {
        SdkManager.dot("ad_click", getDotAdShowMap(ecpm, adIndex, adType, adId, netWorkName, null))
    }

    override fun canAdPrice(priceType: Int, adIndex: Int): Boolean {
        var canPrice = false

        return canPrice
    }

    override fun initCompete(sourceType: SourceType) {
        //广告sdk 初始化成功回调
        if (App.DEBUG) {
            LogUtil.ad("initCompete  " + sourceType.name)
        }

        if (sourceType == SourceType.MAX || sourceType == SourceType.KWAI || sourceType == SourceType.BIGO) {
            AdManager.loadAll(ChargeApp.instance, sourceType)
        } else if (sourceType == SourceType.ADMOB) {
            AdManager.loadAdmobAppOpenAd(ChargeApp.instance, "123")
            AdManager.loadAll(ChargeApp.instance, sourceType)
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
                HOME_FLOAT_ICON -> 1
                HOME_SPEED_UP -> 2
                MOLE_GAME_INFINITE_INDEX -> 3
                MOLE_GAME_SEE_AD_INDEX -> 4
                MOLE_GAME_AWARE_INDEX -> 5
                COIN_GAME_INFINITE_INDEX -> 6
                COIN_GAME_SEE_AD_INDEX -> 7
                COIN_GAME_AWARE_INDEX -> 8
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