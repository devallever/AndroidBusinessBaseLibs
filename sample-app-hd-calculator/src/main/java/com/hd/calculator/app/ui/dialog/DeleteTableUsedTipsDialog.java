package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.HdcDialogDeleteTableUsedTipsBinding;

public class DeleteTableUsedTipsDialog extends BaseDialog<HdcDialogDeleteTableUsedTipsBinding> {
    //listener
    public DeleteTableUsedTipsDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected HdcDialogDeleteTableUsedTipsBinding getViewBinding() {
        return HdcDialogDeleteTableUsedTipsBinding.inflate(getLayoutInflater(), null, false);
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
