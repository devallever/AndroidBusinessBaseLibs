package com.alsg.bakericon.ui.adapter

import app.allever.android.lib.imageloader.core.load
import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.RvFrameImageBigBinding
import com.alsg.bakericon.ui.adapter.data.SingleItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@Description 收藏，专辑 Adapter
 *@author: zq
 *@date: 2024/1/11
 */
class BigFrameImgAdapter :
    BaseQuickAdapter<SingleItem, BaseViewHolder>(R.layout.rv_frame_image_big) {
    override fun convert(holder: BaseViewHolder, item: SingleItem) {
        val binding = RvFrameImageBigBinding.bind(holder.itemView)
        binding.ivImage.load(item.url)
    }
}