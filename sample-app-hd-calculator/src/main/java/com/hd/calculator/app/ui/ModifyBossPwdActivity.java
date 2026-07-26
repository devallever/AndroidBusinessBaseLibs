package com.hd.calculator.app.ui;

import android.app.Activity;
import android.content.Intent;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.databinding.HdcActivityModifyBossPwdBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.util.StringUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

public class ModifyBossPwdActivity extends BaseActivity<HdcActivityModifyBossPwdBinding> {

    AccountEntity bossAccount;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, ModifyBossPwdActivity.class);
        activity.startActivityForResult(intent, ExtraKey.REQUEST_CODE_MODIFY_PWD);
    }

    @Override
    protected HdcActivityModifyBossPwdBinding getViewBinding() {
        return HdcActivityModifyBossPwdBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        //back
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        mBinding.includeTopBar.tvTitle.setText(getString(R.string.hdc_modify_pin_title));

        mBinding.btnOk.setOnClickListener(v -> {
            handleModifyPwd();
        });
    }

    @Override
    protected void initData() {
        getBossAccount();

    }

    private void getBossAccount() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            bossAccount = DataBaseRepository.getInstance().getBossAccount();
        });
    }

    private void handleModifyPwd() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            String oldPwd = mBinding.etOldPin.getText().toString();
            String newPwd = mBinding.etNewPin.getText().toString();
            String confirmPwd = mBinding.etNewPinConfirm.getText().toString();
            if (oldPwd.isEmpty()) {
                ToastUtils.show(getString(R.string.hdc_old_pin_hint));
                return;
            }
            if (newPwd.isEmpty()) {
                ToastUtils.show(getString(R.string.hdc_new_pin_hint));
                return;
            }
            if (confirmPwd.isEmpty()) {
                ToastUtils.show(getString(R.string.hdc_new_pin_confirm_hint));
                return;
            }

            if (!oldPwd.equals(bossAccount.getPassword())) {
                ToastUtils.show("wrong old pin");
                return;
            }

            //checkInteger
            if (!StringUtils.isInteger(newPwd)) {
                ToastUtils.show("new pin must be number");
                return;
            }

            if (!StringUtils.isInteger(confirmPwd)) {
                ToastUtils.show("confirm pin must be number");
                return;
            }

            if (newPwd.startsWith("0")) {
                ToastUtils.show("new pin can not start with 0");
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                ToastUtils.show("new pin not equal confirm pin");
                return;
            }

            bossAccount.setPassword(newPwd);
            DataBaseRepository.getInstance().updateAccount(bossAccount);

            setResult(RESULT_OK);
            finish();

        });


    }
}
