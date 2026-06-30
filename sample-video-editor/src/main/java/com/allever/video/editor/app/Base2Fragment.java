package com.allever.video.editor.app;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.android.absbase.ui.BaseFragment;

/**
 */

public class Base2Fragment extends BaseFragment {


    protected boolean mIsPageSelected;

    public boolean isNeedRefresh = false;

    public boolean isCreated = false;

    public void onPageSelected() {
        mIsPageSelected = true;
    }

    public void onPageUnSelected() {
        mIsPageSelected = false;
    }

    public boolean isPageSelected() {
        return mIsPageSelected;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    public void onNewIntent(Intent intent) {}

    public void onMoveClick(View v) {}

    public void onCancelClick(View v) {}

    public void onShareClick(View v) {}

    public boolean onBackClick(View v) {
        return false;
    }

    public void onDeleteClick(View v) {}

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}


    public void doOnStart() {}

    public void refreshData() {}

    public void forceRefreshData() {}

    public void setNeedRefresh(boolean flag) {
        isNeedRefresh = flag;
    }

    public int getImageNum() {
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
//        UmengStatistics.onPageStart(this.getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
//        UmengStatistics.onPageEnd(this.getClass().getName());
    }

}
