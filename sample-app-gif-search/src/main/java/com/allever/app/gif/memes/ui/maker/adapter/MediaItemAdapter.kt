package com.allever.app.gif.memes.ui.maker.adapter

import android.view.ViewGroup
import com.allever.app.gif.memes.databinding.ItemMediaBinding
import com.allever.app.gif.memes.ui.maker.adapter.bean.MediaItem
import com.allever.app.gif.memes.ui.maker.adapter.view.MediaItemView
import com.android.absbase.utils.DeviceUtils
import com.xm.lib.base.adapter.recyclerview.BaseRecyclerAdapter
import com.xm.lib.base.adapter.recyclerview.BaseViewHolder

class MediaItemAdapter : BaseRecyclerAdapter<MediaItem, BaseViewHolder<MediaItem>>() {

    private var mItemWidth = 0
    init {
        val screenWidth = DeviceUtils.SCREEN_WIDTH_PX.toFloat()
        val margin = DeviceUtils.dip2px(1f)
        mItemWidth = Math.round((screenWidth - margin * 4) / 3f)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<MediaItem> {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        val itemView = viewHolder.getItemView<MediaItemView>()
        val binding = itemView.getDataBinding<ItemMediaBinding>()
        val lp = binding.itemView.layoutParams
//        lp.width = mItemWidth.toInt()
        //根据宽高决定高度
        lp.height = mItemWidth
        itemView.layoutParams = lp
//        itemView.tag = viewHolder
        return viewHolder
    }
    override fun bindViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder<MediaItem> {
        return BaseViewHolder(MediaItemView(mContext))
    }
}