package com.allever.video.editor.ui

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.VideoView
import com.allever.video.editor.R
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.utils.ImageLoader
import com.allever.video.editor.utils.MediaTypeUtil
import com.allever.video.editor.utils.VideoViewHolder

class PreviewFragment : androidx.fragment.app.Fragment() {

    companion object {
        private val TAG = PreviewFragment::class.java.simpleName
    }

    //    private var mVideoMark: View? = null

    private var mThumbnailBean: ThumbnailBean? = null
    private var mVideoViewHolder: VideoViewHolder? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater?.inflate(R.layout.fragment_preview, container, false)

        val imageView = rootView?.findViewById<ImageView>(R.id.id_iv_image)
        val videoView = rootView?.findViewById<VideoView>(R.id.id_video_view)
        val ivPlayAndPause = rootView?.findViewById<ImageView>(R.id.id_iv_video_controller)
//        mVideoMark = mView?.findViewById(R.id.id_video_mark)

        if (MediaTypeUtil.isImage(mThumbnailBean?.type ?: -1)) {
            //图片类型
            imageView?.visibility = View.VISIBLE
            ImageLoader.loadImage(mThumbnailBean?.path, imageView)
        } else if (MediaTypeUtil.isVideo(mThumbnailBean?.type ?: -1)) {
            //视频类型
            imageView?.visibility = View.GONE

            mVideoViewHolder = VideoViewHolder()
            mVideoViewHolder?.initVideo(videoView, mThumbnailBean?.path, ivPlayAndPause)
        }

        if (mThumbnailBean?.isAutoPlay == true) {
            mVideoViewHolder?.play()
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mVideoViewHolder?.stop()
        mVideoViewHolder?.destroy()
        mVideoViewHolder = null
    }

    fun pause() {
        mVideoViewHolder?.pause()
    }

    fun setData(thumbnailBean: ThumbnailBean?) {
        mThumbnailBean = thumbnailBean
    }

}