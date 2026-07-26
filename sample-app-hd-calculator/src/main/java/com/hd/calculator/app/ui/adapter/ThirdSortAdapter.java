package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.HdcItemDishesThirdSortBinding;
import com.hd.calculator.app.ui.item.ThirdSortItem;

import java.util.List;

public class ThirdSortAdapter extends RecyclerView.Adapter<ThirdSortAdapter.ViewHolder> {

    private final List<ThirdSortItem> data;

    private ItemClickListener<ThirdSortItem> itemClickListener;

    public ThirdSortAdapter(List<ThirdSortItem> data) {
        this.data = data;
    }

    public void setItemClickListener(ItemClickListener<ThirdSortItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemDishesThirdSortBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThirdSortItem item = data.get(position);
        HdcItemDishesThirdSortBinding binding = holder.getBinding();
        binding.tvTitle.setText(item.getName());
        if (item.isSelect()) {
            //indicator
            binding.indicator.setVisibility(View.VISIBLE);
            binding.getRoot().setBackgroundColor(MyApp.context.getResources().getColor(R.color.color_F5F7FA));
            binding.tvTitle.setTextColor(MyApp.context.getResources().getColor(R.color.color_414242));
        } else {
            binding.indicator.setVisibility(View.GONE);
            binding.getRoot().setBackgroundColor(MyApp.context.getResources().getColor(R.color.white));
            binding.tvTitle.setTextColor(MyApp.context.getResources().getColor(R.color.color_646566));
        }

        binding.getRoot().setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final HdcItemDishesThirdSortBinding binding;

        public ViewHolder(HdcItemDishesThirdSortBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HdcItemDishesThirdSortBinding getBinding() {
            return binding;
        }

    }
}
