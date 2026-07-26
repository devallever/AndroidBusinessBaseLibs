package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.R;
import com.hd.calculator.app.databinding.ItemOrderPayTypeBinding;
import com.hd.calculator.app.ui.item.BillPayTypeItem;
import com.hd.calculator.app.util.MoneyUtils;

import java.util.List;

public class BillPayTypeAdapter extends RecyclerView.Adapter<BillPayTypeAdapter.ViewHolder> {

    private final List<BillPayTypeItem> data;

    private ItemClickListener<BillPayTypeItem> itemClickListener;

    public BillPayTypeAdapter(List<BillPayTypeItem> data) {
        this.data = data;
    }

    public void setItemClickListener(ItemClickListener<BillPayTypeItem> listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOrderPayTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BillPayTypeItem item = data.get(position);
        ItemOrderPayTypeBinding binding = holder.getBinding();

        //set title 格式：Bargeld (1x) (46,30 €)
        binding.tvTitle.setText(item.getPayTypeName() + " (" + item.getCount() + "x) (" + MoneyUtils.formatMoney(item.getCost()) + ")");
        //select
        if (item.isSelect()) {
            binding.ivSelect.setVisibility(View.VISIBLE);
            binding.tvTitle.setTextColor(ContextCompat.getColor(MyApp.context, R.color.white));
            binding.getRoot().setBackgroundResource(R.drawable.shape_blue_r45);
        } else {
            binding.ivSelect.setVisibility(View.GONE);
            binding.tvTitle.setTextColor(ContextCompat.getColor(MyApp.context, R.color.color_646566));
            binding.getRoot().setBackgroundResource(R.drawable.shape_gray_646566_r45);
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
        public ItemOrderPayTypeBinding binding;

        public ViewHolder(ItemOrderPayTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public ItemOrderPayTypeBinding getBinding() {
            return binding;
        }

    }
}
