package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.ItemSystemInfoBinding;
import com.hd.calculator.app.ui.item.SystemInfoItem;

import java.util.List;

public class SystemInfoAdapter extends RecyclerView.Adapter<SystemInfoAdapter.ViewHolder> {

    //list
    private final List<SystemInfoItem> data;

    public SystemInfoAdapter(List<SystemInfoItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public SystemInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemSystemInfoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SystemInfoItem item = data.get(position);
        ItemSystemInfoBinding binding = holder.getBinding();
        binding.tvTitle.setText(item.getTitle());
        binding.tvDesc.setText(item.getDesc());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSystemInfoBinding binding;


        public ViewHolder(ItemSystemInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemSystemInfoBinding getBinding() {
            return binding;
        }

    }
}
