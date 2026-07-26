package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.ItemDailyReportBinding;
import com.hd.calculator.app.ui.item.DailyReportItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.List;

public class DailyReportAdapter extends RecyclerView.Adapter<DailyReportAdapter.ViewHolder> {


    //list data
    private final List<DailyReportItem> data;

    //itemclicklistener
    private ItemClickListener<DailyReportItem> itemClickListener;

    //constructor data
    public DailyReportAdapter(List<DailyReportItem> data) {
        this.data = data;
    }

    //setlistener
    public void setOnItemClickListener(ItemClickListener<DailyReportItem> listener) {
        itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemDailyReportBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyReportItem item = data.get(position);
        ItemDailyReportBinding binding = holder.getBinding();
        binding.tvTitle.setText(item.getTitle());
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getCost()));
        binding.tvTime.setText(TimeUtils.formatTimestampToHHmm(item.getTime()));

        holder.itemView.setOnClickListener(v -> {
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
        private final ItemDailyReportBinding binding;

        public ViewHolder(ItemDailyReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        //getbinding
        public ItemDailyReportBinding getBinding() {
            return binding;
        }
    }
}
