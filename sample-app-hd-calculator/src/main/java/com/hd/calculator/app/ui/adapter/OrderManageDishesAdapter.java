package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.HdcItemOrderManageDishesBinding;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.MoneyUtils;

import java.util.List;

public class OrderManageDishesAdapter extends RecyclerView.Adapter<OrderManageDishesAdapter.ViewHolder> {

    //list
    private final List<DishesItem> data;

    public OrderManageDishesAdapter(List<DishesItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemOrderManageDishesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishesItem item = data.get(position);
        HdcItemOrderManageDishesBinding binding = holder.getBinding();
        binding.tvCount.setText(item.getCount() + "x");
        binding.tvTitle.setText(item.getCode() + " " + item.getName());
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getPrice() * item.getCount()));
        if (item.getRemark() == null || item.getRemark().isEmpty()) {
            binding.tvRemark.setVisibility(View.GONE);
        } else {
            binding.tvRemark.setVisibility(View.VISIBLE);
            binding.tvRemark.setText(item.getRemark());
        }
//        if (position == data.size() - 1) {
//            binding.line.setVisibility(View.GONE);
//        } else {
//            binding.line.setVisibility(View.VISIBLE);
//        }

        binding.ivAdd.setVisibility(View.GONE);
//        binding.ivAdd.setOnClickListener(v -> {
//            ToastUtils.show("+1");
//        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public HdcItemOrderManageDishesBinding binding;

        public ViewHolder(HdcItemOrderManageDishesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HdcItemOrderManageDishesBinding getBinding() {
            return binding;
        }
    }
}
