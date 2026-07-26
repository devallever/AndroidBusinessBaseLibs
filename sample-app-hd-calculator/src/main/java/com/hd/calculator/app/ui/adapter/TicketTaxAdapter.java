package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.databinding.HdcItemTicketTaxBinding;
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
        return new ViewHolder(HdcItemTicketTaxBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TicketTaxItem item = data.get(position);
        HdcItemTicketTaxBinding binding = holder.getBinding();
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
        public final HdcItemTicketTaxBinding binding;
        public ViewHolder(com.hd.calculator.app.databinding.HdcItemTicketTaxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        //getBinding
        public HdcItemTicketTaxBinding getBinding() {
            return binding;
        }
    }

}
