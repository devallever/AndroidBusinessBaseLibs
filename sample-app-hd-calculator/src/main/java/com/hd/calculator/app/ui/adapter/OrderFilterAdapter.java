package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.HdcItemOrderFilterBinding;
import com.hd.calculator.app.ui.item.BillFilterItem;

import java.util.List;

public class OrderFilterAdapter extends RecyclerView.Adapter<OrderFilterAdapter.ViewHolder> {

    private final List<BillFilterItem> data;

    //itemclicklistener
    private ItemClickListener<BillFilterItem> itemClickListener;

    public OrderFilterAdapter(List<BillFilterItem> data) {
        this.data = data;
    }

    //SETLISTENER
    public void setItemClickListener(ItemClickListener<BillFilterItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemOrderFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillFilterItem item = data.get(position);
        HdcItemOrderFilterBinding binding = holder.getBinding();
        //set title
        binding.tvTitle.setText(item.getName());
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
        private final HdcItemOrderFilterBinding binding;

        public ViewHolder(HdcItemOrderFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        //getbinding
        public HdcItemOrderFilterBinding getBinding() {
            return binding;
        }


    }
}
