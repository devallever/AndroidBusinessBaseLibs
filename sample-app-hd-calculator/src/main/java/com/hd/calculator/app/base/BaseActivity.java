package com.hd.calculator.app.base;

import android.os.Bundle;

import androidx.viewbinding.ViewBinding;

import com.hd.calculator.app.util.LogUtils;

import app.allever.android.lib.core.base.AbstractActivity;

public abstract class BaseActivity<VB extends ViewBinding> extends AbstractActivity {
    protected VB mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.log("activity = " + getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        mBinding = getViewBinding();
        setContentView(mBinding.getRoot());
        adaptStatusBar(mBinding.getRoot());
        initView();
        initData();
    }

    protected abstract VB getViewBinding();

    protected abstract void initView();

    protected abstract void initData();
}
