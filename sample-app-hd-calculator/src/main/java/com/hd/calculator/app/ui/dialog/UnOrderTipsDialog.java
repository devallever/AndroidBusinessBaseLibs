package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.databinding.HdcDialogUnOrderTipsBinding;
import com.hd.calculator.app.function.UserLog;

/***
 * 增加餐品退出提示
 */
public class UnOrderTipsDialog extends BaseDialog<HdcDialogUnOrderTipsBinding> {
    //optionClickListener
    private OptionClickListener optionClickListener;

    public UnOrderTipsDialog(@NonNull Context context) {
        super(context);
    }

    //setOptionClickListener
    public void setOptionClickListener(OptionClickListener optionClickListener) {
        this.optionClickListener = optionClickListener;
    }

    @Override
    protected HdcDialogUnOrderTipsBinding getViewBinding() {
        return HdcDialogUnOrderTipsBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.itemPrint.setOnClickListener(v -> {
            dismiss();
            if (optionClickListener != null) {
                optionClickListener.onClickMakerOrderAndPrint();
            }
        });
        mBinding.itemUnPrint.setOnClickListener(v -> {
            dismiss();
            if (optionClickListener != null) {
                optionClickListener.onClickMakerOrderAndUnPrint();
            }
        });

        mBinding.itemDelete.setOnClickListener(v -> {
            dismiss();
            if (optionClickListener != null) {
                optionClickListener.onClickDrop();
            }
        });
    }

    public interface OptionClickListener {
        void onClickMakerOrderAndPrint();

        void onClickMakerOrderAndUnPrint();

        void onClickDrop();
    }
}
