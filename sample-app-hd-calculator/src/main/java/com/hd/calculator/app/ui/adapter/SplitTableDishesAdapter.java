package com.hd.calculator.app.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.HdcItemSplitDishesBinding;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.MoneyUtils;

import java.util.List;

public class SplitTableDishesAdapter extends RecyclerView.Adapter<SplitTableDishesAdapter.ViewHolder> {

    //data
    private final List<DishesItem> data;

    private OptionClickListener optionClickListener;

    private ItemClickListener<DishesItem> itemClickListener;

    //set
    public void setItemClickListener(ItemClickListener<DishesItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public SplitTableDishesAdapter(List<DishesItem> data) {
        this.data = data;
    }

    //setOptionClickListener
    public void setOptionClickListener(OptionClickListener optionClickListener) {
        this.optionClickListener = optionClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemSplitDishesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishesItem item = data.get(position);
        HdcItemSplitDishesBinding binding = holder.getBinding();
        binding.tvCount.setText(item.getCount() + "x");
        binding.tvTitle.setText(item.getName());
        //价格
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getPrice()));
        binding.tvCode.setText(item.getCode());

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

    public interface OptionClickListener {
        void onClickCount(DishesItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final HdcItemSplitDishesBinding binding;

        public ViewHolder(HdcItemSplitDishesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HdcItemSplitDishesBinding getBinding() {
            return binding;
        }
    }
}
