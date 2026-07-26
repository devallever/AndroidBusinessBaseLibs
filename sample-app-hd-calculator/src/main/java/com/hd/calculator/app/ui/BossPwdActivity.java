package com.hd.calculator.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.databinding.ActivityBossPwdBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BossPwdActivity extends BaseActivity<ActivityBossPwdBinding> {

    //stringBuilder
    private final StringBuilder mPwdBuilder = new StringBuilder();
    private final Set<String> mBossPwdSet = new HashSet<>();
    private Bundle mExtraBundle;

    public static void start(Activity activity, Bundle bundle) {
        Intent intent = new Intent(activity, BossPwdActivity.class);
        if (bundle != null) {
            intent.putExtra(ExtraKey.BOSS_PWD_BUNDLE, bundle);
        }
        activity.startActivityForResult(intent, ExtraKey.REQUEST_CODE_INPUT_BOSS_PWD);
    }

    @Override
    protected ActivityBossPwdBinding getViewBinding() {
        return ActivityBossPwdBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mExtraBundle = getIntent().getBundleExtra(ExtraKey.BOSS_PWD_BUNDLE);
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        mBinding.tvOne.setOnClickListener(v -> {
            appendCharacter("1");
        });
        mBinding.tvTwo.setOnClickListener(v -> {
            appendCharacter("2");
        });
        mBinding.tvThree.setOnClickListener(v -> {
            appendCharacter("3");
        });
        mBinding.tvFour.setOnClickListener(v -> {
            appendCharacter("4");
        });
        mBinding.tvFive.setOnClickListener(v -> {
            appendCharacter("5");
        });
        mBinding.tvSix.setOnClickListener(v -> {
            appendCharacter("6");
        });
        mBinding.tvSeven.setOnClickListener(v -> {
            appendCharacter("7");
        });
        mBinding.tvEight.setOnClickListener(v -> {
            appendCharacter("8");
        });
        mBinding.tvNight.setOnClickListener(v -> {
            appendCharacter("9");
        });
        mBinding.tvZero.setOnClickListener(v -> {
            appendCharacter("0");
        });
        mBinding.ivDelete.setOnClickListener(v -> {
            deleteLastDigit();
        });
        mBinding.tvOk.setOnClickListener(v -> {
            if (mBossPwdSet.contains(mPwdBuilder.toString())) {
                successResult();
            } else {
                ToastUtils.show("Please enter the correct boss password");
            }
        });
    }

    private void successResult() {
        Intent intent = new Intent();
        intent.putExtra(ExtraKey.BOSS_PWD_BUNDLE, mExtraBundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void initData() {
        getBossPwd();
    }

    private void appendCharacter(String character) {
        mPwdBuilder.append(character);
        updatePwdDisplay();
    }

    private void deleteLastDigit() {
        if (mPwdBuilder.length() > 0) {
            mPwdBuilder.delete(mPwdBuilder.length() - 1, mPwdBuilder.length());
        } else {

        }
        updatePwdDisplay();
    }

    private void updatePwdDisplay() {
        int count = mPwdBuilder.length();
        StringBuilder startBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            startBuilder.append("*");
        }
        mBinding.tvBossPwd.setText(startBuilder.toString());
    }

    private void getBossPwd() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<AccountEntity> accountEntities = DataBaseRepository.getInstance().getAccountList();
            for (AccountEntity accountEntity : accountEntities) {
                if (accountEntity.isBoss()) {
                    mBossPwdSet.add(accountEntity.getPassword());
                    break;
                }
            }
        });
    }
}
