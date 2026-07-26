package com.hd.calculator.app.base;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.hd.calculator.app.util.DisplayUtils;

public abstract class BaseDialog<VB extends ViewBinding> extends Dialog {

    protected VB mBinding;

    public BaseDialog(@NonNull Context context) {
        super(context);
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        //统一宽度屏幕宽度减去边距
        layoutParams.width = DisplayUtils.getScreenWidth() - DisplayUtils.dp2px(30 * 2);
        window.setAttributes(layoutParams);
        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);


        mBinding = getViewBinding();
        setContentView(mBinding.getRoot());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    protected abstract VB getViewBinding();

    protected abstract void initView();
}
