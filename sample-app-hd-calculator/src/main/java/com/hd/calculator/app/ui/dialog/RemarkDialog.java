package com.hd.calculator.app.ui.dialog;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.databinding.HdcDialogRemarkBinding;
import com.hd.calculator.app.ui.adapter.RemarkAdapter;
import com.hd.calculator.app.ui.item.RemarkItem;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class RemarkDialog extends BaseDialog<HdcDialogRemarkBinding> {

    //adapter
    private RemarkAdapter mAdapter;
    //datalist
    private final List<RemarkItem> mDataList = new ArrayList<>();

    private final OnItemSelectedListener mListener;

    public RemarkDialog(@NonNull Context context, OnItemSelectedListener listener) {
        super(context);
        mListener = listener;
    }

    @Override
    protected HdcDialogRemarkBinding getViewBinding() {
        return HdcDialogRemarkBinding.inflate(getLayoutInflater(), null, false);
    }

    @Override
    protected void initView() {
        initRemark();
        updateAddCustomRemarkState();
        initRemarkData();
        mBinding.btnAdd.setOnClickListener(v -> {
            RemarkItem item = new RemarkItem();
            item.setRemark(mBinding.etRemark.getText().toString());
            item.setSelect(true);
            mDataList.add(0, item);
            mAdapter.notifyDataSetChanged();
            mBinding.etRemark.setText("");
        });
        mBinding.tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBinding.tvContinue.setOnClickListener(v -> {
            StringBuilder builder = new StringBuilder();
            for (RemarkItem item : mDataList) {
                if (item.isSelect()) {
                    builder.append(item.getRemark());
                    if (mDataList.indexOf(item) != mDataList.size() - 1) {
                        builder.append(";");
                    }
                }
            }

            if (mListener != null) {
                mListener.onItemSelected(builder.toString());
            }

            dismiss();

        });
        mBinding.etRemark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateAddCustomRemarkState();
            }
        });
    }

    private void initRemark() {
        LogUtils.log("initRemark");
        //initRv
        mBinding.rvRemark.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new RemarkAdapter(mDataList);
        mBinding.rvRemark.setAdapter(mAdapter);
        mAdapter.setItemClickListener(item -> {
        });
    }

    private void updateAddCustomRemarkState() {
        String remark = mBinding.etRemark.getText().toString();
        mBinding.btnAdd.setEnabled(!remark.isEmpty());
        mBinding.btnAdd.setClickable(!remark.isEmpty());
        if (remark.isEmpty()) {
            mBinding.btnAdd.setBackgroundResource(R.drawable.hdc_shape_gray_disable_frame_r45);
            mBinding.btnAdd.setTextColor(MyApp.context.getResources().getColor(R.color.color_C3C6C7));
        } else {
            mBinding.btnAdd.setBackgroundResource(R.drawable.hdc_shape_blue_frame_r45);
            mBinding.btnAdd.setTextColor(MyApp.context.getResources().getColor(R.color.color_485ABE));
        }
    }

    private void initRemarkData() {
        LogUtils.log("initTestData");
        ThreadUtils.runOnIoThreadDelayed(() -> {
            mDataList.clear();
            mDataList.add(new RemarkItem("ohne Glas"));
            mDataList.add(new RemarkItem("2 Glaeser"));
            mDataList.add(new RemarkItem("3 Glaeser"));
            mDataList.add(new RemarkItem("4 Glaeser"));
            mDataList.add(new RemarkItem("5 Glaeser "));
            mDataList.add(new RemarkItem("ohne Eis"));
            mDataList.add(new RemarkItem("mit Eis"));
            mDataList.add(new RemarkItem("Glas Eis"));
            mDataList.add(new RemarkItem("nicht kalt"));
            mDataList.add(new RemarkItem("mit Stillwasser"));
            mDataList.add(new RemarkItem("mit Strohhalm"));
            mDataList.add(new RemarkItem("mit Zitrone"));
            mDataList.add(new RemarkItem("ohne Zitrone"));
            mDataList.add(new RemarkItem("alkoholfrei"));
            mDataList.add(new RemarkItem("wenig Bier"));
            mDataList.add(new RemarkItem("mehr Bier"));
            mDataList.add(new RemarkItem("mit Kristall"));
            mDataList.add(new RemarkItem("mehr Wasser"));
            mDataList.add(new RemarkItem("weniger Wasser"));
            mDataList.add(new RemarkItem("mehr suess"));
            mDataList.add(new RemarkItem("weniger suess"));
            mDataList.add(new RemarkItem("mit Zucker"));
            mDataList.add(new RemarkItem("ohne Zucker"));
            mDataList.add(new RemarkItem("mit Suessstoff"));
            mDataList.add(new RemarkItem("mit Milch"));

            ThreadUtils.runOnUiThread(() -> mAdapter.notifyDataSetChanged());
        });
    }

    public interface OnItemSelectedListener {
        void onItemSelected(String remark);
    }
}
