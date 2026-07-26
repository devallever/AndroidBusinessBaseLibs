package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hd.calculator.app.databinding.HdcItemUserBinding;
import com.hd.calculator.app.ui.item.UserItem;

import java.util.List;

public class UserAdapter extends AbsAdapter<UserItem, HdcItemUserBinding>{
    public UserAdapter(List<UserItem> data) {
        super(data);
    }

    @Override
    public HdcItemUserBinding getBinding(ViewGroup parent) {
        return HdcItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    public void onBindingViewHolder(UserItem item, HdcItemUserBinding binding, int position) {
        binding.tvUser.setText(item.getUserName());
        if (item.isSelected()) {
            binding.ivSelect.setVisibility(View.VISIBLE);
        } else {
            binding.ivSelect.setVisibility(View.GONE);
        }
    }
}
