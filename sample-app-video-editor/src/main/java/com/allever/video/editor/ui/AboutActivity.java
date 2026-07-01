package com.allever.video.editor.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.absbase.utils.AppUtils;
import com.allever.video.editor.R;
import com.allever.video.editor.app.Base2Activity;
import com.allever.video.editor.utils.FontUtil;
/**
 */
@SuppressLint("NonConstantResourceId")
public class AboutActivity extends Base2Activity {
    ImageView mToolbarBack;
    TextView mAboutVersion;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        return intent;
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView(savedInstanceState);
        initData(savedInstanceState);
        adaptStatusBar(findViewById(R.id.rl_title_bar));
    }

    protected void initContentView(Bundle savedInstanceState) {
        setContentView(R.layout.ve_activity_about);
        mAboutVersion = findViewById(R.id.about_version);
        mToolbarBack = findViewById(R.id.top_back);
        mToolbarBack.setOnClickListener(v -> {
            finish();
        });
        FontUtil.setCustomFont(mAboutVersion);
        FontUtil.setCustomFont((TextView) findViewById(R.id.about_name));
        FontUtil.setCustomFont((TextView) findViewById(R.id.about_privacy));
        FontUtil.setCustomFont((TextView) findViewById(R.id.about_ad));
        FontUtil.setCustomFont((TextView) findViewById(R.id.tv_copyright));

        int color = getResources().getColor(R.color.default_btn_color_filter);
        mToolbarBack.setColorFilter(color);
    }

    public void initView(Bundle savedInstanceState) {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    protected void initData(Bundle savedInstanceState) {
        String text = String.format("V%s", "1.0");
        mAboutVersion.setText(text);
    }


    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected boolean needExitPageAd() {
        return false;
    }
}
