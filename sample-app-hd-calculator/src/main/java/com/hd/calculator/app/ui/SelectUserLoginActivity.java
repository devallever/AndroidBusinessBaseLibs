package com.hd.calculator.app.ui;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.databinding.ActivitySelectUserLoginBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.ui.dialog.SelectUserDialog;
import com.hd.calculator.app.ui.item.UserItem;
import com.hd.calculator.app.util.RippleHelper;
import com.hd.calculator.app.util.ThreadUtils;

public class SelectUserLoginActivity extends BaseActivity<ActivitySelectUserLoginBinding> {
    private SelectUserDialog mSelectUserDialog;
    private UserItem mSelecgtUserItem;

    private boolean mFromMain;

    public static void start(Activity activity, boolean fromMain) {
        Intent intent = new Intent(activity, SelectUserLoginActivity.class);
        intent.putExtra(ExtraKey.FROM_MAIN, fromMain);
        activity.startActivityForResult(intent, ExtraKey.REQUEST_CODE_SELECT_USER_LOGIN);
    }


    @Override
    protected ActivitySelectUserLoginBinding getViewBinding() {
        return ActivitySelectUserLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        initExtraData();
        mSelecgtUserItem = new UserItem();
        AccountEntity accountEntity = AccountManager.getIns().getAccount();
        mSelecgtUserItem.setUserId(accountEntity.getUserId());
        mSelecgtUserItem.setUserName(accountEntity.getUserName());
        mSelecgtUserItem.setPwd(accountEntity.getPassword());
        mSelecgtUserItem.setBoss(accountEntity.isBoss());
        mSelecgtUserItem.setSelected(true);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {
                performClickSwitchUser();
            }
        });
        initSelectUserDialog();
        mBinding.tvUser.setText(AccountManager.getIns().getAccount().getUserName());
        RippleHelper.setOnClickListener(mBinding.includeTopBar.ivBack, () -> {
            performClickSwitchUser();
        });
        RippleHelper.setOnClickListener(mBinding.btnLogin, () -> {
            performClickSwitchUser();
        });
        RippleHelper.setOnClickListener(mBinding.tvUser, () -> {
            mSelectUserDialog.show();
        });
    }

    private void performClickSwitchUser() {
        if (mSelecgtUserItem == null) {
            startMainActivity();
            return;
        }

//            if (mSelecgtUserItem.getUserName().equals(AccountManager.getIns().getAccount().getUserName())) {
//                startMainActivity();
//                return;
//            }


        if (mSelecgtUserItem.isBoss()) {
            BossPwdActivity.start(this, null);
            return;
        }

        handleSwitchUser();
    }

    private void initExtraData() {
        mFromMain = getIntent().getBooleanExtra(ExtraKey.FROM_MAIN, false);
    }

    private void initSelectUserDialog() {
        mSelectUserDialog = new SelectUserDialog(this);
        mSelectUserDialog.setItemClickListener(item -> {
            mSelecgtUserItem = item;
            mBinding.tvUser.setText(item.getUserName());
        });
    }

    @Override
    protected void initData() {

    }

    private void handleSwitchUser() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            AccountEntity accountEntity = DataBaseRepository.getInstance().getByUserId(mSelecgtUserItem.getUserId());
            AccountManager.getIns().updateAccount(accountEntity);
            startMainActivity();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_INPUT_BOSS_PWD) {
            handleSwitchUser();
        }
    }

    private void startMainActivity() {
        finish();
        startActivity(new Intent(SelectUserLoginActivity.this, MainActivity.class));
    }
}
