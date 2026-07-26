package com.hd.calculator.app.ui;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.constant.PayType;
import com.hd.calculator.app.databinding.ActivityReportDetailBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillWithDishesRef;
import com.hd.calculator.app.ui.adapter.ReportDetailAdapter;
import com.hd.calculator.app.ui.item.ReportDetailItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 跑堂日报表
 */
public class ReportWaiterActivity extends BaseActivity<ActivityReportDetailBinding> {

    //list
    private final List<ReportDetailItem> mList = new ArrayList<>();
    //adapter
    private ReportDetailAdapter mAdapter;

    @Override
    protected ActivityReportDetailBinding getViewBinding() {
        return ActivityReportDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        mBinding.includeTopBar.tvTitle.setText(getString(R.string.daily_report));
        initReportDetail();
    }

    @Override
    protected void initData() {
        initReportData();
    }

    private void initReportDetail() {
        //init rv
        mAdapter = new ReportDetailAdapter(mList);
        mBinding.rvReport.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvReport.setAdapter(mAdapter);
    }

    private void initReportData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            long startTime = TimeUtils.getAdjustedTimestamp(System.currentTimeMillis());
            List<BillWithDishesRef> billList = DataBaseRepository.getInstance().getTodayBillByUserId(AccountManager.getIns().getAccount().getUserId());
            float payTotal = 0;
            int cardBillCount = 0;
            int cashBillCount = 0;
            float cardPayTotal = 0;
            float cashPayTotal = 0;
            float cardTipsTotal = 0;
            float cashTipsTotal = 0;
            for (BillWithDishesRef billRef : billList) {
                BillRecordEntity bill = billRef.getBill();
                List<BillDishesRecordEntity> dishesList = billRef.getDishesList();
                payTotal = payTotal + bill.getPayTotal();
                if (!bill.isCanceled()) {
                    if (bill.getPayType() == PayType.PAY_TYPE_CARD) {
                        cardBillCount++;
                        cardPayTotal = cardPayTotal + bill.getPayTotal();
                        cardTipsTotal = cardTipsTotal + bill.getTipsTotal();
                    } else {
                        cashBillCount++;
                        cashPayTotal = cashPayTotal + bill.getPayTotal();
                        cashTipsTotal = cashTipsTotal + bill.getTipsTotal();
                    }
                }
            }


            //收银总额: 点餐菜牌金额+小费金额
            mList.add(new ReportDetailItem(getString(R.string.report_label_total_income), MoneyUtils.formatMoney(payTotal)));

            //刷卡单数:没有被取消或者恢复台的 刷卡买单的账单数
            mList.add(new ReportDetailItem(getString(R.string.report_label_total_card_order_count), cardBillCount+""));
            //刷卡买单总额:没有被取消或者恢复台的 刷卡买单的点餐菜牌金额+小费金额
            mList.add(new ReportDetailItem(getString(R.string.report_label_total_card_order_amount), MoneyUtils.formatMoney(cardPayTotal)));
            //刷卡小费总额:没有被取消或者恢复台的 刷卡买单的
            mList.add(new ReportDetailItem(getString(R.string.report_label_total_card_order_tips), MoneyUtils.formatMoney(cardTipsTotal)));
            //现金单数:没有被取消或者恢复台的 现金买单的帐单数
            mList.add(new ReportDetailItem(getString(R.string.report_label_total_cash_order_count), cashBillCount + ""));
            //现金买单总额:没有被取消或者恢复台的 现金买单的点餐金额+小费金额
            mList.add(new ReportDetailItem(getString(R.string.report_label_total_cash_order_amount), MoneyUtils.formatMoney(cashPayTotal)));

            float value = cashPayTotal - cardTipsTotal;
            //需交现金数(=现金买单总额-刷卡小费总额
            mList.add(new ReportDetailItem(getString(R.string.report_label_need_cash_amount), MoneyUtils.formatMoney(value)));

            runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
            });
        });
    }
}
