package com.alsg.bakericon.ui.adapter

import app.allever.android.lib.imageloader.core.load
import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.RvFrameImageBinding
import com.alsg.bakericon.ui.adapter.data.SingleItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class TopItemAdapter : BaseQuickAdapter<SingleItem, BaseViewHolder>(R.layout.rv_frame_image) {
    override fun convert(holder: BaseViewHolder, item: SingleItem) {
        val binding = RvFrameImageBinding.bind(holder.itemView)
        binding.ivImage.load(item.url)
    }
}