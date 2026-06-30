package com.allever.video.editor.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.android.absbase.utils.DeviceUtils
import com.allever.video.editor.R
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.utils.ImageLoader
import com.allever.video.editor.utils.MediaTypeUtil
import com.allever.video.editor.utils.TimeUtils
import com.bumptech.glide.Glide


/**
 * 图片列表的适配器
 */
class AlbumCellAdapter(val context: Context, val layoutResId: Int, data: MutableList<ThumbnailBean>?)
    : BaseRecyclerViewAdapter<ThumbnailBean>(context, layoutResId, data) {

    companion object {
        private val TAG = AlbumCellAdapter::class.java.simpleName
    }

    private var mItemWidth = 0
    init {
        val screenWidth = DeviceUtils.SCREEN_WIDTH_PX.toFloat()
        val margin = DeviceUtils.dip2px(4f)
        mItemWidth = Math.round((screenWidth - margin * 5) / 4)
    }

    private var mOptionListener: OptionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val itemView = LayoutInflater.from(context).inflate(layoutResId, parent,
                false)
        val holder = BaseViewHolder(context, itemView)
        val lp = itemView.layoutParams
//        lp.width = mItemWidth.toInt()
        //根据宽高决定高度
        lp.height = mItemWidth
        itemView.layoutParams = lp
        itemView.tag = holder
        return holder
    }

    override fun bindHolder(holder: BaseViewHolder, position: Int, item: ThumbnailBean) {
        ImageLoader.loadImage(item.path, R.drawable.icon_album_default,  holder.getView(R.id.iv_image))
        holder.getView<ImageView>(R.id.iv_image)?.let {
            Glide.with(mContext).asBitmap().load(item.path).placeholder(R.drawable.icon_album_default).override(100, 100).into(it)
        }

        if (MediaTypeUtil.isVideo(item.type)) {
            holder.setVisible(R.id.tv_video_duration, true)
            holder.setText(R.id.tv_video_duration, TimeUtils.formatTime(item.duration))
        } else {
            holder.setVisible(R.id.tv_video_duration, false)
        }

        holder.itemView.setOnClickListener {
            item.isChecked = !item.isChecked
            val result = mOptionListener?.onAlbumCellAdapterItemClick(item, position)
            if(result == true){
                checkSelected(holder, item)
            }else{
                item.isChecked = false
            }
        }

        holder.itemView.setOnLongClickListener {
            mOptionListener?.onAlbumCellAdapterItemLongClick(item, position)
            return@setOnLongClickListener true
        }

        checkSelected(holder, item)

    }

    fun setOptionListener(optionListener: OptionListener) {
        mOptionListener = optionListener
    }

    private fun checkSelected(holder: BaseViewHolder, item: ThumbnailBean) {
        if (item.isChecked) {
            holder.setVisible(R.id.mask, true)
            holder.setVisible(R.id.iv_select_flag, true)
        } else {
            holder.setVisible(R.id.mask, false)
            holder.setVisible(R.id.iv_select_flag, false)
        }
    }


    public interface OptionListener {
        fun onAlbumCellAdapterItemClick(thumbnailBean: ThumbnailBean, position: Int): Boolean

        fun onAlbumCellAdapterItemLongClick(thumbnailBean: ThumbnailBean, position: Int)
    }

}