package com.allever.daymatter.ui.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.allever.daymatter.R;
import com.allever.daymatter.bean.ItemSlidMenuSort;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

/**
 *
 * @author Allever
 * @date 18/5/26
 */

public class SlidMenuSortAdapter extends BaseQuickAdapter<ItemSlidMenuSort, BaseViewHolder> {
    public SlidMenuSortAdapter(List<ItemSlidMenuSort> data) {
        super(R.layout.dm_item_slid_menu_sort, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, ItemSlidMenuSort item) {
        holder.setText(R.id.id_item_slid_sort_tv_name, item.getName());
        holder.setText(R.id.id_item_slid_sort_tv_count, item.getCount() + "");

    }
}
