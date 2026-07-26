package com.hd.calculator.app.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.databinding.HdcItemOrderCancelBinding;
import com.hd.calculator.app.ui.item.DishesCancelItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.List;

public class DishesCancelAdapter extends RecyclerView.Adapter<DishesCancelAdapter.ViewHolder> {
    //list
    private final List<DishesCancelItem> data;

    public DishesCancelAdapter(List<DishesCancelItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemOrderCancelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishesCancelItem item = data.get(position);
        HdcItemOrderCancelBinding binding = holder.getBinding();
        binding.tvTitle.setText(item.getDishesName());
        //cost
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getCost()));
        // table
        if (item.getOrderType() == OrderType.ORDER_TYPE_IN_HOUSE) {
            binding.tvTable.setText("Tisch " + item.getTableCode());
        }
        //waiter
        binding.tvWaiter.setText(item.getWaiterName() + " (" + TimeUtils.formatTimestampToHHmm(item.getTime()) + ")");

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final HdcItemOrderCancelBinding binding;

        public ViewHolder(HdcItemOrderCancelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HdcItemOrderCancelBinding getBinding() {
            return binding;
        }
    }
}
