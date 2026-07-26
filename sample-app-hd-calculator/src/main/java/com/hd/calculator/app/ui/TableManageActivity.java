package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.GridLayoutManager;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.databinding.HdcActivityTableManageBinding;
import com.hd.calculator.app.ui.adapter.TableManageAdapter;
import com.hd.calculator.app.ui.dialog.DeleteTableDialog;
import com.hd.calculator.app.ui.dialog.DeleteTableUsedTipsDialog;
import com.hd.calculator.app.ui.dialog.NewTableDialog;
import com.hd.calculator.app.ui.item.TableManageItem;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能区-桌台增删改，不过应该不用了，改在后台管理
 */
public class TableManageActivity extends BaseActivity<HdcActivityTableManageBinding> {

    private final List<TableManageItem> mLokal1List = new ArrayList<>();
    private final List<TableManageItem> mLokal2List = new ArrayList<>();

    private TableManageAdapter mLokal1Adapter;
    private TableManageAdapter mLokal2Adapter;

    private int mCurrentTable = 1;

    @Override
    protected HdcActivityTableManageBinding getViewBinding() {
        return HdcActivityTableManageBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        initAllTable();
        initListener();
        updateCurrentTableZoom(1);
    }

    @Override
    protected void initData() {
        initTestData();
    }

    private void initListener() {
        mBinding.ivBack.setOnClickListener(v -> finish());
        mBinding.ivAdd.setOnClickListener(v -> {
            addTable();
        });
        mBinding.tvAdd.setOnClickListener(v -> {
            addTable();
        });
        mLokal1Adapter.setItemClickListener(item -> {

        });
        mLokal1Adapter.setItemLongClickListener(item -> {
            new DeleteTableDialog(this, new DeleteTableDialog.ClickListener() {

                @Override
                public void onClickOk() {
                    new DeleteTableUsedTipsDialog(TableManageActivity.this).show();
                }

                @Override
                public void onClickCancel() {

                }
            }).show();
            return true;
        });
        mLokal2Adapter.setItemClickListener(item -> {
            ToastUtils.show(item.getName());
        });
        mLokal2Adapter.setItemLongClickListener(item -> {
            new DeleteTableDialog(this, new DeleteTableDialog.ClickListener() {

                @Override
                public void onClickOk() {
                    new DeleteTableUsedTipsDialog(TableManageActivity.this).show();
                }

                @Override
                public void onClickCancel() {

                }
            }).show();
            return true;
        });
        mBinding.tvZoom1.setOnClickListener(v -> {
            updateCurrentTableZoom(1);
        });
        mBinding.tvZoom2.setOnClickListener(v -> {
            updateCurrentTableZoom(2);
        });
    }

    private void initAllTable() {
        mBinding.rvTable1.setLayoutManager(new GridLayoutManager(this, 3));
        mLokal1Adapter = new TableManageAdapter(mLokal1List);
        mBinding.rvTable1.setAdapter(mLokal1Adapter);
        mBinding.rvTable2.setLayoutManager(new GridLayoutManager(this, 3));
        mLokal2Adapter = new TableManageAdapter(mLokal2List);
        mBinding.rvTable2.setAdapter(mLokal2Adapter);
    }

    private void updateCurrentTableZoom(int index) {
        mCurrentTable = index;
        switch (index) {
            case 1:
                mBinding.rvTable1.setVisibility(android.view.View.VISIBLE);
                mBinding.rvTable2.setVisibility(android.view.View.GONE);
                mBinding.tvZoom1.setBackgroundColor(getResources().getColor(R.color.color_576BD5));
                mBinding.tvZoom2.setBackgroundColor(getResources().getColor(R.color.color_DCDFE0));
                //设置颜色
                mBinding.tvZoom1.setTextColor(getResources().getColor(R.color.white));
                mBinding.tvZoom2.setTextColor(getResources().getColor(R.color.color_646566));

                break;
            case 2:
                mBinding.rvTable1.setVisibility(android.view.View.GONE);
                mBinding.rvTable2.setVisibility(android.view.View.VISIBLE);
                mBinding.tvZoom1.setBackgroundColor(getResources().getColor(R.color.color_DCDFE0));
                mBinding.tvZoom2.setBackgroundColor(getResources().getColor(R.color.color_576BD5));
                //设置颜色
                mBinding.tvZoom1.setTextColor(getResources().getColor(R.color.color_646566));
                mBinding.tvZoom2.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    private void addTable() {
        new NewTableDialog(this).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initTestData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            for (int i = 1; i <= 20; i++) {
                TableManageItem item = new TableManageItem();
                item.setId(i);
                item.setName("T-" + i);
                mLokal1List.add(item);
            }

            for (int i = 1; i <= 9; i++) {
                TableManageItem item = new TableManageItem();
                item.setId(i);
                item.setName("T-" + i);
                mLokal2List.add(item);
            }

            runOnUiThread(() -> {
                mLokal1Adapter.notifyDataSetChanged();
                mLokal2Adapter.notifyDataSetChanged();
            });
        });


    }
}
