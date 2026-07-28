package com.alsg.bakericon.ui.adapter

import com.alsg.bakericon.ui.adapter.data.AppItem
import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.RvAppItemBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@Description
 *@author: zq
 *@date: 2024/2/1
 */
class AppItemAdapter : BaseQuickAdapter<AppItem, BaseViewHolder>(R.layout.rv_app_item) {
    override fun convert(holder: BaseViewHolder, item: AppItem) {
        val binding = RvAppItemBinding.bind(holder.itemView)
        binding.apply {
            ivIcon.setImageDrawable(item.iconDrawable)
            tvTitle.text = item.name
            tvPkg.text = item.pkg
        }
    }
}