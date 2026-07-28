package com.alsg.bakericon.ui.adapter

import com.allever.lib.base.function.imageloader.load
import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.RvPackBinding
import com.alsg.bakericon.ui.adapter.data.PackItem
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@Description
 *@author: 专辑Adapter，Icon和Sticker
 *@date: 2024/1/9
 */
class CommonPackItemAdapter : BaseQuickAdapter<PackItem, BaseViewHolder>(R.layout.rv_pack) {
    override fun convert(holder: BaseViewHolder, item: PackItem) {
        val binding = RvPackBinding.bind(holder.itemView)
        binding.tvName.text = item.name
        binding.ivCover.load(item.cover)
    }
}