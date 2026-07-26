package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.HdcDialogDeleteTableBinding;
import com.hd.calculator.app.util.ToastUtils;

public class DeleteTableDialog extends BaseDialog<HdcDialogDeleteTableBinding> {
    //listener
    private final ClickListener clickListener;

    public DeleteTableDialog(@NonNull Context context, ClickListener clickListener) {
        super(context);
        this.clickListener = clickListener;
    }

    @Override
    protected HdcDialogDeleteTableBinding getViewBinding() {
        return HdcDialogDeleteTableBinding.inflate(getLayoutInflater(), null, false);
    }

    @Override
    protected void initView() {
        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnOk.setOnClickListener(v -> {
            ToastUtils.show("Ok");
            dismiss();
            if (clickListener != null) {
                clickListener.onClickOk();
            }
        });
        mBinding.btnCancel.setOnClickListener(v -> {
            ToastUtils.show("Cancel");
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
