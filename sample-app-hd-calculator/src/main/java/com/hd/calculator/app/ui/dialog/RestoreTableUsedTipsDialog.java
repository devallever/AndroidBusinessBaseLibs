package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.DialogRestoreTableUsedTipsBinding;

public class RestoreTableUsedTipsDialog extends BaseDialog<DialogRestoreTableUsedTipsBinding> {

    private final OptionClickListener clickListener;

    public RestoreTableUsedTipsDialog(@NonNull Context context, OptionClickListener clickListener) {
        super(context);
        this.clickListener = clickListener;
    }

    @Override
    protected DialogRestoreTableUsedTipsBinding getViewBinding() {
        return DialogRestoreTableUsedTipsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnChooseTable.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClickChooseTable();
            }
            dismiss();
        });
        mBinding.btnOk.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClickChooseTable();
            }
            dismiss();
        });
    }

    public interface OptionClickListener {
        void onClickChooseTable();
    }
}
