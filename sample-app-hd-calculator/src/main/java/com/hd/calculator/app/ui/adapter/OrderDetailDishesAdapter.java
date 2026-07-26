package com.hd.calculator.app.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.HdcItemOrderDetailDishesBinding;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.MoneyUtils;

import java.util.List;

public class OrderDetailDishesAdapter extends RecyclerView.Adapter<OrderDetailDishesAdapter.ViewHolder> {

    //list
    private final List<DishesItem> data;

    //listener
    private ItemClickListener<DishesItem> itemClickListener;

    //Optionlistener
    private OptionClickListener optionClickListener;

    public OrderDetailDishesAdapter(List<DishesItem> data) {
        this.data = data;
    }

    //set option listener
    public void setOptionClickListener(OptionClickListener optionClickListener) {
        this.optionClickListener = optionClickListener;
    }

    public void setItemClickListener(ItemClickListener<DishesItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemOrderDetailDishesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DishesItem item = data.get(position);
        HdcItemOrderDetailDishesBinding binding = holder.getBinding();
        if (item.isOrdered()) {
            binding.tvCount.setText(item.getCount() + "x");
            binding.tvCount.setBackgroundResource(R.drawable.hdc_shape_gray);
            binding.tvCount.setTextColor(MyApp.context.getColor(R.color.color_282929));
            binding.ivDel.setVisibility(View.GONE);
            if (item.isCanceled()) {
                binding.tvCount.setBackgroundResource(R.drawable.hdc_shape_red);
                //binding.tvCost 删除线
                binding.tvCost.setPaintFlags(binding.tvCost.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.ivAdd.setVisibility(View.GONE);
                binding.tvCount.setTextColor(MyApp.context.getColor(R.color.white));
            } else {
                binding.tvCount.setBackgroundResource(R.drawable.hdc_shape_gray);
                binding.tvCost.setPaintFlags(binding.tvCost.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                binding.ivAdd.setVisibility(View.VISIBLE);
                binding.tvCount.setTextColor(MyApp.context.getColor(R.color.color_282929));
            }
        } else {
            binding.tvCount.setText("+" + item.getCount());
            binding.tvCount.setBackgroundResource(R.drawable.hdc_shape_green);
            binding.tvCount.setTextColor(MyApp.context.getColor(R.color.white));
            binding.ivDel.setVisibility(View.VISIBLE);
        }
        binding.tvTitle.setText(item.getCode() + " " + item.getName());

        //这里显示价格
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getPrice()));
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
            if (optionClickListener != null) {
                optionClickListener.onClickAdd(item);
            }
        });

        binding.ivDel.setOnClickListener(v -> {
            if (optionClickListener != null) {
                optionClickListener.onClickDel(item);
            }
        });

        binding.tvCount.setOnClickListener(v -> {
            if (optionClickListener != null) {
                optionClickListener.onClickCount(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OptionClickListener {
        void onClickCount(DishesItem data);

        default void onClickAdd(DishesItem data) {
        }

        default void onClickDel(DishesItem data) {
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public HdcItemOrderDetailDishesBinding binding;

        public ViewHolder(HdcItemOrderDetailDishesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HdcItemOrderDetailDishesBinding getBinding() {
            return binding;
        }
    }
}
