package com.plinkopro.wincash.init;


import static gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager.getCountry;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.plinkopro.wincash.BuildConfig;
import com.plinkopro.wincash.R;
import com.plinkopro.wincash.base.AppNotifyReceiver;
import com.plinkopro.wincash.base.BaseApplication;
import com.plinkopro.wincash.beans.CurrencyType;
import com.plinkopro.wincash.beans.ExtraKey;
import com.plinkopro.wincash.business.withdraw.CountryUtil;
import com.plinkopro.wincash.business.withdraw.WithdrawBusiness;
import com.plinkopro.wincash.ui.activity.LaunchActivity;
import com.plinkopro.wincash.ui.activity.MainActivity;
import com.plinkopro.wincash.ui.activity.WithdrawActivity;
import com.plinkopro.wincash.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import gjofg.frytfkrqy.hxrdk.gddrjgra.ConfigWrapper;
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager;
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.max.MaxParams;
import gjofg.frytfkrqy.hxrdk.gddrjgra.notify.data.INotifyDataCallback;
import gjofg.frytfkrqy.hxrdk.gddrjgra.notify.data.NotifyConfigData;
import gjofg.frytfkrqy.hxrdk.gddrjgra.notify.data.NotifyViewData;


public class InitManager {

    public static String getCountryCode() {
//        if (BuildConfig.DEBUG) return "KR";
        String countryCode = getCountry();
        if (TextUtils.isEmpty(countryCode)) countryCode = getCountry();
        return countryCode;
    }

    public static void init(Application application) {
        SdkManager.Builder builder = new SdkManager.Builder()
                .setAppId(Constance.APP_ID)
                .setAesIv(Constance.AES_IV)
                .setAesKey(Constance.AES_KEY)
                .setPublicKey(Constance.PUBLIC_KEY)
                .setTruePgName(Constance.TRUE_PACKAGE_NAME)
                .setServerHost(Constance.HOST_URL)
                .setAdJustId(Constance.AdJUST_ID)
                .setFbAppId(Constance.ADJUST_FB_APP_ID)
                .setLog(BuildConfig.LOG_OUTPUT)
                .setAdjustConfigKey(Constance.FP_ADJUST_KEY)
                .setAdmobConfigKey(Constance.FP_ADMOB_KEY)
                .setNotifyConfigId(Constance.FP_NOTIFY_ID)
                .setNotifyConfigData(getNotifyConfigData())
//                .setOkSpinName(Constance.OKSPINE)
//                .setOkSpinUrl(Constance.OKSPIN_URL)
                .setAdjustEventIndex(12)
//                .setFpConfigListener(new FpConfigListener())
                .setProtectConfigKey(Constance.FP_DEVICES_INFO)
                .setProtectApi(Constance.DEVICES_API)
                ;

        //设置广告
        builder
                .setNativeAdId(Constance.ADMOB_NATIVE_ID)
                .setBannerAdId(Constance.ADMOB_BANNER_ID)
                .setLoadAdmob(true)
                ////上传广告价值得接口
                .setUploadRevenueApi(Constance.UPLOAD_API)
                .setActionListener(new AdActionListener());
        //数数域名
        builder.setTaHost(Constance.TA_SERVER_API)
                //数数appId
                .setTaId(Constance.TA_APP_ID);

        //设置拉取配置
        builder.setConfigWrapper(getConfigWrapper());
        builder.setOkHttpClientWrapper(builder1 -> {
            builder1.retryOnConnectionFailure(true);
            return builder1;
        });
        SdkManager.init(application, builder);
    }

    //获取拉取配置
    private static ConfigWrapper getConfigWrapper() {
        return new ConfigWrapper.Builder(Constance.FP_API)
                .addParser(Constance.FP_STEP_KEY, (configId, parser) -> {
                    if (BuildConfig.LOG_OUTPUT) {
                        LogUtil.INSTANCE.fp("configId " + configId + " parser " + parser);
                    }
                    FpManger.INSTANCE.saveStepSetting(parser);

                })
                .build();
    }

    //获取max得广告id
    private static MaxParams getMaxParams() {

        MaxParams maxParams = new MaxParams();
        //广告id配置key
        maxParams.setAdIdConfigKey(Constance.FP_ADID_KEY);
        //竞价开关配置key
        maxParams.setAdPriceConfigKey(Constance.FP_PRICE_KEY);
        //max sdk id
        maxParams.setMaxAppId(Constance.MAX_SDK_ID);
        //bigo appId
        maxParams.setBigoAppId(Constance.BIGO_APPID);
//        //快手appId
        maxParams.setKwaiAppId(Constance.KWAI_APP_ID);
//        //快手token
        maxParams.setKwaiToken(Constance.KWAI_TOKEN);
        //admob appid
        maxParams.setAdmobAppId(Constance.ADMOB_APPLICATION_ID);
        //广告得fpKey
//        maxParams.setRemoteConfigId(Constance.FP_ADID);
        //bigo得激励广告id集合
        maxParams.setBigoAdIds(getId(Constance.BIGO_REWARD_ID1, Constance.BIGO_REWARD_ID));
        //快手得激励广告id集合
        maxParams.setKwaiAdIds(getId(Constance.KWAI_REWARD_ID, Constance.KWAI_REWARD_ID1, Constance.KWAI_REWARD_ID2));
        //max得激励广告id集合
        maxParams.setRewardAdIds(getId(Constance.MAX_REWARD_ID, Constance.MAX_REWARD_ID1));
        //max得插屏广告id集合
        maxParams.setInstallAdIds(getId(Constance.MAX_INTER_ID, Constance.MAX_INTER_ID1));
        //admob得激励广告id集合
        maxParams.setAdmobRewardAdIds(getId(Constance.ADMOB_REWARD_ID));
        //admob得插屏广告id集合
        maxParams.setAdmobInterAdIds(getId(Constance.ADMOB_INTER_ID));
        maxParams.setLoadingLayoutId(R.layout.dialog_loading);
        return maxParams;
    }

    private static List<String> getId(String... ids) {
        if (ids != null) {
            return Arrays.asList(ids);
        }
        return new ArrayList<>();
    }

    private static NotifyConfigData getNotifyConfigData() {
        NotifyConfigData.Builder builder = new NotifyConfigData.Builder(
                BaseApplication.instance.getString(R.string.app_name),
                R.mipmap.ic_launcher,
                LaunchActivity.class,
                MainActivity.class);

        //消息
        builder.setMessageViewData(
                new NotifyViewData.Builder(
                        R.layout.layout_notify_msg_small,
                        R.layout.layout_notify_msg_big,
                        "lottery_message_id",
                        "lottery_message_name",
                        20001
                ).setContentIntent(BaseApplication.instance, 2001, WithdrawActivity.class, null).build());

        Bundle goldBundle = new Bundle();
        goldBundle.putInt("type", CurrencyType.GOLD.getType());

        Bundle banknoteBundle = new Bundle();
        banknoteBundle.putInt("type", CurrencyType.GREEN.getType());

        //导航栏
        builder.setToolViewData(
                new NotifyViewData.Builder(
                        R.layout.layout_notify_bar_small,
                        R.layout.layout_notify_bar_big,
                        "lottery_tool_id",
                        "lottery_tool_name",
                        20002)
                        .setContentIntent(BaseApplication.instance, 2002, MainActivity.class, null)
                        .addClickActivity(BaseApplication.instance, R.id.ll_gold, WithdrawActivity.class, 2003, goldBundle)
                        .addClickActivity(BaseApplication.instance, R.id.ll_green, WithdrawActivity.class, 2004, banknoteBundle)
                        .addClickIntent(R.id.iv_close, PendingIntent.getBroadcast(
                                BaseApplication.instance,
                                0,
                                new Intent(BaseApplication.instance, AppNotifyReceiver.class).setAction("action_notify_bar_close"),
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                                        ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                                        : PendingIntent.FLAG_UPDATE_CURRENT
                        )).build()
        );

        builder.setNotifyDataCallback(new INotifyDataCallback() {

            @Override
            public void setMessageContent(RemoteViews remoteView) {
                if (remoteView.getLayoutId() == R.layout.layout_notify_msg_small) {
                    remoteView.setTextViewText(R.id.content, getNotifyMsgContent(true));
                    remoteView.setImageViewResource(R.id.imageView, R.drawable.notify_btn);

                } else if (remoteView.getLayoutId() == R.layout.layout_notify_msg_big) {
                    remoteView.setTextViewText(R.id.content, getNotifyMsgContent(false));
                    remoteView.setImageViewResource(R.id.imageView, R.drawable.notify_box);

                } else if (remoteView.getLayoutId() == R.layout.layout_notify_bar_small || remoteView.getLayoutId() == R.layout.layout_notify_bar_big) {
                    remoteView.setTextViewText(R.id.tvGold, getBigTaskFirstGearMoneyByType());
                    remoteView.setTextViewText(R.id.tvGreen, getBigTaskFirstGearMoneyByType());
                }
            }

            @Nullable
            @Override
            public Intent onActivityOpen(String activityName, Intent intent) {
                if (!TextUtils.isEmpty(activityName)) {
                    if (activityName.equals(WithdrawActivity.class.getName())) {
                        int goldType = intent.getIntExtra("type", CurrencyType.GREEN.getType());
                        Intent newIntent = new Intent(BaseApplication.instance, WithdrawActivity.class);
                        newIntent.putExtra(ExtraKey.CURRENCY_TYPE, goldType);
                        return newIntent;
                    } else if (activityName.equals(MainActivity.class.getName())) {
                        return new Intent(BaseApplication.instance, MainActivity.class);
                    }
                }
                return null;
            }
        });


        return builder.build();

    }

    private static String getNotifyMsgContent(Boolean isSmall) {
        String content = "";
        if (isSmall){
            content =  String.format(
                    BaseApplication.instance.getString(R.string.txt_wd_notify_small_content),
                    getBigTaskFirstGearMoneyByType());
        }else {
            content =  String.format(
                    BaseApplication.instance.getString(R.string.txt_wd_notify_small_content),
                    getBigTaskFirstGearMoneyByType());
        }

        return content;
    }


    /**
     * 根据类型找到第一档的金额
     */
    private static String getBigTaskFirstGearMoneyByType() {
        String countryCode = getCountryCode();
        return CountryUtil.INSTANCE.getSymbolByCode(countryCode) + WithdrawBusiness.INSTANCE.getWithdrawCurrencyLabelValue(countryCode, 1);
    }

    private static void reportAb(String eventName, String value) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(eventName, value);
        SdkManager.userSetOnce(map);
    }
}
