package com.allever.app.gif.memes.ui.maker.adapter

import android.content.Context
import android.view.ViewGroup
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.core.util.DeviceUtils
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.ui.maker.adapter.bean.MediaItem
import com.allever.app.gif.memes.ui.widget.recycler.BaseRecyclerViewAdapter
import com.allever.app.gif.memes.ui.widget.recycler.BaseViewHolder
import com.bumptech.glide.Glide

class MediaItemAdapter(val context: Context, resId: Int, data: MutableList<MediaItem>) :
    BaseRecyclerViewAdapter<MediaItem>(context, resId, data) {

    private var mItemWidth = 0

    init {
        val screenWidth = DisplayHelper.getScreenWidth()
        val margin = DisplayHelper.dip2px(1f)
        mItemWidth = Math.round((screenWidth - margin * 4) / 3f)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        val itemView = viewHolder.itemView
        val lp = itemView.layoutParams
//        lp.width = mItemWidth.toInt()
        //根据宽高决定高度
        lp.height = mItemWidth
        itemView.layoutParams = lp
        return viewHolder
    }

    override fun bindHolder(
        holder: BaseViewHolder,
        position: Int,
        item: MediaItem
    ) {
        Glide.with(context).load(item.data?.uri).into(holder.getView(R.id.iv_image))
        if (item.selected) {
            holder.setImageResource(R.id.iv_select_flag, R.drawable.icon_album_select)
        } else {
            holder.setImageResource(R.id.iv_select_flag, R.drawable.icon_album_unselected)
        }
        holder.itemView
        holder.setOnClickListener(holder.itemView.id, {
            mItemListener?.onItemClick(position, holder)
        })
        holder.setOnLongClickListener(holder.itemView.id, {
            return@setOnLongClickListener mItemListener?.onItemLongClick(position, holder) ?: false
        })

    }
}