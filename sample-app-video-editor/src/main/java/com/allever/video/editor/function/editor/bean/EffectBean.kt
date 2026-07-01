package com.allever.video.editor.function.editor.bean

import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import com.android.absbase.utils.ResourcesUtils
import com.android.absbase.utils.TimeUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.media.BitmapUtil
import com.allever.video.editor.ui.widget.gesture.IContentView
import com.allever.video.editor.utils.MediaTypeUtil
import java.util.*
import kotlin.math.max

data class VideoTime(
        /**
         * 视频源文件的开始时间
         */
        var srcStartTime: Long,
        /**
         * 视频源文件的结束时间
         */
        var srcEndTime: Long,
        /**
         * 目标视频的开始时间
         */
        var dstStartTime: Long,
        /**
         * 目标视频的结束时间
         */
        var dstEndTime: Long) {

    fun clone(): VideoTime {
        return VideoTime(
            srcStartTime,
            srcEndTime,
            dstStartTime,
            dstEndTime
        )
    }

    fun set(vt: VideoTime) {
        srcStartTime = vt.srcStartTime
        srcEndTime = vt.srcEndTime
        dstStartTime = vt.dstStartTime
        dstEndTime = vt.dstEndTime
    }
}


/**
 * 所有视频编辑需要用到的特效的基类
 */
open class EffectBean {
    companion object {
        /**
         * 媒体资源的分块时间
         */
        const val SPILT_TIME_BITMAP = TimeUtils.TimeConstant.ONE_SEC
        /**
         * 当前状态有效
         */
        const val STATE_VALID = 0
        /**
         * 当前处于删除状态
         */
        const val STATE_DELETE = 1

        /**
         * 重复状态: 不重复
         */
        const val REPEAT_NO = 0
        /**
         * 重复状态: 无限重复
         */
        const val REPEAT_UNLIMITED = -1

        /**
         * seek bar 调节
         * @param percentage
         * @param start
         * @param end
         * @return
         */
        fun range(percentage: Int, start: Float, end: Float): Float {
            return (end - start) * percentage / 100.0f + start
        }
    }

    /**
     * bean 对应在EditorLayout的图片宽高
     */
    var thumbBitmapWidth = ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_bitmap_width)
    var thumbBitmapHeight = ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_bitmap_height)
    var thumbBitmapPadding = ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_padding)
    var stickerBitmapSize =  ResourcesUtils.getDimension(R.dimen.effect_edit_sticker_bitmap_size).toInt()
    /**
     * 默认小图标
     */
    var smallIcon: Drawable? = null

    var labelStartColor: Int? = null
    var labelEndColor: Int? = null

    /**
     * 资源路径(content://)
     */
    var uri:Uri? = null
    /**
     * 资源路径(storage/0/emulated/)
     */
    var path:String? = null
    /**
     * 资源的类型
     */
    var type =  MediaTypeUtil.TYPE_OTHER_IMAGE
    /**
     * 用来对bean唯一化, 相同id,则认为是同一个特效
     */
    var id: Int = 0
        private set
    /**
     * 时长
     */
    var dstDuration: Long = 0
        get() = videoTime.dstEndTime - videoTime.dstStartTime
        private set

    var srcDuration: Long = 0
        get() = videoTime.srcEndTime - videoTime.srcStartTime
        private set

    /**
     * 是否允许扩大时长
     */
    var allowExpand = true
        protected set

    var duration: Long = 0
    /**
     * 特效时间
     */
    var videoTime: VideoTime =
        VideoTime(0, 0, 0, 0)

    /**
     * 特效位置信息
     */
    var position: RectF = RectF()

    /**
     * 特效角度
     */
    var angle: Float = 0f

    /**
     * 特效当前的状态
     */
    var state: Int = 0

    /**
     * 重复状态, -1, 0, 1+
     */
    var repeat: Int = REPEAT_NO

    /**
     * 播放完成是否回到视频开始位置
     */
    var whetherEnd2Start: Boolean = true

    var frameBitmapList: ArrayList<Bitmap?> = ArrayList()
    var frameThumbBitmapList: ArrayList<Bitmap?> = ArrayList()

    protected val onEffectListeners = Collections.synchronizedList(ArrayList<EffectListener>())!!

    /**
     * 主副特效
     */
    var primary = true

    constructor() {
        // 考虑怎么来指定id, new出来的bean一定是一个新的id, clone出去的bean一定是一个重复的bean
        id = this.hashCode()
    }

    fun isDelete(): Boolean {
        return state == STATE_DELETE
    }

    /**
     * 在指定view上播放特效, 需要根据调研结果来决定怎么play
     */
    open fun play(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        seekTo(timeOffset, view, isPlaying)
    }

    open fun seekTo(timeOffset: Long, view: IContentView?, isPlaying: Boolean) {
        val effectView = view?.getEffectView(this)
        effectView?.let {
            if (inTimeLine(timeOffset) && state != STATE_DELETE) {
                it.visibility = View.VISIBLE
            } else {
                // 这里如果使用GONE,会出现获取坐标问题
                // bug: 选择2个图片进入编辑, 直接点击save,第二个图片无法save,原因为GONE后获取的坐标错误
                it.visibility = View.INVISIBLE
                view?.invalidateSelf()
                it
            }
        }
    }

    open fun pause(view: IContentView?) {

    }

    open fun stop(view: IContentView?) {

    }

    open fun release() {

    }

    /**
     * 刷新bean及相应的View
     */
    open fun refresh(view: IContentView){

    }

    /**
     * 获取媒体资源裁剪总宽度
     */
    open fun getCropTotalWidth(singleWidth: Int, centerPadding: Int): Int {
        val counts = getCropBitmapCounts()
        val divisibleRatio = getDstCropDivisibleRatio()
        var width = singleWidth * counts + centerPadding * (counts - 1)
        if (divisibleRatio > 0f) {
            width -= ((1 - divisibleRatio) * singleWidth).toInt()
        }
        return width.toInt()
    }
    /**
     * 获取媒体资源原始总宽度
     */
    open fun getOriginalTotalWidth(singleWidth: Int, centerPadding: Int): Int {
        val counts = getOriginalBitmapCounts()
        val divisibleRatio = getOriginalDivisibleRatio()
        var width = singleWidth * counts + centerPadding * (counts - 1)
        if (divisibleRatio > 0f) {
            width -= ((1 - divisibleRatio) * singleWidth).toInt()
        }
        return width.toInt()
    }
    /**
     * 获取媒体资源帧图片数(裁剪后)
     * 7 秒 返回 7
     * 7.6秒 返回 8，
     */
    open fun getCropBitmapCounts():Int{
        var count = (dstDuration / SPILT_TIME_BITMAP).toInt()
        if(getDstCropDivisibleRatio().compareTo(0) != 0){
            count++
        }
        return count
    }
    /**
     * 获取媒体资源帧图片数(源)
     * 7 秒 返回 7
     * 7.6秒 返回 8，
     */
    open fun getOriginalBitmapCounts():Int{
        val duration = Math.max(Math.max(duration, srcDuration), dstDuration)
        var count = (duration / SPILT_TIME_BITMAP).toInt()
        if(getOriginalDivisibleRatio().compareTo(0) != 0){
            count++
        }
        return count
    }

    /**
     * 时长7.6秒 ，那返回 600/1000 = 0.6
     */
    open fun getOriginalDivisibleRatio(): Float {
        val duration = Math.max(Math.max(duration, srcDuration), dstDuration)
        val modTime = duration % SPILT_TIME_BITMAP
        return 1.0f * modTime / SPILT_TIME_BITMAP
    }

    /**
     * 时长7.6秒 ，那返回 600/1000 = 0.6
     */
    open fun getDstCropDivisibleRatio(): Float {
        val modTime = dstDuration % SPILT_TIME_BITMAP
        return 1.0f * modTime / SPILT_TIME_BITMAP
    }

    /**
     * 时长7.6秒 ，那返回 600/1000 = 0.6
     */
    open fun getSrcCropDivisibleRatio(): Float {
        val modTime = srcDuration % SPILT_TIME_BITMAP
        return 1.0f * modTime / SPILT_TIME_BITMAP
    }

    open fun getBitmapCountForFrame(): Int {
        return getOriginalBitmapCounts()
    }

    /**
     * 获取媒体资源帧图片(所有图片)
     */
    open fun getBitmapForFrame(): List<Bitmap?> {
        val count = getOriginalBitmapCounts()
        val expansionCount = max(count, frameBitmapList.size) - frameBitmapList.size
        if (expansionCount > 0) {
            for (i in 0 until expansionCount) {
                frameBitmapList.add(null)
            }
        }
        return frameBitmapList.subList(0, count)
    }

    open fun getThumbBitmapForFrame(): List<Bitmap?> {
        val count = getOriginalBitmapCounts()
        val expansionCount = max(count, frameThumbBitmapList.size) - frameThumbBitmapList.size
        if (expansionCount > 0) {
            for (i in 0 until expansionCount) {
                frameThumbBitmapList.add(null)
            }
        }
        return frameThumbBitmapList.subList(0, count)
    }

    open fun getBitmapForFrameByIndex(index: Int): Bitmap? {
        return if (index in 0 until frameBitmapList.size) {
            frameBitmapList[index]
        } else null
    }

    open fun getThumbBitmapForFrameByIndex(index: Int): Bitmap? {
        return if (index in 0 until frameThumbBitmapList.size) {
            frameThumbBitmapList[index]
        } else null
    }

    /**
     * 输出特效, 需要根据调研结果来决定怎么save
     */
    open fun save() {

    }

    open fun allowMoveDst(startTime: Long?, endTime: Long? = null): Boolean {
        if (!allowExpand) {
            val dstStartTime = startTime ?: videoTime.dstStartTime
            val dstEndTime = endTime ?: videoTime.dstEndTime
            if (dstEndTime - dstStartTime > srcDuration) {
                return false
            }
        }
        return true
    }

    open fun allowMoveSrc(startTime: Long?, endTime: Long? = null): Boolean {
        if (!allowExpand) {
            val dstStartTime = startTime ?: videoTime.srcStartTime
            val dstEndTime = endTime ?: videoTime.srcEndTime
            if (dstEndTime - dstStartTime > srcDuration) {
                return false
            }
        }
        return true
    }

    /**
     * 移动特效到指定时间
     */
    open fun moveDst(startTime: Long?, endTime: Long? = null) {
//        if (!allowMoveDst(startTime, endTime)) {
//            return
//        }
        val duration = dstDuration
        if (startTime != null) {
            videoTime.dstStartTime = startTime
        }
        videoTime.dstEndTime = endTime ?: videoTime.dstStartTime + duration
    }

    open fun moveDstBy(startOffset: Long? = null, endOffset: Long? = null) {
        if (startOffset != null) {
//            val endTime = if (endOffset != null) {
//                videoTime.dstEndTime + endOffset
//            } else null
//            if (!allowMoveDst(videoTime.dstStartTime + startOffset, endTime)) {
//                return
//            }
            videoTime.dstStartTime += startOffset
        }
        if (endOffset != null) {
//            val startTime = if (startOffset != null) {
//                videoTime.dstStartTime + startOffset
//            } else null
//            if (!allowMoveDst(startTime, videoTime.dstEndTime + endOffset)) {
//                return
//            }
            videoTime.dstEndTime += endOffset
        }
    }

    open fun moveSrc(newStartTime: Long?, newEndTime: Long? = null) {
//        if (!allowMoveSrc(newStartTime, newEndTime)) {
//            return
//        }
        if (newStartTime != null) {
            videoTime.srcStartTime = newStartTime
        }
        if (newEndTime != null) {
            videoTime.srcEndTime = newEndTime
        }
    }

    open fun moveSrcBy(startOffset: Long? = null, endOffset: Long? = null) {
        if (startOffset != null) {
//            val endTime = if (endOffset != null) {
//                videoTime.srcEndTime + endOffset
//            } else null
//            if (!allowMoveSrc(videoTime.srcStartTime + startOffset, endTime)) {
//                return
//            }
            videoTime.srcStartTime += startOffset
        }
        if (endOffset != null) {
//            val startTime = if (startOffset != null) {
//                videoTime.srcStartTime + startOffset
//            } else null
//            if (!allowMoveSrc(startTime, videoTime.srcEndTime + endOffset)) {
//                return
//            }
            videoTime.srcEndTime += endOffset
        }
    }

    open fun inTimeLine(timeOffset: Long): Boolean {
        return timeOffset in videoTime.dstStartTime..videoTime.dstEndTime
    }

    /**
     * 获取指定时间点的快照
     * @param timeOffset 时间戳
     * @param view 对应view 获取位置、角度、缩放等信息
     */
    open fun snapshot(timeOffset: Long, view: IContentView?): Bitmap? {
        return if (inTimeLine(timeOffset)) {
            getEffectBitmap(view,timeOffset)
        } else null
    }

    /**
     * 获取特效Bitmap
     */
    open fun getEffectBitmap(view: IContentView?, timeOffset: Long = -1): Bitmap? {
        view ?: return getOriginalBitmap(view,timeOffset)
        if (state != STATE_VALID) {
            return null
        }
        val videoRect = view.videoRect
        //以视频大小创建位图
        val outBitmap = Bitmap.createBitmap(videoRect.width(), videoRect.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outBitmap)
        val effectView = view.getEffectView(this)
        if (effectView != null) {
            //获取view缩放大小
            val dstRect = view.getEffectViewRect(this)
            val rotate = effectView.rotation
            val alpha = effectView.alpha * 255
            val originalBitmap = getOriginalBitmap(view,timeOffset)
            if (originalBitmap != null) {
                val originalRect = Rect(0, 0, originalBitmap.width, originalBitmap.height)
                val paint = Paint()
                paint.alpha = alpha.toInt()
                //抗锯齿
                paint.isAntiAlias = true
                paint.isFilterBitmap = true
                val paintFilter = PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG)
                canvas.drawFilter = paintFilter
                canvas.rotate(rotate,dstRect.centerX(),dstRect.centerY())
                canvas.drawBitmap(originalBitmap, originalRect, dstRect, paint)
            }
        }
        return outBitmap
    }

    /**
     * 获取原图
     */
    open fun getOriginalBitmap(view: IContentView?, timeOffset: Long): Bitmap? {
        return null
    }

    open fun clone(action: EffectBean): EffectBean {
        action.thumbBitmapWidth = thumbBitmapWidth
        action.thumbBitmapHeight = thumbBitmapHeight
        action.thumbBitmapPadding = thumbBitmapPadding
        action.stickerBitmapSize = stickerBitmapSize
        action.smallIcon = smallIcon
        action.labelStartColor = labelStartColor
        action.labelEndColor = labelEndColor
        action.uri = uri
        action.path = path
        action.type = type
        action.id = id
        action.dstDuration = dstDuration
        action.videoTime = videoTime.clone()
        action.allowExpand = allowExpand
        action.position = position
        action.angle = angle
        action.state = state
        action.repeat = repeat
        action.whetherEnd2Start = whetherEnd2Start
        action.frameBitmapList = frameBitmapList
        return action
    }

    open fun clone(): EffectBean {
        return clone(EffectBean())
    }

    open fun set(eb: EffectBean) {
        thumbBitmapWidth = eb.thumbBitmapWidth
        thumbBitmapHeight = eb.thumbBitmapHeight
        thumbBitmapPadding = eb.thumbBitmapPadding
        stickerBitmapSize = eb.stickerBitmapSize
        smallIcon = eb.smallIcon
        labelStartColor = eb.labelStartColor
        labelEndColor = eb.labelEndColor
        uri = eb.uri
        path = eb.path
        type = eb.type
        id = eb.id
        dstDuration = eb.dstDuration
        videoTime.set(eb.videoTime)
        position = eb.position
        allowExpand = eb.allowExpand
        angle = eb.angle
        state = eb.state
        repeat = eb.repeat
        whetherEnd2Start = eb.whetherEnd2Start
        frameBitmapList = eb.frameBitmapList
    }

    fun addEffectListener(listener: EffectListener) {
        if (!onEffectListeners.contains(listener)) {
            onEffectListeners.add(listener)
        }
    }

    fun removeEffectListener(listener: EffectListener) {
        if (onEffectListeners.contains(listener)) {
            onEffectListeners.remove(listener)
        }
    }

    open fun destroy() {
        frameBitmapList.map {
            BitmapUtil.recycle(it)
        }
        frameBitmapList.clear()

        frameThumbBitmapList.map {
            BitmapUtil.recycle(it)
        }
        frameThumbBitmapList.clear()

        onEffectListeners.clear()
    }

    interface EffectListener {
        /**
         * 返回图片
         */
        fun callBack(bitmaps: MutableList<Bitmap?>)

        fun callBack(index: Int, bitmap: Bitmap)
    }
}