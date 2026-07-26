package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.HdcDialogNewTableBinding;
import com.hd.calculator.app.util.ToastUtils;

public class NewTableDialog extends BaseDialog<HdcDialogNewTableBinding> {
    private int zoomId = 1;

    public NewTableDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected HdcDialogNewTableBinding getViewBinding() {
        return HdcDialogNewTableBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        updateSelectedZoom(1);
        mBinding.ivClose.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.btnOk.setOnClickListener(v -> {
            ToastUtils.show("OK");
            dismiss();
        });
        mBinding.tvZoom1.setOnClickListener(v -> {
            updateSelectedZoom(1);
        });
        mBinding.tvZoom2.setOnClickListener(v -> {
            updateSelectedZoom(2);
        });
    }

    private void updateSelectedZoom(int zoom) {
        zoomId = zoom;
        switch (zoomId) {
            case 1:
                mBinding.tvZoom1.setBackgroundResource(R.drawable.hdc_shape_blue_r45);
                mBinding.tvZoom2.setBackgroundResource(R.drawable.hdc_shape_gray_r45);
                //设置颜色
                mBinding.tvZoom1.setTextColor(MyApp.context.getResources().getColor(R.color.white));
                mBinding.tvZoom2.setTextColor(MyApp.context.getResources().getColor(R.color.color_646566));

                break;
            case 2:
                mBinding.tvZoom1.setBackgroundResource(R.drawable.hdc_shape_gray_r45);
                mBinding.tvZoom2.setBackgroundResource(R.drawable.hdc_shape_blue_r45);
                //设置颜色
                mBinding.tvZoom1.setTextColor(MyApp.context.getResources().getColor(R.color.color_646566));
                mBinding.tvZoom2.setTextColor(MyApp.context.getResources().getColor(R.color.white));
                break;
        }
    }
}
