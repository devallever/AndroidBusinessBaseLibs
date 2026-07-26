package com.hd.calculator.app.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

public class AbsViewHolder<VB extends ViewBinding> extends RecyclerView.ViewHolder {
    private final VB binding;
    public AbsViewHolder(VB binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public VB getBinding() {
        return binding;
    }
}
