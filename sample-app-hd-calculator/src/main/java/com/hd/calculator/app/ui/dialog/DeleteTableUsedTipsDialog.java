package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.DialogDeleteTableUsedTipsBinding;

public class DeleteTableUsedTipsDialog extends BaseDialog<DialogDeleteTableUsedTipsBinding> {
    //listener
    public DeleteTableUsedTipsDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected DialogDeleteTableUsedTipsBinding getViewBinding() {
        return DialogDeleteTableUsedTipsBinding.inflate(getLayoutInflater(), null, false);
    }

    @Override
    protected void initView() {
        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnOk.setOnClickListener(v -> {
            dismiss();
        });
    }
}
