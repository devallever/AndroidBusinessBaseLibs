package com.hd.calculator.app.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.ItemMainTableBinding;
import com.hd.calculator.app.ui.item.MainTableItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.List;

public class MainTableAdapter extends RecyclerView.Adapter<MainTableAdapter.ViewHolder> {

    private final List<MainTableItem> data;
    private ItemClickListener<MainTableItem> mItemClickListener = null;
    private ItemLongClickListener<MainTableItem> mItemLongClickListener = null;

    public MainTableAdapter(@NonNull List<MainTableItem> data) {
        this.data = data;
    }

    //set
    public void setItemClickListener(ItemClickListener<MainTableItem> itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(ItemLongClickListener<MainTableItem> itemLongClickListener) {
        this.mItemLongClickListener = itemLongClickListener;
    }

    @NonNull
    @Override
    public MainTableAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMainTableBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MainTableAdapter.ViewHolder holder, int position) {
        // 防止索引越界，添加边界检查
        if (position < 0 || position >= data.size()) {
            return;
        }
        
        MainTableItem item = data.get(position);
        ItemMainTableBinding binding = holder.getBinding();
        // 显示时间和菜品数量 22:00 (20)
        binding.tvTimeAndCount.setText(TimeUtils.formatTimestampToHHmm(item.getTime()) + " (" + item.getDishesCount() + ")");
        //显示id
        binding.tvId.setText("T-" + item.getTableCode());
        binding.tvIdUnUsed.setText("T-" + item.getTableCode());
        //显示数量
//        binding.tvBuffetCount.setText(String.valueOf(item.getBuffetCount()));
        //显示价格
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getCost()));
        //EBK
        binding.tvBuffetCount.setText(item.getAdultCount() + "E/" + item.getChildCount() + "K/" + item.getBabyCount() + "B");

        if (item.isUsed()) {
            binding.usedContainer.setVisibility(View.VISIBLE);
            binding.unUsedContainer.setVisibility(View.GONE);
        } else {
            binding.usedContainer.setVisibility(View.GONE);
            binding.unUsedContainer.setVisibility(View.VISIBLE);
        }

        if (mItemClickListener != null) {
            binding.getRoot().setOnClickListener(v -> mItemClickListener.onItemClick(item));
        }

        if (mItemLongClickListener != null) {
            binding.getRoot().setOnLongClickListener(v -> mItemLongClickListener.onItemLongClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMainTableBinding binding;

        public ViewHolder(ItemMainTableBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemMainTableBinding getBinding() {
            return binding;
        }
    }
}
