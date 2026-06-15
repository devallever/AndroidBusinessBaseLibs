package com.example.charge.init

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.charge.ChargeApp
import com.example.charge.R
import com.example.charge.ad.AdActionListener
import com.example.charge.base.AppNotifyReceiver
import com.example.charge.ui.activity.LaunchActivity
import com.example.charge.ui.activity.ChargeMainActivity
import com.example.charge.ui.activity.WithdrawActivity
import com.example.charge.withdraw.WithdrawHelper
import gjofg.frytfkrqy.hxrdk.gddrjgra.ConfigWrapper
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import gjofg.frytfkrqy.hxrdk.gddrjgra.base.IOkHttpClientWrapper
import gjofg.frytfkrqy.hxrdk.gddrjgra.notify.data.INotifyDataCallback
import gjofg.frytfkrqy.hxrdk.gddrjgra.notify.data.NotifyConfigData
import gjofg.frytfkrqy.hxrdk.gddrjgra.notify.data.NotifyViewData
import okhttp3.OkHttpClient

object InitManager {
    fun getCountryCode(): String {
        return SdkManager.getCountry()
    }

    fun init(application: Application) {
//        val config = SDKConfig.Builder()
//            .setNotifyConfig(getNotifyConfigData())
//            .setLoadingLayout(R.layout.dialog_loading)
//            .setConfigWrapper(getConfigWrapper())
//            .setOrgUrl(Constance.ORG_URL) //V1V2不用 V3才需要传这个
//            .build()
//        SdkManager.initConfig(config)
//        SdkManager.init(application,  AdActionListener())
//        SdkManager.updateServerHost(Constance.HOST_URL)


        val builder = SdkManager.Builder()
            .setAppId(Constance.APP_ID)
            .setAesIv(Constance.AES_IV)
            .setAesKey(Constance.AES_KEY)
            .setPublicKey(Constance.PUBLIC_KEY)
            .setTruePgName(Constance.TRUE_PACKAGE_NAME)
            .setServerHost(Constance.HOST_URL)
            .setAdJustId(Constance.AdJUST_ID)
            .setFbAppId(Constance.ADJUST_FB_APP_ID)
            .setLog(true)
            .setAdjustConfigKey(Constance.FP_ADJUST_KEY)
            .setAdmobConfigKey(Constance.FP_ADMOB_KEY)
//            .setNotifyConfigId(Constance.FP_NOTIFY_ID)
            .setNotifyConfigData(getNotifyConfigData()) //                .setOkSpinName(Constance.OKSPINE)
            //                .setOkSpinUrl(Constance.OKSPIN_URL)
            .setAdjustEventIndex(12) //                .setFpConfigListener(new FpConfigListener())
//            .setProtectConfigKey(Constance.FP_DEVICES_INFO)
//            .setProtectApi(Constance.DEVICES_API)


        //设置广告
        builder
            .setNativeAdId(Constance.ADMOB_NATIVE_ID)
            .setBannerAdId(Constance.ADMOB_BANNER_ID)
            .setLoadAdmob(true) /** 上传广告价值得接口 */
                .setUploadRevenueApi(Constance.UPLOAD_API)
            .setActionListener(AdActionListener())

        //数数域名
        builder.setTaHost(Constance.TA_SERVER_API) //数数appId
            .setTaId(Constance.TA_APP_ID)


        //设置拉取配置
        builder.configWrapper = getConfigWrapper()
        builder.okHttpClientWrapper = IOkHttpClientWrapper { builder1: OkHttpClient.Builder? ->
            builder1!!.retryOnConnectionFailure(true)
            builder1
        }
        SdkManager.init(application, builder)
    }

    private fun getConfigWrapper(): ConfigWrapper? {
        return ConfigWrapper.Builder("")
            .addParser(Constance.FP_WITHDRAW_KEY){ configId, parser ->
                FpManger.saveWithdrawSetting(parser)
            }.build()
    }

    private fun getNotifyConfigData(): NotifyConfigData? {
        val configBuilder = NotifyConfigData.Builder(
            ChargeApp.instance.getString(R.string.app_name),
            R.mipmap.ic_launcher,
            LaunchActivity::class.java,
            ChargeMainActivity::class.java
        )
        // 工具栏通知
        // layoutId 小的布局, bigLayoutId, 如果布局相同, 两个layout可以设置一样
        val toolViewData = NotifyViewData.Builder(
            R.layout.layout_notify_bar_small,
            R.layout.layout_notify_bar_big,
            "chargeId",
            "chargeName",
            20002
        ) //设置整个通知栏的点击 跳转
            .setContentIntent(ChargeApp.instance, 2002, ChargeMainActivity::class.java, null)
            .addClickActivity(ChargeApp.instance, R.id.ll_gold, WithdrawActivity::class.java, 2003, null)
            .addClickActivity(ChargeApp.instance, R.id.ll_green, WithdrawActivity::class.java, 2004, null)
            .addClickIntent(
                R.id.iv_close, PendingIntent.getBroadcast(
                    ChargeApp.instance,
                    0,
                    Intent(
                        ChargeApp.instance,
                        AppNotifyReceiver::class.java
                    ).setAction("action_notify_bar_close"),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                )
            ).build()
        //消息通知
        val messageViewData = NotifyViewData.Builder(
            R.layout.layout_notify_msg_small,
            R.layout.layout_notify_msg_big,
            "charge_message_id",
            "charge_message_name",
            20001
        ) //设置整个通知栏的点击 跳转
            .setContentIntent(ChargeApp.instance, 2001, WithdrawActivity::class.java, null) //指定布局id的跳转
            .build()

        configBuilder.setToolViewData(toolViewData)
            .setMessageViewData(messageViewData)

        //通知栏点击跳转activity中间的处理
        configBuilder.setNotifyDataCallback(object : INotifyDataCallback {
            override fun setMessageContent(remoteView: RemoteViews) {
                //通知的额外设置, 会回调多次, 可以通过remoteView.getLayoutId()判断是哪个layout回调的
                if (remoteView.layoutId == R.layout.layout_notify_msg_small) {
                    remoteView.setTextViewText(R.id.content, getNotifyMsgContent());
                    remoteView.setImageViewResource(R.id.imageView, R.drawable.notify_btn);

                } else if (remoteView.getLayoutId() == R.layout.layout_notify_msg_big) {
                    remoteView.setTextViewText(R.id.content, getNotifyMsgContent());
                    remoteView.setImageViewResource(R.id.imageView, R.drawable.notify_box);
                } else if (remoteView.getLayoutId() == R.layout.layout_notify_bar_small || remoteView.getLayoutId() == R.layout.layout_notify_bar_big) {
                    remoteView.setTextViewText(R.id.tvGold, getBigTaskFirstGearMoneyByType());
                    remoteView.setTextViewText(R.id.tvGreen, getBigTaskFirstGearMoneyByType());
                }
            }

            /**
             * addClickActivity和setContentIntent设置的打开的activity, 会回调这里, 控制打开哪个activity
             * @param openActivityName addClickActivity 添加的name, 例如: unity.luc.cts.android.ui.MainActivity
             * @param intent 添加intent的额外参数
             * @return 需要跳转的intent, null 不跳转
             */
            override fun onActivityOpen(s: String?, intent: Intent?): Intent? {
                return null
            }
        })
        return configBuilder.build()
    }

    private fun getNotifyMsgContent(): String {
        return  String.format(
            ChargeApp.instance.getString(R.string.txt_wd_notify_small_content),
            getBigTaskFirstGearMoneyByType()
        )
    }

    /**
     * 根据类型找到第一档的金额
     */
    private fun getBigTaskFirstGearMoneyByType(): String {
        val countryCode = getCountryCode()
//        return CountryUtil.getSymbolByCode(countryCode) + WithdrawHelper.getWithdrawLevelValue(1)
        return "$" + WithdrawHelper.getWithdrawLevelValue(1)

    }
}