package com.hd.calculator.app.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.ItemTicketDishesBinding;
import com.hd.calculator.app.ui.item.TicketDishesItem;
import com.hd.calculator.app.util.MoneyUtils;

import java.util.List;

public class TicketDishesAdapter extends RecyclerView.Adapter<TicketDishesAdapter.ViewHolder>{

    //data
    private List<TicketDishesItem> data;

    public TicketDishesAdapter(List<TicketDishesItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTicketDishesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketDishesItem item = data.get(position);
        ItemTicketDishesBinding binding = holder.getBinding();
        binding.tvCost.setText(MoneyUtils.formatMoney(item.getCost()));
        binding.tvTitle.setText(item.getName());
        binding.tvTaxSign.setText(item.getTaxSign());
        binding.tvCount.setText(item.getCount() + "x");
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //binding
        public ItemTicketDishesBinding binding;
        public ViewHolder(ItemTicketDishesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemTicketDishesBinding getBinding() {
            return binding;
        }
    }

}
