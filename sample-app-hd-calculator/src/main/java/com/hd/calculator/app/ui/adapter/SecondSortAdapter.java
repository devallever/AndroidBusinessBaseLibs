package com.hd.calculator.app.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.HdcItemDishesSecondSortBinding;
import com.hd.calculator.app.ui.item.SecondSortItem;

import java.util.List;

public class SecondSortAdapter extends RecyclerView.Adapter<SecondSortAdapter.ViewHolder> {

    private final List<SecondSortItem> data;
    private ItemClickListener<SecondSortItem> itemClickListener;

    public SecondSortAdapter(List<SecondSortItem> data) {
        this.data = data;
    }

    public void setItemClickListener(ItemClickListener<SecondSortItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemDishesSecondSortBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SecondSortItem item = data.get(position);
        HdcItemDishesSecondSortBinding binding = holder.getBinding();
        binding.tvTitle.setText(item.getName());
        if (item.isSelect()) {
            binding.tvTitle.setTextColor(MyApp.context.getResources().getColor(R.color.color_485ABE));
            binding.tvTitle.setTypeface(null, Typeface.BOLD);
        } else {
            binding.tvTitle.setTextColor(MyApp.context.getResources().getColor(R.color.color_969799));
            binding.tvTitle.setTypeface(null, Typeface.NORMAL);
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

        private final HdcItemDishesSecondSortBinding binding;

        public ViewHolder(HdcItemDishesSecondSortBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HdcItemDishesSecondSortBinding getBinding() {
            return binding;
        }
    }
}
