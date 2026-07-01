package com.allever.video.editor.app;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import com.android.absbase.App;
import com.android.absbase.utils.AppUtils;
import com.android.absbase.utils.TaskRunnable;


/**
 */

public class Base2Activity extends AppCompatActivity {

    protected boolean mIsCanShowRateDialog = true;
    private boolean mIsFinishing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTranslucentStatusBar()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
//        isActive = true;

//        StatisticsUtils.statisics(StatisticsConstant.EVENT_PREFIX_FUNCTION + "_page", "in",
//                "usertype", String.valueOf(InappAdManager.getInstance().isBuyUser()),
//                "class", getClass().getSimpleName());

        loadExitPageAd();
    }

    protected boolean isTranslucentStatusBar() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

//        boolean isReenter = !isActive;
//        if (!isActive) {
//            isActive = true;
//            boolean showRate = RateGuide.showRate(this);
//
////            if (!showRate) {
////                UpdateManager updateManager = UpdateManager.obtain();
////                updateManager.release();
////                if (!updateManager.isUpdateDialogShowing()) {
////                    updateManager.checkUpdate(this);
////                }
////            }
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private boolean isActive;
    @Override
    protected void onStop() {
        super.onStop();
        if (mIsCanShowRateDialog && !AppUtils.isFrontActivity(App.getContext(), App.getPackageName())) {
            //app 进入后台
            isActive = false;
        }
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    public void finish() {
        if (mIsFinishing) {
            return;
        }
        mIsFinishing = true;
        finishInternal();

    }

    protected void finishInternal() {
        showExitPageAd(new TaskRunnable(TaskRunnable.TYPE_MAIN) {
            @Override
            public void run() {
                Base2Activity.super.finish();
            }
        });
    }

    protected boolean needExitPageAd() {
        return true;
    }

    private void loadExitPageAd() {
//        if (!needExitPageAd()) {
//            ExitPageAdProvider.getInstance().statisticsNoRequestAd(getClass().getSimpleName());
//            return;
//        }
//        ExitPageAdProvider.getInstance().loadAd();
    }

    private void showExitPageAd(TaskRunnable run) {
//        if (!needExitPageAd()) {
//            ExitPageAdProvider.getInstance().statisticsNoShowAd(getClass().getSimpleName());
//            run.runSelfAdaptation();
//            return;
//        }
//        String simpleName = getClass().getSimpleName();
//        if (!ExitPageAdProvider.getInstance().showAd(run, simpleName)) {
            run.runSelfAdaptation();
//        }
    }
}
