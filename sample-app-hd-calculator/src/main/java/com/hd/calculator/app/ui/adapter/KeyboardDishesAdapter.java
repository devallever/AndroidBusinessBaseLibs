package com.hd.calculator.app.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.ItemChooseDishesBinding;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.MoneyUtils;

import java.util.List;

public class KeyboardDishesAdapter extends RecyclerView.Adapter<KeyboardDishesAdapter.ViewHolder> {

    private final List<DishesItem> data;
    //optionClick
    private OptionClickListener mOptionClickListener;

    public KeyboardDishesAdapter(List<DishesItem> data) {
        this.data = data;
    }

    public void setOptionClickListener(OptionClickListener mOptionClickListener) {
        this.mOptionClickListener = mOptionClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemChooseDishesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishesItem item = data.get(position);
        ItemChooseDishesBinding binding = holder.getBinding();
        binding.tvTitle.setText(item.getCode() + " " + item.getName());
        //此处是价格
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getPrice()));

        //设置count背景
        if (item.isOrdered()) {
            binding.tvCount.setBackgroundResource(R.drawable.shape_gray);
            binding.tvCount.setText(item.getCount() + "x");
            binding.ivDel.setVisibility(View.GONE);
        } else {
            binding.tvCount.setBackgroundResource(R.drawable.shape_green);
            binding.tvCount.setText("+" + item.getCount());
            binding.ivDel.setVisibility(View.VISIBLE);
        }

        if (item.getRemark() == null || item.getRemark().isEmpty()) {
            binding.tvRemark.setVisibility(View.GONE);
        } else {
            binding.tvRemark.setVisibility(View.VISIBLE);
            binding.tvRemark.setText(item.getRemark());
        }
        if (position == data.size() - 1) {
            binding.line.setVisibility(View.GONE);
        } else {
            binding.line.setVisibility(View.VISIBLE);
        }


        binding.ivAdd.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onClickAdd(position, item);
            }
        });

        binding.getRoot().setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onClickItem(position, item);
            }
        });

        binding.ivDel.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onClickDel(position, item);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void onUpdateCount(ItemChooseDishesBinding binding, DishesItem item) {
        binding.tvCount.setText("+" + item.getCount());
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getPrice() * item.getCount()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OptionClickListener {
        void onClickItem(int position, DishesItem item);

        void onClickDel(int position, DishesItem item);

        void onClickAdd(int position, DishesItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemChooseDishesBinding mBinding;

        public ViewHolder(ItemChooseDishesBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public ItemChooseDishesBinding getBinding() {
            return mBinding;
        }
    }
}
