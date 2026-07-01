package com.allever.video.editor.function.editor.bean

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.View
import com.allever.video.editor.function.media.BitmapUtil
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.ui.widget.gesture.IContentView
import com.allever.video.editor.ui.widget.video.TextureVideoView
import com.allever.video.editor.utils.AsyncTask
import com.allever.video.editor.utils.MediaThumbnailUtil
import kotlin.math.max

class VideoBean : EffectBean() {
    /**
     * 视频块, 一个视频需要有一个默认块
     */
    var videoBlock: MutableList<VideoTime> = arrayListOf()

    /**
     * 是否有音频
     */
    var hasAudio:Boolean = false

    /**
     * 声音音量 默认1  静音为0 [0 - 255]
     */
    var volume: Int = 128

    /**
     * 对视频进行切块
     */
    fun split(timeOffset: Long) {

    }

    /**
     * 当视频初始化，添加默认块
     */
    fun addBlock(){
        videoBlock.add(this.videoTime)
    }
    /**
     * 移动特效到指定时间
     */
    fun moveBlock(touchTimeOffset: Long, timeOffset: Long) {
    }

    /**
     * 裁剪开始时间
     */
    fun cropBlockStartTime(touchTimeOffset: Long, newStartTime: Long) {
    }

    /**
     * 裁剪结束时间
     */
    fun cropBlockEndTime(touchTimeOffset: Long, newEndTime: Long) {
    }

    fun setVideoVolume(volume: Int, view: IContentView?) {
        this.volume = volume
        val effectView =  view?.getEffectView(this)
        (effectView as? TextureVideoView)?.let {
            val volumeRatio = volume.toFloat() / 255
            it.mediaPlayer?.setVolume(volumeRatio, volumeRatio)
        }
    }

    private fun getBitmapBy(time: Long): Bitmap? {
        var bitmap: Bitmap? = null
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(path)
            val time = if (time > dstDuration) {
                dstDuration
            } else time
            bitmap = retriever.getFrameAtTime(time * 1000)

        } catch (ex: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (ex: Exception) {
            }
        }
        return bitmap
    }

    private fun checkBitmapForFrame(list: List<Bitmap?>, count: Int, thumbnail: Boolean, listeners: List<EffectListener>?): List<Bitmap?> {
        val path = path ?: return list
        val lists = list as ArrayList
        val notNullBitmaps = list.mapNotNull { it }
        if (notNullBitmaps.size != count) {
            object : AsyncTask<Void, Void, Unit>() {
                override fun doInBackground(vararg params: Void) {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(path)
                        lists.mapIndexed { index, bitmap ->
                            if (bitmap == null) {
                                var frameTime = index * SPILT_TIME_BITMAP
                                if (frameTime > dstDuration) {
                                    frameTime = dstDuration
                                }
                                var frameBitmap = retriever.getFrameAtTime(frameTime * 1000)
                                if (thumbnail) {
                                    frameBitmap = BitmapUtil.scaleBitmap(frameBitmap, thumbBitmapWidth, thumbBitmapHeight)
                                }
                                lists[index] = frameBitmap
                                listeners?.map {
                                    it.callBack(index, frameBitmap!!)
                                }
                            }
                        }
                    } catch (ex: Exception) {
                    } finally {
                        try {
                            retriever.release()
                        } catch (ex: Exception) {
                        }
                    }
                }

                override fun onPostExecute(result: Unit?) {
                    listeners?.map {
                        it.callBack(lists)
                    }
                }
            }.executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR)
        }
        return lists
    }

    override fun getBitmapForFrame(): List<Bitmap?> {
        val count = getOriginalBitmapCounts()
        val expansionCount = max(count, frameBitmapList.size) - frameBitmapList.size
        if (expansionCount > 0) {
            for (i in 0 until expansionCount) {
                frameBitmapList.add(null)
            }
        }
        val bitmaps = checkBitmapForFrame(frameBitmapList, count, false, onEffectListeners)
        return bitmaps.subList(0, count)
    }

    override fun getThumbBitmapForFrame(): List<Bitmap?> {
        val count = getOriginalBitmapCounts()
        val expansionCount = max(count, frameThumbBitmapList.size) - frameThumbBitmapList.size
        if (expansionCount > 0) {
            for (i in 0 until expansionCount) {
                frameThumbBitmapList.add(null)
            }
        }
        val bitmaps = checkBitmapForFrame(frameThumbBitmapList, count, true, onEffectListeners)
        return bitmaps.subList(0, count)
    }

    override fun play(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        val effectView =  view?.getEffectView(this)
        var need2play = false
        effectView?.let {
            if (inTimeLine(timeOffset) && state != EffectBean.STATE_DELETE && isPlaying) {
                it.visibility = View.VISIBLE
                need2play = true
            } else {
                it.visibility = View.GONE
                need2play = false
            }
        }
        (effectView as? TextureVideoView)?.let {
            if (need2play) {
                val duration = videoTime.srcEndTime - videoTime.srcStartTime
                if (!it.isPlaying) {
                    it.seekTo(((timeOffset.toInt() - videoTime.dstStartTime) % duration + videoTime.srcStartTime).toInt())
                    it.resume()
                } else {
                    val currentPosition = it.currentPosition
                    if (currentPosition >= videoTime.srcEndTime) {
                        it.seekTo(((timeOffset.toInt() - videoTime.dstStartTime) % duration + videoTime.srcStartTime).toInt())
                    }
                }
            } else {
                if(it.isPlaying){
                    it.pause()
                }
            }
            it
        }
    }

    override fun pause(view: IContentView?) {
        val effectView = view?.getEffectView(this)
        (effectView as? TextureVideoView)?.let {
            it.pause()
        }
    }

    override fun stop(view: IContentView?) {
        val effectView =  view?.getEffectView(this)
        (effectView as? TextureVideoView)?.let {
            it.stop()
        }
    }

    override fun seekTo(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        val effectView =  view?.getEffectView(this)
        effectView?.let {
            val visibility = if (inTimeLine(timeOffset) && state != EffectBean.STATE_DELETE) {
                it.visibility = View.VISIBLE
                true
            } else {
                it.visibility = View.GONE
                false
            }
            (effectView as? TextureVideoView)?.let { videoView ->
                if (visibility) {
                    videoView.seekTo((timeOffset.toInt() - videoTime.dstStartTime).toInt())
                } else {
                    if (videoView.isPlaying) {
                        videoView.pause()
                    }
                }
                null
            }
        }
    }

    override fun getOriginalBitmap(view: IContentView?, timeOffset: Long): Bitmap? {
        val effectView = view?.getEffectView(this)
        return (effectView as? TextureVideoView)?.let {
            val offset = if(timeOffset != -1L){ timeOffset }else it.currentPosition.toLong()
            val bitmap = getBitmapBy(offset)
            bitmap
        } ?: if (frameBitmapList.size > 0) {
            frameBitmapList[0]
        } else null
    }

    override fun clone(action: EffectBean): EffectBean {
        super.clone(action)
        (action as? VideoBean)?.let {
            it.videoBlock.clear()
            videoBlock.forEach { vt ->
                it.videoBlock.add(vt.clone())
            }
            it.videoBlock = videoBlock
            it.hasAudio = hasAudio
            it
        }
        return action
    }

    override fun clone(): EffectBean {
        return clone(VideoBean())
    }

    override fun set(eb: EffectBean) {
        super.set(eb)
        (eb as? VideoBean)?.let {
            it.videoBlock.mapIndexed { index, vt ->
                if (index in 0 until videoBlock.size) {
                    videoBlock[index].set(vt)
                } else {
                    videoBlock.add(vt)
                }
            }
            videoBlock.removeAll(videoBlock.subList(it.videoBlock.size, videoBlock.size))
            videoBlock = it.videoBlock
            hasAudio = it.hasAudio

            it
        }
    }

    override fun inTimeLine(timeOffset: Long): Boolean {
        for (block in videoBlock) {
            if (timeOffset in block.dstStartTime..block.dstEndTime) {
                return true
            }
        }
        return false
    }
    companion object {
        fun getBean(obj: Any): VideoBean {
            return getBean(obj,0)
        }

        fun getBean(obj: Any,timeLineOffset: Long): VideoBean {
            // 根据内容获取一个video bean
            val videoBean = VideoBean()
            // 比如传入一个视频文件路径,根据路径得到一个bean
            if(obj is ThumbnailBean){
                videoBean.uri = obj.uri
                videoBean.path = obj.path
                videoBean.type = obj.type
                MediaThumbnailUtil.loadVideoInfo(videoBean)
                videoBean.moveDst(timeLineOffset)
                videoBean.allowExpand = false
                videoBean.addBlock()

                // 提前开启获取缩略图
                videoBean.getThumbBitmapForFrame()
            }
            return videoBean
        }
    }
}