package com.hd.calculator.app.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.DialogCommonTipsBinding;

public class CommonTipsDialog extends BaseDialog<DialogCommonTipsBinding> {
    //listener
    private final ClickListener clickListener;
    private String mTitle = "";
    private String mMessage;

    private String mOkText;
    private String mCancelText;

    public CommonTipsDialog(@NonNull Context context, String message, ClickListener clickListener) {
        super(context);
        this.clickListener = clickListener;
        this.mMessage = message;
    }

    public void setTitle(String title) {
        if (mTitle != null) {
            mTitle = title;
        }
    }

    public void setBtnText(String okText, String cancelText) {
        if (okText != null) {
            mOkText = okText;
            mBinding.btnOk.setText(okText);
        }
        if (cancelText != null) {
            mCancelText = cancelText;
            mBinding.btnCancel.setText(cancelText);
        }
    }

    public void setMessage(String message) {
        if (message != null) {
            mMessage = message;
        }
        mBinding.tvTips.setText(message);
    }

    @Override
    protected DialogCommonTipsBinding getViewBinding() {
        return DialogCommonTipsBinding.inflate(getLayoutInflater(), null, false);
    }

    @Override
    protected void initView() {
        if (!mTitle.isEmpty()) {
            mBinding.tvTitle.setText(mTitle);
        }
        mBinding.tvTips.setText(mMessage);

        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
            if (clickListener != null) {
                clickListener.onClickClose(this);
            }
        });
        mBinding.btnOk.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClickOk(this);
            } else {
                dismiss();
            }
        });
        mBinding.btnCancel.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClickCancel(this);
            } else {
                dismiss();
            }
        });
    }

    public interface ClickListener {
        void onClickOk(DialogInterface dialog);

        void onClickCancel(DialogInterface dialog);

        default void onClickClose(DialogInterface dialog){}
    }
}
