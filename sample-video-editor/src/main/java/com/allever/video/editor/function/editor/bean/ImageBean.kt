package com.allever.video.editor.function.editor.bean

import android.graphics.Bitmap
import com.allever.video.editor.function.editor.VideoEditorManager
import com.allever.video.editor.function.media.BitmapUtil
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.ui.widget.gesture.IContentView
import kotlin.math.max


open class ImageBean : EffectBean() {

    private var thumbBitmap: Bitmap? = null
    var bitmap: Bitmap? = null
//        private set

    private fun timeToShow(timeOffset: Int): Boolean {
        return this.videoTime.dstStartTime <= timeOffset && (timeOffset + this.videoTime.dstStartTime) <= this.videoTime.dstEndTime
    }


    override fun getBitmapForFrame(): List<Bitmap?> {
        val count = getOriginalBitmapCounts()
        val expansionCount = max(count, frameBitmapList.size) - frameBitmapList.size
        if (expansionCount > 0) {
            for (i in 0 until expansionCount) {
                frameBitmapList.add(bitmap)
            }
        }
        return frameBitmapList.subList(0, count)
    }

    override fun getThumbBitmapForFrame(): List<Bitmap?> {
        val count = getOriginalBitmapCounts()
        val expansionCount = max(count, frameThumbBitmapList.size) - frameThumbBitmapList.size
        if (expansionCount > 0) {
            for (i in 0 until expansionCount) {
                frameThumbBitmapList.add(thumbBitmap)
            }
        }
        return frameThumbBitmapList.subList(0, count)
    }

    override fun getBitmapForFrameByIndex(index: Int): Bitmap? {
        return bitmap
    }

    override fun getThumbBitmapForFrameByIndex(index: Int): Bitmap? {
        return thumbBitmap
    }

    override fun clone(action: EffectBean): EffectBean {
        super.clone(action)
        (action as? ImageBean)?.let {
            it.thumbBitmap = thumbBitmap
            it.bitmap = bitmap
            it
        }
        return action
    }

    override fun clone(): EffectBean {
        return clone(ImageBean())
    }

    override fun set(eb: EffectBean) {
        super.set(eb)
        (eb as? ImageBean)?.let {
            thumbBitmap = it.thumbBitmap
            bitmap = it.bitmap
            it
        }
    }
    override fun getOriginalBitmap(view: IContentView?, timeOffset: Long): Bitmap? {
        return if(bitmap == null && path != null){
            try {
                bitmap  = BitmapUtil.decodeSampledBitmapFromFile(path, -1, -1)
                bitmap
            }catch (e:Exception){
                null
            }
        }else bitmap
    }

    override fun destroy() {
        super.destroy()

        BitmapUtil.recycle(thumbBitmap)
        BitmapUtil.recycle(bitmap)
    }

    companion object {
        fun getBean(obj: Any): ImageBean {
            return getBean(obj, 0)
        }

        fun getBean(obj: Any, timeLineOffset: Long): ImageBean {
            // 根据内容获取一个video bean
            // 比如传入一个视频文件路径,根据路径得到一个bean
            val imageBean = ImageBean()
            if (obj is ThumbnailBean) {
                imageBean.uri = obj.uri
                imageBean.path = obj.path
                imageBean.bitmap = BitmapUtil.decodeSampledBitmapFromFile(obj.path, -1, -1)
                imageBean.thumbBitmap = BitmapUtil.scaleBitmap(imageBean.bitmap, imageBean.thumbBitmapWidth, imageBean.thumbBitmapHeight)
                val duration = VideoEditorManager.staticImageDuration
                imageBean.position.set(0f, 0f,
                        imageBean.bitmap?.width?.toFloat() ?: 300f,
                        imageBean.bitmap?.height?.toFloat() ?: 300f
                )
                imageBean.type = obj.type
                imageBean.videoTime.dstStartTime = timeLineOffset
                imageBean.videoTime.dstEndTime = timeLineOffset + duration
                imageBean.videoTime.srcStartTime = 0
                imageBean.videoTime.srcEndTime = duration
                imageBean.duration = duration
                imageBean.allowExpand = true
            }
            return imageBean
        }
    }
}