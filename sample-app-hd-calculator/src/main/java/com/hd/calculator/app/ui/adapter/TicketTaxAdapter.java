package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.ItemTicketTaxBinding;
import com.hd.calculator.app.ui.item.TicketTaxItem;

import java.util.List;

public class TicketTaxAdapter extends RecyclerView.Adapter<TicketTaxAdapter.ViewHolder>{

    //data
    private List<TicketTaxItem> data;

    public TicketTaxAdapter(List<TicketTaxItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTicketTaxBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketTaxItem item = data.get(position);
        ItemTicketTaxBinding binding = holder.getBinding();
        binding.tv1.setText(item.getColumn1());
        binding.tv2.setText(item.getColumn2());
        binding.tv3.setText(item.getColumn3());
        binding.tv4.setText(item.getColumn4());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //binding
        public final ItemTicketTaxBinding binding;
        public ViewHolder(com.hd.calculator.app.databinding.ItemTicketTaxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        //getBinding
        public ItemTicketTaxBinding getBinding() {
            return binding;
        }
    }

}
