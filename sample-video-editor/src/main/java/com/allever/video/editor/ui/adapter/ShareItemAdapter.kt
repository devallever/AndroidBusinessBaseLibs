package com.allever.video.editor.ui.adapter

import android.content.Context
import com.allever.video.editor.R
import com.allever.video.editor.function.share.ShareImageItem


class ShareItemAdapter(context: Context, layoutId: Int, data: MutableList<ShareImageItem.ShareImageItemData>) :
    BaseRecyclerViewAdapter<ShareImageItem.ShareImageItemData>(context, layoutId, data) {

    companion object {
        private val TAG = ShareItemAdapter::class.java.simpleName
    }

    private var mOptionListener: OptionListener? = null

    override fun bindHolder(holder: BaseViewHolder, position: Int, item: ShareImageItem.ShareImageItemData) {
        holder.setImageDrawable(R.id.iv_icon, item.getmIcon())
        holder.itemView.setOnClickListener {
            mOptionListener?.onShareItemClick(item)
        }
    }

    fun setOptionListener(optionListener: OptionListener) {
        mOptionListener = optionListener
    }

    public interface OptionListener {
        fun onShareItemClick(item: ShareImageItem.ShareImageItemData)
    }
}