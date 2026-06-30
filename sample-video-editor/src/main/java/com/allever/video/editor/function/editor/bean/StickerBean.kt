package com.allever.video.editor.function.editor.bean

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.VideoEditorManager
import com.allever.video.editor.ui.widget.gesture.IContentView
import com.allever.video.editor.utils.AssetsUtil
import com.allever.video.editor.utils.MediaTypeUtil
import kotlin.random.Random

class StickerBean : ImageBean() {

    override fun clone(action: EffectBean): EffectBean {
        super.clone(action)
        (action as? StickerBean)?.let {
            it
        }
        return action
    }

    override fun clone(): EffectBean {
        return clone(StickerBean())
    }

    override fun set(eb: EffectBean) {
        super.set(eb)
        (eb as? StickerBean)?.let {
            it
        }
    }

    override fun getOriginalBitmap(view: IContentView?, timeOffset: Long): Bitmap? {
        return if(bitmap == null && path != null){
            try {
                bitmap  = AssetsUtil.toBitmap(path)
                bitmap
            }catch (e:Exception){
                null
            }
        }else bitmap
    }

    companion object {
        fun getBean(obj: Any): StickerBean {
            // 根据内容获取一个video bean
            // 比如传入一个视频文件路径,根据路径得到一个bean
            return getBean(obj,0)
        }

        fun getBean(obj: Any,timeLineOffset: Long): StickerBean {
            val stickerBean = StickerBean()
            stickerBean.primary = false
            if (obj is String) {
                stickerBean.uri = Uri.parse(obj)
                stickerBean.path = obj
                stickerBean.bitmap = AssetsUtil.toBitmap(obj)
            }
            if (obj is Bitmap) {
//                stickerBean.uri = Uri.parse(MediaStore.Images.Media.insertImage(App.getContext().contentResolver, obj, null, null))
                stickerBean.bitmap = obj
            }

            val duration = VideoEditorManager.staticImageDuration
            val size = stickerBean.stickerBitmapSize.toFloat()
            stickerBean.position.set(0f, 0f, size, size)
            stickerBean.type = MediaTypeUtil.TYPE_STICKER
            stickerBean.videoTime.dstStartTime = timeLineOffset
            stickerBean.videoTime.dstEndTime = timeLineOffset + duration
            stickerBean.videoTime.srcStartTime = 0
            stickerBean.videoTime.srcEndTime = duration
            stickerBean.duration = duration
            stickerBean.allowExpand = true
            stickerBean.smallIcon = ResourcesUtils.getDrawable(R.drawable.icon_edit_sticker_s)
            stickerBean.labelStartColor = Color.parseColor("#ffcd2f")
            stickerBean.labelEndColor = Color.parseColor("#febb08")
            val random = Random(System.currentTimeMillis())

            return stickerBean
        }
    }
}