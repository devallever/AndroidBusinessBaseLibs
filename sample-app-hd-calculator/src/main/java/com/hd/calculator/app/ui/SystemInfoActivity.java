package com.hd.calculator.app.ui;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.databinding.HdcActivitySystemInfoBinding;
import com.hd.calculator.app.ui.adapter.SystemInfoAdapter;
import com.hd.calculator.app.ui.item.SystemInfoItem;

import java.util.ArrayList;
import java.util.List;

/***
 *
 */
public class SystemInfoActivity extends BaseActivity<HdcActivitySystemInfoBinding> {

    //list
    private final List<SystemInfoItem> mList = new ArrayList<>();
    //adapter
    private SystemInfoAdapter mAdapter;

    @Override
    protected HdcActivitySystemInfoBinding getViewBinding() {
        return HdcActivitySystemInfoBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        mBinding.includeTopBar.tvTitle.setText(getString(R.string.hdc_menu_service));
        initReportDetail();
    }

    @Override
    protected void initData() {
        initReportData();
    }

    private void initReportDetail() {
        //init rv
        mAdapter = new SystemInfoAdapter(mList);
        mBinding.rvInfo.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvInfo.setAdapter(mAdapter);
    }

    private void initReportData() {
        mList.add(new SystemInfoItem("Name wide", "-"));
        mList.add(new SystemInfoItem("Name narrow", "Restaurant Asiamo"));
        mList.add(new SystemInfoItem("Owner", "Asiamo Gastronomie WaiblingenGmbH"));
        mList.add(new SystemInfoItem("Street", "Stuttgarter Straße 147"));
        mList.add(new SystemInfoItem("Zip", "71332"));
        mList.add(new SystemInfoItem("City", "Waiblingen"));
        mList.add(new SystemInfoItem("Phone", "Tel.07151 79114988"));
        mList.add(new SystemInfoItem("Fax", "-"));
        mList.add(new SystemInfoItem("Website", "-"));
        mList.add(new SystemInfoItem("Taxcode", "90491/08214"));

        mAdapter.notifyDataSetChanged();
    }
}
