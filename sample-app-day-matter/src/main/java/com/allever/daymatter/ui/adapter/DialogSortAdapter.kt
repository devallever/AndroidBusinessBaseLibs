package com.allever.daymatter.ui.adapter

import com.allever.daymatter.data.Event
import com.chad.library.adapter.base.BaseQuickAdapter
import com.allever.daymatter.R
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class DialogSortAdapter(data: List<Event.Sort>)
    : BaseQuickAdapter<Event.Sort, BaseViewHolder>(R.layout.dm_item_dialog_sort,
    data as MutableList<Event.Sort>?
) {

    override fun convert(holder: BaseViewHolder, item: Event.Sort) {
        holder.setText(R.id.item_dialog_sort_title, item.name)
    }
}