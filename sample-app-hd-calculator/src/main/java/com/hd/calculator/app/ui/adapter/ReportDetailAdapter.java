package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.ItemReportDetailBinding;
import com.hd.calculator.app.ui.item.ReportDetailItem;

import java.util.List;

public class ReportDetailAdapter extends RecyclerView.Adapter<ReportDetailAdapter.ViewHolder> {

    //list
    private final List<ReportDetailItem> data;

    public ReportDetailAdapter(List<ReportDetailItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ReportDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemReportDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportDetailItem item = data.get(position);
        ItemReportDetailBinding binding = holder.getBinding();
        binding.tvTitle.setText(item.getTitle());
        binding.tvDesc.setText(item.getDesc());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemReportDetailBinding binding;


        public ViewHolder(ItemReportDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemReportDetailBinding getBinding() {
            return binding;
        }

    }
}
