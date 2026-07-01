package com.allever.video.editor.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.Nullable;

import android.widget.ImageView;


import com.allever.video.editor.ui.widget.SettingItem;
import com.allever.video.editor.R;

import com.allever.video.editor.app.Base2Activity;
import com.allever.video.editor.utils.Feedback;
import com.allever.video.editor.utils.umeng.StatisticsConstant;
import com.allever.video.editor.utils.umeng.StatisticsUtils;


/**
 */

public class SettingsActivity extends Base2Activity {
    public static final String TAG = SettingsActivity.class.getName();

    ImageView mToolbarBack;
    SettingItem mSettingFeedback;
    SettingItem mSettingAbout;
    SettingItem mSettingPremium;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ve_activity_settings);
        adaptStatusBar(findViewById(R.id.rl_top_bar));

        initView();
    }

    private void initView() {
        mToolbarBack = (ImageView) findViewById(R.id.setting_back);
        mSettingFeedback = (SettingItem) findViewById(R.id.setting_feedback);
        mSettingAbout = (SettingItem) findViewById(R.id.setting_about);
        mSettingPremium = (SettingItem) findViewById(R.id.setting_premium);

        mSettingFeedback.setOnClickListener(v -> {
            Feedback.feedback(this);
        });
        mSettingAbout.setOnClickListener(v -> {
            StatisticsUtils.statisicsCustomSettings(StatisticsConstant.EVENT_SETTINGS_ABOUT_ICON, null);

            startActivity(AboutActivity.newIntent(this));
            StatisticsUtils.statisicsCustomFunction(StatisticsConstant.EVENT_FUNC_SETTING_MORE_ABOUT_CLICK);
        });
        mToolbarBack.setOnClickListener(v -> {
            finish();
        });

        Resources resources = getResources();
        // TODO: 设置bulingbuling有问题,暂时没找到原因
//        BulingBulingDrawable bulingBulingDrawable = new BulingBulingDrawable(resources, resources.getDrawable(R.drawable.icon_setting_premium));
//        mSettingPremium.setIvDrawable(bulingBulingDrawable);
        // TODO:屏蔽主题
//        StatusBarUtils.setStatusBarColor(this, getResources().getColor(R.color.colorPrimary));
        //StatusBarUtils.setStatusBarColor(this, ThemeManager.getInstance().getResId(getTheme(), R.attr.skin_color_primary));
        int color = resources.getColor(R.color.default_btn_color_filter);
        mToolbarBack.setColorFilter(color);
//        if(ConfigManager.INSTANCE.getPurchaseSubSize() > 0){
//            mSettingPremium.setIvRight(false);
//        }else{
//            mSettingPremium.setIvRight(true);
//        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(R.anim.activity_close_enter, R.anim.activity_close_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateLockSwitch() {
//        int lockSwitchByStrategy = BatmobiSDK.isLockSwitchByStrategy();
//
//        if (lockSwitchByStrategy == 1) {
//            mSettingScreenLock.setVisibility(View.VISIBLE);
//        } else if (lockSwitchByStrategy == 0 || lockSwitchByStrategy == -1) {
//            mSettingScreenLock.setVisibility(View.GONE);
//        } else if (BatmobiSDK.isBuyUser()) {
//            mSettingScreenLock.setVisibility(View.VISIBLE);
//        } else {
//            mSettingScreenLock.setVisibility(View.GONE);
//        }
    }

    @Override
    protected boolean needExitPageAd() {
        return false;
    }
}
