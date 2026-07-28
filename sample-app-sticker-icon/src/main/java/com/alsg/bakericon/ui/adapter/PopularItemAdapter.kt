package com.alsg.bakericon.ui.adapter

import app.allever.android.lib.imageloader.core.load
import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.RvPopularImgBinding
import com.alsg.bakericon.ui.adapter.data.SingleItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@Description Icon和Sticker底部popular列表Adapter
 *@author:
 *@date: 2024/1/9
 */
class PopularItemAdapter : BaseQuickAdapter<SingleItem, BaseViewHolder>(R.layout.rv_popular_img) {
    override fun convert(holder: BaseViewHolder, item: SingleItem) {
        val binding = RvPopularImgBinding.bind(holder.itemView)
        binding.ivImage.load(item.url)
    }
}