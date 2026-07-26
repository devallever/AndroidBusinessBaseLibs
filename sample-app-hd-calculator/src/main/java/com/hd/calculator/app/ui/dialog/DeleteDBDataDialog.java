package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.DialogDeleteDataBinding;

public class DeleteDBDataDialog extends BaseDialog<DialogDeleteDataBinding> {

    private OptionClickListener mOptionClickListener;

    public DeleteDBDataDialog(@NonNull Context context) {
        super(context);
    }

    //setListener
    public void setOptionClickListener(OptionClickListener optionClickListener) {
        mOptionClickListener = optionClickListener;
    }

    @Override
    protected DialogDeleteDataBinding getViewBinding() {
        return DialogDeleteDataBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.btnDeleteTodayBefore.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onClickDeleteTodayBefore();
                dismiss();
            }
        });

        mBinding.btnDeleteToday.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onClickToday();
                dismiss();
            }
        });

        mBinding.btnCancel.setOnClickListener(v -> {
            dismiss();
        });
    }

    public interface OptionClickListener {
        void onClickDeleteTodayBefore();
        void onClickToday();
    }
}
