package com.hd.calculator.app.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.databinding.ItemMainTableUnpaidBinding;
import com.hd.calculator.app.ui.item.MainUnpaidTableItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.List;

public class MainTableUnpaidAdapter extends RecyclerView.Adapter<MainTableUnpaidAdapter.ViewHolder> {

    private List<MainUnpaidTableItem> data = null;
    //itemClickListener
    private ItemClickListener<MainUnpaidTableItem> itemClickListener;

    public MainTableUnpaidAdapter(List<MainUnpaidTableItem> data) {
        this.data = data;
    }

    //set
    public void setItemClickListener(ItemClickListener<MainUnpaidTableItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMainTableUnpaidBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainUnpaidTableItem item = data.get(position);
        ItemMainTableUnpaidBinding binding = holder.getBinding();
        binding.tvTimeAndWaiter.setText(item.getWaiter() + " (" + TimeUtils.formatTimestampToHHmm(item.getTime()) + ")");
        //id
        binding.tvId.setText(TableManager.getIns().getDisplayTableName(item.getTableCode(), item.getOrderType()));
        //count
//        binding.tvBuffetCount.setText(item.getBuffetCountDisplay());
        binding.tvBuffetCount.setText(item.getAdultCount() + "E/" + item.getChildCount() + "K/" + item.getBabyCount() + "B");
        // cost
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getCost()));

        if (itemClickListener != null) {
            binding.getRoot().setOnClickListener(v -> {
                itemClickListener.onItemClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMainTableUnpaidBinding binding;

        public ViewHolder(ItemMainTableUnpaidBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemMainTableUnpaidBinding getBinding() {
            return binding;
        }
    }
}
