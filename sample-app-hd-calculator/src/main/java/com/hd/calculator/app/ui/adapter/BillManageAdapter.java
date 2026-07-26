package com.hd.calculator.app.ui.adapter;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.databinding.ItemOrderManageBinding;
import com.hd.calculator.app.ui.item.BillManageItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.List;

public class BillManageAdapter extends AbsAdapter<BillManageItem, ItemOrderManageBinding> {

    public BillManageAdapter(List<BillManageItem> data) {
        super(data);
    }

    @Override
    public ItemOrderManageBinding getBinding(ViewGroup parent) {
        return ItemOrderManageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindingViewHolder(BillManageItem item, ItemOrderManageBinding binding, int position) {
        if (item.getOrderType() == OrderType.ORDER_TYPE_IN_HOUSE) {
            binding.tvTitle.setText("RE-" + item.getBillCode() + "(Tisch" + item.getTableCode() + ")");
        } else {
            binding.tvTitle.setText("RE-" + item.getBillCode() + "(T-A" + item.getTableCode() + ")");
        }
        //waiter 格式：Kellner-B (23:39)
        binding.tvWaiter.setText(item.getWaiterName() + " (" + TimeUtils.formatTimestampToDDMMYYYYHHmm(item.getOrderTime()) + ")");
        //cost
        if (item.isFromDeleteTable() || item.isCanceled()) {
            //tvCost 加删除线
            binding.tvCost.setPaintFlags(binding.tvCost.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvTitle.setPaintFlags(binding.tvCost.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            //tvCost 去掉删除线
            binding.tvCost.setPaintFlags(binding.tvCost.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            binding.tvTitle.setPaintFlags(binding.tvCost.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (item.isCanceled()) {
            //tvCost setTextColor
            binding.tvCost.setTextColor(MyApp.context.getColor(R.color.color_E12B2B));
            binding.tvCost.setText(MoneyUtils.formatMoney(0));
        } else {
            binding.tvCost.setTextColor(MyApp.context.getColor(R.color.color_3848A8));
            binding.tvCost.setText(MoneyUtils.formatMoney(item.getCost()));
        }
        if (item.getCost() == item.getPayTotal() || item.isFromDeleteTable()) {
            //tvCostContainTips GONE
            binding.tvCostContainTips.setVisibility(View.GONE);
        } else {
            //tvCostContainTips VISIBLE
            binding.tvCostContainTips.setVisibility(View.VISIBLE);
        }
        binding.tvCostContainTips.setText("Summe inkl. TK  " + MoneyUtils.formatMoney(item.getPayTotal()));

        if (item.isExpend() && !item.getDishesList().isEmpty()) {
            //init rv
            OrderManageDishesAdapter dishesAdapter = new OrderManageDishesAdapter(item.getDishesList());
            binding.rvDishes.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
            binding.rvDishes.setAdapter(dishesAdapter);
            binding.rvDishes.setVisibility(View.VISIBLE);
        } else {
            //gone
            binding.rvDishes.setVisibility(View.GONE);
        }
    }
}
