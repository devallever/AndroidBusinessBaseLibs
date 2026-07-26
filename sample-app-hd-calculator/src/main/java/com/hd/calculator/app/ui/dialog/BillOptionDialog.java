package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.HdcDialogBillOptionBinding;

public class BillOptionDialog extends BaseDialog<HdcDialogBillOptionBinding> {

    //listener
    private final OptionClickListener optionClickListener;

    public BillOptionDialog(@NonNull Context context, OptionClickListener clickListener) {
        super(context);
        this.optionClickListener = clickListener;
    }

    @Override
    protected HdcDialogBillOptionBinding getViewBinding() {
        return HdcDialogBillOptionBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.tvRestoreTable.setOnClickListener(v -> {
            if (optionClickListener != null) {
                optionClickListener.onClickRestoreTable();
            }
            dismiss();
        });
        mBinding.tvModifyPayType.setOnClickListener(v -> {
            if (optionClickListener != null) {
                optionClickListener.onClickModifyPayType();
            }
            dismiss();
        });
        mBinding.tvCancelBill.setOnClickListener(v -> {
            if (optionClickListener != null) {
                optionClickListener.onClickCancelBill();
            }
            dismiss();
        });
    }

    public interface OptionClickListener {
        void onClickRestoreTable();

        void onClickModifyPayType();

        void onClickCancelBill();
    }
}
