package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.databinding.HdcActivityModifyDishesCountBinding;

/**
 * 待结账界面->修改数量
 */
public class ModifyDishesCountActivity extends BaseActivity<HdcActivityModifyDishesCountBinding> {
    private int mCount = 10;
    private String mName;
    private boolean mIsOrdered;

    public static void startActivity(Activity context, String dishesName, int dishesCount, boolean isOrdered) {
        Intent intent = new Intent(context, ModifyDishesCountActivity.class);
        intent.putExtra(ExtraKey.DISHES_NAME, dishesName);
        intent.putExtra(ExtraKey.DISHES_COUNT, dishesCount);
        intent.putExtra(ExtraKey.DISHES_IS_ORDERED, isOrdered);
        context.startActivityForResult(intent, ExtraKey.REQUEST_CODE_MODIFY_DISHES_COUNT);
    }

    @Override
    protected HdcActivityModifyDishesCountBinding getViewBinding() {
        return HdcActivityModifyDishesCountBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        initExtraData();
        mBinding.includeTopBar.tvTitle.setText(getString(R.string.hdc_modify_count));
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());

        mBinding.tvCount.setText(mCount + "");
        mBinding.tvName.setText(mName);

        initClickListener();
    }

    @Override
    protected void initData() {

    }

    private void initExtraData() {
        mName = getIntent().getStringExtra(ExtraKey.DISHES_NAME);
        mCount = getIntent().getIntExtra(ExtraKey.DISHES_COUNT, 0);
        mIsOrdered = getIntent().getBooleanExtra(ExtraKey.DISHES_IS_ORDERED, false);
    }

    private void initClickListener() {
        mBinding.btnAdd.setOnClickListener(v -> {
            mCount++;
            mBinding.tvCount.setText(String.valueOf(mCount));
        });
        mBinding.btnDel.setOnClickListener(v -> {
            mCount--;
            if (mCount < 0) {
                mCount = 0;
            }
            mBinding.tvCount.setText(String.valueOf(mCount));

        });

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
        mBinding.tvOk.setOnClickListener(v -> {
            if (mIsOrdered && mCount == 0) {
                if (AccountManager.getIns().canReduceDishes()) {
                    handleNormalAction();
                } else {
                    //无权限
                    BossPwdActivity.start(this, null);
                }
            } else {
                handleNormalAction();
            }
        });
    }

    private void handleNormalAction() {
        Intent intent = new Intent();
        intent.putExtra(ExtraKey.RESULT_DISHES_COUNT, mCount);
        setResult(RESULT_OK, intent);
        finish();
        //修改数量
    }

    private void appendCharacter(String character) {
        String current = mBinding.tvCount.getText().toString();
        if (current.equals("0")) {
            mBinding.tvCount.setText(character);
        } else {
            mBinding.tvCount.append(character);
        }

        mCount = Integer.parseInt(mBinding.tvCount.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_INPUT_BOSS_PWD) {
            handleNormalAction();
        }
    }
}
