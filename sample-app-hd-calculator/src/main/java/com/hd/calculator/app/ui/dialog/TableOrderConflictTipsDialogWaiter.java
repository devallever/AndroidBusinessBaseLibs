package com.hd.calculator.app.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.databinding.DialogTableOrderConflictTipsBossBinding;
import com.hd.calculator.app.databinding.DialogTableOrderConflictTipsWaiterBinding;

public class TableOrderConflictTipsDialogWaiter extends BaseDialog<DialogTableOrderConflictTipsWaiterBinding> {
    private int mTableCode;
    private OptionClickListener mOptionClickListener;

    public void setOptionClickListener(OptionClickListener optionClickListener) {
        mOptionClickListener = optionClickListener;
    }
    public TableOrderConflictTipsDialogWaiter(@NonNull Context context, int tableCode) {
        super(context);
        mTableCode = tableCode;
    }

    @Override
    protected DialogTableOrderConflictTipsWaiterBinding getViewBinding() {
        return DialogTableOrderConflictTipsWaiterBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        setCancelable(false);
        mBinding.tvConflictTitle.setText(TableManager.getIns().getDisplayTableName(mTableCode, OrderType.ORDER_TYPE_IN_HOUSE) + "冲突");
//        mBinding.tvTips.setText(String.format("当前桌台%s号订单冲突，请选择本地数据或服务器数据", mTableCode));
        mBinding.btnNotify.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onWaiterClickNotify();
            }
            dismiss();
        });
        mBinding.btnTransform.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onWaiterClickTransform();
            }
            dismiss();
        });
    }

    public interface OptionClickListener{
        void onWaiterClickNotify();
        void onWaiterClickTransform();
    }
}
