package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.DialogTransformTableBinding;

public class TransformTableTipsDialog extends BaseDialog<DialogTransformTableBinding> {
    //listener
    private final ClickListener clickListener;

    public TransformTableTipsDialog(@NonNull Context context, ClickListener clickListener) {
        super(context);
        this.clickListener = clickListener;
    }

    @Override
    protected DialogTransformTableBinding getViewBinding() {
        return DialogTransformTableBinding.inflate(getLayoutInflater(), null, false);
    }

    @Override
    protected void initView() {
        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnTransform.setOnClickListener(v -> {
            dismiss();
            if (clickListener != null) {
                clickListener.onClickTransform();
            }
        });
        mBinding.btnDelete.setOnClickListener(v -> {
            dismiss();
            if (clickListener != null) {
                clickListener.onClickDelete();
            }
        });
    }

    public interface ClickListener {
        void onClickTransform();

        void onClickDelete();
    }
}
