package com.allever.video.editor.ui.adapter

import android.content.Context
import com.allever.video.editor.R
import com.allever.video.editor.ui.bean.ImageFolder
import com.allever.video.editor.utils.ImageLoader


/***
 * 选择相册的适配器
 */
class SelectAlbumAdapter(context: Context, layoutId: Int, data: MutableList<ImageFolder>) :
    BaseRecyclerViewAdapter<ImageFolder>(context, layoutId, data) {

    private var mOptionListener: OptionListener? = null

    override fun bindHolder(holder: BaseViewHolder, position: Int, item: ImageFolder) {
        ImageLoader.loadImage(item.firstThumbnailBean.path, holder.getView(R.id.iv_image))

        holder.setText(R.id.tv_photo_count, item.photoCount.toString())
        holder.setText(R.id.tv_video_count, item.videoCount.toString())
        holder.setText(R.id.tv_album_name, item.name)
        holder.itemView.setOnClickListener {
            mOptionListener?.onChooseAlbumAdapterItemClick(item, position)
        }
    }

    fun setOptionListener(optionListener: OptionListener) {
        mOptionListener = optionListener
    }

    public interface OptionListener {
        fun onChooseAlbumAdapterItemClick(imageFolder: ImageFolder, position: Int)
    }
}