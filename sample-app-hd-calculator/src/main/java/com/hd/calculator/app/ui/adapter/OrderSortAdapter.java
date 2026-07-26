package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.HdcItemOrderSortBinding;
import com.hd.calculator.app.ui.item.BillSortItem;

import java.util.List;

public class OrderSortAdapter extends RecyclerView.Adapter<OrderSortAdapter.ViewHolder> {

    private final List<BillSortItem> data;

    //itemclicklistener
    private ItemClickListener<BillSortItem> itemClickListener;

    public OrderSortAdapter(List<BillSortItem> data) {
        this.data = data;
    }

    //SETLISTENER
    public void setItemClickListener(ItemClickListener<BillSortItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemOrderSortBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillSortItem item = data.get(position);
        HdcItemOrderSortBinding binding = holder.getBinding();
        //set title
        binding.tvTitle.setText(item.getSortName());
        if (item.isSelect()) {
            binding.ivSelect.setImageResource(R.drawable.hdc_ic_check_2_checked);
        } else {
            binding.ivSelect.setImageResource(R.drawable.hdc_ic_check_2_uncheck);
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
        //binding
        private final HdcItemOrderSortBinding binding;

        public ViewHolder(HdcItemOrderSortBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        //getbinding
        public HdcItemOrderSortBinding getBinding() {
            return binding;
        }


    }
}
