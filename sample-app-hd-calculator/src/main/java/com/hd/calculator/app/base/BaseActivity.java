package com.hd.calculator.app.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.hd.calculator.app.util.LogUtils;

public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {
    protected VB mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.log("activity = " + getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        mBinding = getViewBinding();
        setContentView(mBinding.getRoot());
        initView();
        initData();
    }

    protected abstract VB getViewBinding();

    protected abstract void initView();

    protected abstract void initData();
}
