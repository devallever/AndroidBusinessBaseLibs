package com.hd.calculator.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hd.calculator.app.databinding.ItemUserBinding;
import com.hd.calculator.app.ui.item.UserItem;

import java.util.List;

public class UserAdapter extends AbsAdapter<UserItem, ItemUserBinding>{
    public UserAdapter(List<UserItem> data) {
        super(data);
    }

    @Override
    public ItemUserBinding getBinding(ViewGroup parent) {
        return ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    public void onBindingViewHolder(UserItem item, ItemUserBinding binding, int position) {
        binding.tvUser.setText(item.getUserName());
        if (item.isSelected()) {
            binding.ivSelect.setVisibility(View.VISIBLE);
        } else {
            binding.ivSelect.setVisibility(View.GONE);
        }
    }
}
