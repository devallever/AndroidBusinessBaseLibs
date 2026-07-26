package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.ItemTableManageBinding;
import com.hd.calculator.app.ui.item.TableManageItem;

import java.util.List;

public class TableManageAdapter extends RecyclerView.Adapter<TableManageAdapter.ViewHolder> {


    //list
    private final List<TableManageItem> data;

    //itemClickListener
    private ItemClickListener<TableManageItem> itemClickListener;

    //longclicklistener
    private ItemLongClickListener<TableManageItem> itemLongClickListener;

    public TableManageAdapter(List<TableManageItem> data) {
        this.data = data;
    }

    public void setItemClickListener(ItemClickListener<TableManageItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(ItemLongClickListener<TableManageItem> itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    @NonNull
    @Override
    public TableManageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTableManageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableManageItem item = data.get(position);
        ItemTableManageBinding binding = holder.getBinding();
        binding.tvName.setText(item.getName());

        if (itemClickListener != null) {
            binding.getRoot().setOnClickListener(v -> itemClickListener.onItemClick(item));
        }

        if (itemLongClickListener != null) {
            binding.getRoot().setOnLongClickListener(v -> itemLongClickListener.onItemLongClick(item));
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTableManageBinding binding;

        public ViewHolder(ItemTableManageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemTableManageBinding getBinding() {
            return binding;
        }

    }

}
