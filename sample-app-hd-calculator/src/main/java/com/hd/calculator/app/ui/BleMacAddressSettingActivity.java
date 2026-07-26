package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.Config;
import com.hd.calculator.app.databinding.ActivityBleMacSettingBinding;
import com.hd.calculator.app.util.ToastUtils;

public class BleMacAddressSettingActivity extends BaseActivity<ActivityBleMacSettingBinding> {
    @Override
    protected ActivityBleMacSettingBinding getViewBinding() {
        return ActivityBleMacSettingBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        mBinding.ivBack.setOnClickListener(v -> finish());
        mBinding.etMac.setText(Config.getPrinterBleMac());
        mBinding.tvCurrentPrinterBleMac.setText("Current Mac: " + Config.getPrinterBleMac());
        mBinding.ivConfirm.setOnClickListener(v -> {
            //bleAddress
            String bleAddress = mBinding.etMac.getText().toString();
            if (bleAddress.isEmpty()) {
                ToastUtils.show("empty");
                return;
            }
            Config.setPrinterBle(bleAddress);
            finish();
        });
    }

    @Override
    protected void initData() {

    }
}
