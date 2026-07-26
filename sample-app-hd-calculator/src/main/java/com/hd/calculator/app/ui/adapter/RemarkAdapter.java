package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.HdcItemRemarkBinding;
import com.hd.calculator.app.ui.item.RemarkItem;

import java.util.List;

public class RemarkAdapter extends RecyclerView.Adapter<RemarkAdapter.ViewHolder> {

    //list
    private final List<RemarkItem> data;
    private ItemClickListener<RemarkItem> itemClickListener;

    public RemarkAdapter(List<RemarkItem> data) {
        this.data = data;
    }

    public void setItemClickListener(ItemClickListener<RemarkItem> itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(HdcItemRemarkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RemarkItem item = data.get(position);
        HdcItemRemarkBinding binding = holder.getBinding();
        binding.tvRemark.setText(item.getRemark());
        if (item.isSelect()) {
            binding.ivSelect.setImageResource(R.drawable.hdc_ic_frame_checked);
        } else {
            binding.ivSelect.setImageResource(R.drawable.hdc_ic_frame_uncheck);
        }
        binding.getRoot().setOnClickListener(v -> {
            item.setSelect(!item.isSelect());
            notifyItemChanged(position, position);
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

        private final HdcItemRemarkBinding binding;

        public ViewHolder(HdcItemRemarkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public HdcItemRemarkBinding getBinding() {
            return binding;
        }
    }
}
