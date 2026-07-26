package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.ItemSelectorDishesBinding;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.MoneyUtils;

import java.util.List;

public class SelectorDishesAdapter extends RecyclerView.Adapter<SelectorDishesAdapter.ViewHolder> {

    //list data
    private final List<DishesItem> data;

    //listener
    private OptionClickListener optionClickListener;

    public SelectorDishesAdapter(List<DishesItem> data) {
        this.data = data;
    }

    //setListener
    public void setOptionClickListener(OptionClickListener optionClickListener) {
        this.optionClickListener = optionClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSelectorDishesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishesItem item = data.get(position);
        ItemSelectorDishesBinding binding = holder.getBinding();
        binding.tvCount.setText("+" + item.getCount());
        binding.tvTitle.setText(item.getName());
        //此处是价格
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getPrice()));
        //code
        binding.tvCode.setText(item.getCode());
        onCountUpdate(binding, item);

        binding.getRoot().setOnClickListener(v -> {
            int count = item.getCount() + 1;
            item.setCount(count);
            onCountUpdate(binding, item);
            if (optionClickListener != null) {
                optionClickListener.onCountChanged(item);
            }
        });

        binding.ivDel.setOnClickListener(v -> {
            int count = item.getCount() - 1;
            if (count < 0) {
                count = 0;
            }
            item.setCount(count);
            onCountUpdate(binding, item);
            if (optionClickListener != null) {
                optionClickListener.onCountChanged(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void onCountUpdate(ItemSelectorDishesBinding binding, DishesItem item) {
        binding.tvCount.setText("+" + item.getCount());
//        binding.tvCost.setText(MoneyUtils.formatMoney(item.getPrice() * item.getCount()));
        if (item.getCount() == 0) {
            binding.tvCount.setBackgroundResource(R.drawable.shape_gray);
            binding.tvCost.setTextColor(MyApp.context.getResources().getColor(R.color.color_969799));
        } else {
            binding.tvCount.setBackgroundResource(R.drawable.shape_green);
            binding.tvCost.setTextColor(MyApp.context.getResources().getColor(R.color.color_3848A8));
        }
    }

    public interface OptionClickListener {
        void onCountChanged(DishesItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemSelectorDishesBinding binding;

        public ViewHolder(ItemSelectorDishesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemSelectorDishesBinding getBinding() {
            return binding;
        }

    }
}
