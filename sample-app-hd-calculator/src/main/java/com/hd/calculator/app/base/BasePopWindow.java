package com.hd.calculator.app.base;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import androidx.viewbinding.ViewBinding;

public abstract class BasePopWindow<VB extends ViewBinding> {

    private final PopupWindow mPopupWindow;
    protected VB mBinding;
    protected Context context;

    protected BasePopWindow(Context context) {
        this.context = context;
        this.mBinding = intBinding();
        int width = getWidth();
        int height = getHeight();
        mPopupWindow = new PopupWindow(mBinding.getRoot(), width, height, true);
        // 设置背景（可选）
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        // 设置点击外部消失
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        initView();
    }

    public abstract VB intBinding();

    public abstract void initView();

    public abstract int getWidth();

    public abstract int getHeight();

    public void show(View view) {
//        mPopupWindow.showAsDropDown(view);
        mPopupWindow.showAsDropDown(view, 0, 0, Gravity.NO_GRAVITY);
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

}
