package com.alsg.bakericon.ui.adapter

import com.alsg.bakericon.R
import com.alsg.bakericon.databinding.RvPreviewBinding
import com.alsg.bakericon.ui.adapter.data.PreviewItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 *@Description
 *@author: zq
 *@date: 2024/1/11
 */
class PreviewItemAdapter : BaseQuickAdapter<PreviewItem, BaseViewHolder>(R.layout.rv_preview) {
    override fun convert(holder: BaseViewHolder, item: PreviewItem) {
        val binding = RvPreviewBinding.bind(holder.itemView)
        binding.preview.setBackgroundResource(item.previewBackgroundSmall)
    }
}