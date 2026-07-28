package com.alsg.bakericon.ui.adapter

import app.allever.android.lib.imageloader.core.load
import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.SiRvPackBinding
import com.alsg.bakericon.ui.adapter.data.PackItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@Description
 *@author: 专辑Adapter，Icon和Sticker
 *@date: 2024/1/9
 */
class CommonPackItemAdapter : BaseQuickAdapter<PackItem, BaseViewHolder>(R.layout.si_rv_pack) {
    override fun convert(holder: BaseViewHolder, item: PackItem) {
        val binding = SiRvPackBinding.bind(holder.itemView)
        binding.tvName.text = item.name
        binding.ivCover.load(item.cover)
    }
}