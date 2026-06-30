package com.allever.video.editor.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.RelativeLayout

import com.android.absbase.utils.thread.ThreadPool
import com.allever.video.editor.ConfigManager
import com.allever.video.editor.R


class SplashActivity : Activity() {

    private var mSplashLayout: RelativeLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //            // 5.0以上全透明
        //            Window window = getWindow();
        //            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        //                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //            window.setStatusBarColor(Color.TRANSPARENT);
        //            window.setNavigationBarColor(Color.TRANSPARENT);
        //        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //            // 5.0以下 4.4以上半透明
        //            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //        }
        setContentView(R.layout.activity_splash)

        ConfigManager.recordOpenApp()

        //        if (SPDataManager.hasCheckPrivacy()) {
        showSplashView()
        //        } else {
        //            showPrivacyCheckView();
        //            addStoreListener();
        //        }

    }

    private fun showSplashView() {
        val viewStub = findViewById<ViewStub>(R.id.viewstub_splash)
        mSplashLayout = viewStub.inflate() as RelativeLayout
        val ivBackground = mSplashLayout!!.findViewById<ImageView>(R.id.iv_background)
        val icon = mSplashLayout!!.findViewById<View>(R.id.icon)
        val title = mSplashLayout!!.findViewById<View>(R.id.title)
        if (ivBackground != null) {
            //            ivBackground.setImageResource(R.drawable.startup_page_bg);
            //            if (icon != null) {
            //                icon.setVisibility(View.GONE);
            //            }
            //            if (title != null) {
            //                title.setVisibility(View.GONE);
            //            }
            ivBackground.visibility = View.GONE
        }
        ThreadPool.runUITask({ jumpToMain() }, 2000)
    }

    private fun jumpToMain() {
        if (ConfigManager.openAppCountInDay == 1 && ConfigManager.purchaseSubSize < 1) {
        } else {
            AlbumActivity.startActivity(this)
        }
        finish()
    }
}
