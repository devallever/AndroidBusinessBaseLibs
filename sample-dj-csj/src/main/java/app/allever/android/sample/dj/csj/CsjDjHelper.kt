package app.allever.android.sample.dj.csj

import android.content.Context
import android.content.Intent
import app.allever.android.lib.core.app.App
import com.bytedance.sdk.djx.DJXSdk
import com.bytedance.sdk.djx.DJXSdkConfig
import com.bytedance.sdk.djx.IDJXPrivacyController
import com.bytedance.sdk.nov.api.NovSdk.init
import com.bytedance.sdk.nov.api.NovSdkConfig
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdSdk

object CsjDjHelper {
    fun init() {
        val ttaBuild = TTAdConfig.Builder()
            .appId(Constants.CSJ_APP_ID)
            .appName("穿山甲短剧")
            .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)
            .allowShowNotify(true)
            .supportMultiProcess(true)
            .useMediation(true) //                .customController()
            .debug(false)
            .build()
        TTAdSdk.init(App.context, ttaBuild)

        val djxSdkConfig = DJXSdkConfig.Builder()
            .debug(false)
            //.newUser(true)
            .privacyController(object : IDJXPrivacyController() {
                override fun isOnlyICPNumber(): Boolean {
                    return false
                }

                override fun isTeenagerMode(): Boolean {
                    return isTeenagerMode
                }
            })
            .build()
        DJXSdk.init(App.context, "SDK_Setting_5707189.json", djxSdkConfig)


        //初始化短故事组件
        val novConfig = NovSdkConfig.Builder()
            .build()
        init(App.context, "SDK_Setting_5707189.json", novConfig)


    }

    fun toVideoDetailPage(
        context: Context,
        id: Long,
        index: Int,
        playDuration: Int,
        fromGid: Long
    ) {
        toVideoDetailPage(context, id, index, playDuration, fromGid, true)
    }

    fun toVideoDetailPage(
        context: Context,
        id: Long,
        index: Int,
        playDuration: Int,
        fromGid: Long,
        is_drama_id: Boolean
    ) {
        val intent = Intent(context, VideoDetailActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("index", index)
        intent.putExtra("playDuration", playDuration)
        intent.putExtra("fromGid", fromGid)
        intent.putExtra("is_drama_id", is_drama_id)
        context.startActivity(intent)
    }
}