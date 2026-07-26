package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.DialogDeleteTableOrderBinding;

public class DeleteTableOrderTipsDialog extends BaseDialog<DialogDeleteTableOrderBinding> {
    //listener
    private final ClickListener clickListener;

    public DeleteTableOrderTipsDialog(@NonNull Context context, ClickListener clickListener) {
        super(context);
        this.clickListener = clickListener;
        setCancelable(false);
    }

    @Override
    protected DialogDeleteTableOrderBinding getViewBinding() {
        return DialogDeleteTableOrderBinding.inflate(getLayoutInflater(), null, false);
    }

    @Override
    protected void initView() {
        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
            if (clickListener != null) {
                clickListener.onClickCancel();
            }
        });
        mBinding.btnOk.setOnClickListener(v -> {
            dismiss();
            if (clickListener != null) {
                clickListener.onClickOk();
            }
        });
        mBinding.btnCancel.setOnClickListener(v -> {
            dismiss();
            if (clickListener != null) {
                clickListener.onClickCancel();
            }
        });
    }

    public interface ClickListener {
        void onClickOk();

        void onClickCancel();
    }
}
