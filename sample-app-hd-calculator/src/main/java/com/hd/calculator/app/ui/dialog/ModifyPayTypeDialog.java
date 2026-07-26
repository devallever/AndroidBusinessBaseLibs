package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.constant.PayType;
import com.hd.calculator.app.databinding.HdcDialogModifyPayTypeBinding;

public class ModifyPayTypeDialog extends BaseDialog<HdcDialogModifyPayTypeBinding> {

    private final ItemChangedListener listener;
    private final int mOldPayType;
    private int mNewPayType;

    public ModifyPayTypeDialog(@NonNull Context context, int payType, ItemChangedListener listener) {
        super(context);
        this.listener = listener;
        mOldPayType = payType;
    }

    @Override
    protected HdcDialogModifyPayTypeBinding getViewBinding() {
        return HdcDialogModifyPayTypeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        updatePayType(mOldPayType);
        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnOk.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onClickConfirm(mNewPayType);
            }
        });
        mBinding.itemCard.setOnClickListener(v -> {
            updatePayType(PayType.PAY_TYPE_CARD);
        });
        mBinding.itemCash.setOnClickListener(v -> {
            updatePayType(PayType.PAY_TYPE_CASH);
        });
    }

    private void updatePayType(int payType) {
        mNewPayType = payType;
        if (payType == PayType.PAY_TYPE_CASH) {
            mBinding.ivCheckCash.setImageResource(R.drawable.hdc_ic_check_2_checked);
            mBinding.ivCheckCard.setImageResource(R.drawable.hdc_ic_check_2_uncheck);
        } else {
            mBinding.ivCheckCash.setImageResource(R.drawable.hdc_ic_check_2_uncheck);
            mBinding.ivCheckCard.setImageResource(R.drawable.hdc_ic_check_2_checked);
        }
    }

    public interface ItemChangedListener {
        void onClickConfirm(int newPayType);
    }
}
