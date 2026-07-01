package com.allever.video.editor.ui.adapter

import android.content.Context
import android.view.View
import com.allever.video.editor.R
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.utils.ImageLoader


/***
 * 选中的图片列表适配器
 */
class SelectedAdapter(context: Context, layoutId: Int, data: MutableList<ThumbnailBean>) :
    BaseRecyclerViewAdapter<ThumbnailBean>(context, layoutId, data) {

    private var mOptionListener: OptionListener? = null

    override fun bindHolder(holder: BaseViewHolder, position: Int, item: ThumbnailBean) {
        ImageLoader.loadImage(item.path, holder.getView(R.id.iv_image))
        holder.getView<View>(R.id.iv_delete)?.setOnClickListener {
            mOptionListener?.onSelectedAdapterDeleteClick(item, position)
        }
    }

    fun setOptionListener(optionListener: OptionListener) {
        mOptionListener = optionListener
    }

    public interface OptionListener {
        //        fun onSelectedAdapterItemClick(thumbnailBean: ThumbnailBean, position: Int)
        fun onSelectedAdapterDeleteClick(thumbnailBean: ThumbnailBean, position: Int)
    }
}