package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.android.absbase.utils.DebugUtil
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.editor.bean.EffectBean
import com.allever.video.editor.utils.TimeUtils
import com.allever.video.editor.utils.rectCenterExpansion

class MediaTrimView : View, EffectBean.EffectListener {
    private lateinit var paint: Paint
    private lateinit var bitmapMatrix: Matrix
    private lateinit var textPaint: Paint
    private lateinit var gestureDetector: GestureDetector
    private var mBitmapWidth = DeviceUtils.dip2px(30f)
    private var mBitmapHeight = mBitmapWidth
    private var mPadding =   ResourcesUtils.getDimension(R.dimen.bitmap_content_view_default_padding).toInt()
    private var defaultBitmap: Bitmap = BitmapFactory.decodeResource(ResourcesUtils.resources, R.drawable.icon_album_default)

    private val mainToneColor = resources.getColor(R.color.main_color_tone)
    private val arrowLeftDrawable = resources.getDrawable(R.drawable.arrow_left)
    private val arrowRightDrawable = resources.getDrawable(R.drawable.arrow_right)
    private val arrowWidth = arrowLeftDrawable.intrinsicWidth
    private val arrowHeight = arrowLeftDrawable.intrinsicHeight
    private val marginLeftAndRight = DeviceUtils.dip2pxF(20f)
    private val selectWidth = DeviceUtils.dip2pxF(15f)
    private val selectHeight = DeviceUtils.dip2pxF(30f)
    private val timeHeight = DeviceUtils.dip2pxF(13f)
    private val triangleHeight = DeviceUtils.dip2pxF(2f)
    private val intervalHeight = DeviceUtils.dip2pxF(3f)
    private val selectPadding = DeviceUtils.dip2pxF(3f)
    private val timeSize = DeviceUtils.dip2pxF(8f)
    private val timeColor = ResourcesUtils.getColor(R.color.video_edit_bottom_music_trim_time_text_color)
    private val noselectMark = ResourcesUtils.getColor(R.color.video_edit_bottom_trim_single_noselect_color)
    private val timeTextPadding = DeviceUtils.dip2pxF(3f)
    private var originalStartOffset = 0f
    private var originalEndOffset = 0f
    private var startMaxOffset = 0f
    private var endMinOffset = 0f
    private var speed = mBitmapWidth.toFloat() / 1000

    private var startSelectDrawRect = RectF()
    private var endSelectDrawRect = RectF()
    private var startSelectRect = RectF()
    private var endSelectRect = RectF()
    private var timeRect = RectF()
    private var trianglePath = Path()

    private var mBitmapList = mutableListOf<Bitmap?>()

    private var startOffset = 0f
    private var endOffset = 0f
    private var prevStartOffset = -1f
    private var prevEndOffset = -1f

    var onTimeChangeLintener: OnTimeChangeLintener? = null


    private var duration: Long = 0
        set(value) {
            field = value
            originalStartOffset = marginLeftAndRight
            originalEndOffset = marginLeftAndRight + speed * value
            startOffset = originalStartOffset
            endOffset = originalEndOffset
            startMaxOffset = originalEndOffset - speed * minDuration
            endMinOffset = originalStartOffset + speed * minDuration
            requestLayout()
        }

    private var startTime: Long = 0
        set(value) {
            field = value
            val temp = if (effectBean?.allowExpand == true) {
                0
            } else {
                value
            }
            startOffset = time2distance(temp) + marginLeftAndRight
            invalidate()
        }
    private var endTime: Long = 0
        set(value) {
            field = value
            val temp = if (effectBean?.allowExpand == true) {
                value - (effectBean?.videoTime?.dstStartTime ?: 0)
            } else {
                value
            }
            endOffset = time2distance(temp) + marginLeftAndRight
            invalidate()
        }

    var minDuration: Int = 1000
        set(value) {
            field = value
            startMaxOffset = endOffset - speed * value
            endMinOffset = startOffset + speed * value
        }

    var effectBean: EffectBean? = null
        set(value) {
            field = value
            if (value != null) {
                value.addEffectListener(this)
                val bitmapList = value.getThumbBitmapForFrame()
                mBitmapList = mutableListOf(*bitmapList.toTypedArray())
                duration = if(value.allowExpand){ value.dstDuration }else{ value.duration }
                startTime = if (value.allowExpand) {
                    value.videoTime.dstStartTime
                } else {
                    value.videoTime.srcStartTime
                }
                endTime = if (value.allowExpand) {
                    value.videoTime.dstEndTime
                } else {
                    value.videoTime.srcEndTime
                }
            }
        }

    var arrowColor = ResourcesUtils.getColor(R.color.main_color_tone)
        set(value) {
            field = value
            arrowLeftDrawable.setColorFilter(value, PorterDuff.Mode.SRC_IN)
            arrowRightDrawable.setColorFilter(value, PorterDuff.Mode.SRC_IN)
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView()
    }

    private fun initView() {
        paint = Paint(Paint.ANTI_ALIAS_FLAG)

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = timeSize
        textPaint.color = timeColor

        bitmapMatrix = Matrix()

        startSelectDrawRect.set(0f, 0f, selectWidth, selectHeight)
        endSelectDrawRect.set(0f, 0f, selectWidth, selectHeight)
        timeRect.set(0f, 0f, 0f, timeHeight)

        arrowColor = ResourcesUtils.getColor(R.color.main_color_tone)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = if (effectBean?.allowExpand == true) {
            (effectBean?.getCropTotalWidth(mBitmapWidth, 0)
                    ?: (speed * duration).toInt()) + (marginLeftAndRight * 2).toInt()
        } else {
            (effectBean?.getOriginalTotalWidth(mBitmapWidth, 0)
                    ?: (speed * duration).toInt()) + (marginLeftAndRight * 2).toInt()
        }
        val height = timeHeight + triangleHeight + intervalHeight + selectHeight
        setMeasuredDimension(width, height.toInt())
    }

    private var prevX = 0f
    private var prevY = 0f
    private var selectLeft = false
    private var selectRight = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        return gestureDetector.onTouchEvent(event)

        val event = event ?: return super.onTouchEvent(event)
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                prevX = x
                prevY = y
                selectLeft = startSelectRect.contains(x, y)
                selectRight = endSelectRect.contains(x, y)
                if (selectLeft || selectRight) {
                    onTimeChangeLintener?.timeStart(selectLeft, selectRight)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectLeft || selectRight) {
                    val distance = x - prevX
                    timeMove(selectLeft, selectRight, distance)
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                if (selectLeft || selectRight) {
                    onTimeChangeLintener?.timeEnd(
                            startOffset != prevStartOffset,
                            endOffset != prevEndOffset,
                            distance2time(startOffset-marginLeftAndRight), distance2time(endOffset-marginLeftAndRight))
                    prevStartOffset = startOffset
                    prevEndOffset = endOffset
                }
                selectLeft = false
                selectRight = false
            }
        }
        prevX = x
        prevY = y
        if (selectLeft || selectRight) {
            return true
        }
//        return super.onTouchEvent(event)
        return true
    }

    private fun timeMove(start: Boolean, end: Boolean, distance: Float) {
        var offset = distance
        if (start) {
            when {
                startOffset + distance < originalStartOffset -> {
                    offset = startOffset - originalStartOffset
                    startOffset = originalStartOffset
                }
                startOffset + distance > startMaxOffset -> {
                    offset = startMaxOffset - startOffset
                    startOffset = startMaxOffset
                }
                else -> startOffset += distance
            }
        } else if (end) {
            when {
                endOffset + distance > originalEndOffset -> {
                    offset = originalEndOffset - endOffset
                    endOffset = originalEndOffset
                }
                endOffset + distance < endMinOffset -> {
                    offset = endOffset - endMinOffset
                    endOffset = endMinOffset
                }
                else -> endOffset += distance
            }
        }
        minDuration = minDuration

        invalidate()

        val distance2time = distance2time(offset)
        onTimeChangeLintener?.timeMove(selectLeft, selectRight, distance2time)
    }

    /**
     * 距离转时间
     */
    private fun distance2time(dx: Float): Long {
        return (dx / speed).toLong()
    }


    /**
     * 时间转距离
     * @param intervalTime  时间间隔
     */
    private fun time2distance(intervalTime: Long): Float {
        return speed * intervalTime
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        paint.color = mainToneColor
        // 画开始时间
        if (true) {
            val distance = startOffset - marginLeftAndRight
            val time = distance2time(distance)
            val fontMetrics = textPaint.fontMetrics
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val formatTime = TimeUtils.formatTime(time)
            val timeTextWidth = textPaint.measureText(formatTime)
            val timeRectWidth = timeTextWidth + timeTextPadding * 2
            timeRect.set(startOffset - timeRectWidth / 2, 0f, startOffset + timeRectWidth / 2, timeHeight)
            canvas.drawRect(timeRect, paint)
            canvas.drawText(formatTime, startOffset - timeTextWidth / 2, timeRect.centerY() + textHeight / 4, textPaint)

            // 画三角形
            trianglePath.reset()
            trianglePath.moveTo(startOffset - triangleHeight, timeRect.bottom - 1)
            trianglePath.lineTo(startOffset + triangleHeight, timeRect.bottom - 1)
            trianglePath.lineTo(startOffset, timeRect.bottom + triangleHeight - 1)
            trianglePath.close()
            canvas.drawPath(trianglePath, paint)
        }

        // 画结束时间
        if (true) {
            val distance = endOffset - marginLeftAndRight
            val time = distance2time(distance)
            val fontMetrics = textPaint.fontMetrics
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val formatTime = TimeUtils.formatTime(time)
            val timeTextWidth = textPaint.measureText(formatTime)
            val timeRectWidth = timeTextWidth + timeTextPadding * 2
            timeRect.set(endOffset - timeRectWidth / 2, 0f, endOffset + timeRectWidth / 2, timeHeight)
            canvas.drawRect(timeRect, paint)
            canvas.drawText(formatTime, endOffset - timeTextWidth / 2, timeRect.centerY() + textHeight / 4, textPaint)

            // 画三角形
            trianglePath.reset()
            trianglePath.moveTo(endOffset - triangleHeight, timeRect.bottom - 1)
            trianglePath.lineTo(endOffset + triangleHeight, timeRect.bottom - 1)
            trianglePath.lineTo(endOffset, timeRect.bottom + triangleHeight - 1)
            trianglePath.close()
            canvas.drawPath(trianglePath, paint)
        }


        startSelectDrawRect.offsetTo(startOffset - selectWidth, timeHeight + triangleHeight + intervalHeight)
        endSelectDrawRect.offsetTo(endOffset, timeHeight + triangleHeight + intervalHeight)
        startSelectRect.set(startSelectDrawRect)
        startSelectRect.rectCenterExpansion(selectPadding)
        endSelectRect.set(endSelectDrawRect)
        endSelectRect.rectCenterExpansion(selectPadding)
        val left = marginLeftAndRight
        val startCenterX = startSelectDrawRect.centerX()
        val startCenterY = startSelectDrawRect.centerY()
        val endCenterX = endSelectDrawRect.centerX()
        val endCenterY = endSelectDrawRect.centerY()
        val top = startCenterY - mBitmapHeight / 2
        val size = mBitmapList.size
        val divisibleRatio = effectBean?.getOriginalDivisibleRatio() ?: 1.0f
        for (position in 0 until size) {
            val startX = mBitmapWidth * position.toFloat() + mPadding * position + originalStartOffset
            val bitmap = mBitmapList[position] ?: defaultBitmap
            bitmapMatrix.setScale(mBitmapWidth.toFloat()/bitmap.width, mBitmapHeight.toFloat()/bitmap.height)
            bitmapMatrix.postTranslate(startX, top)
            if (position == size - 1 && divisibleRatio != 0f) {
                canvas.save()
                canvas.clipRect(startX, top, startX + mBitmapWidth * divisibleRatio, top + mBitmapHeight)
                canvas.drawBitmap(bitmap, bitmapMatrix, paint)
                canvas.restore()
            } else {
                canvas.drawBitmap(bitmap, bitmapMatrix, paint)
            }

            if (DebugUtil.isDebuggable()) {
                val oldColor = paint.color
                paint.color = Color.RED
                // 用来测试位置
                canvas.drawText("${position + 1}", startX, height.toFloat() / 2, paint)
                val last = width - startX
                if (last < mBitmapWidth) {
                    val scale = last / mBitmapWidth
                    val text = "$scale"
                    val measureText = paint.measureText(text)
                    canvas.drawText(text, startX - measureText, height.toFloat() / 2 + 20, paint)
                }
                paint.color = oldColor
            }
        }
        if (startOffset > originalStartOffset) {
            paint.color = noselectMark
            canvas.drawRect(originalStartOffset, top, startOffset, top + mBitmapHeight, paint)
        }
        if (endOffset < originalEndOffset) {
            paint.color = noselectMark
            canvas.drawRect(endOffset, top, originalEndOffset, top + mBitmapHeight, paint)
        }

        paint.color = mainToneColor
        canvas.drawRect(startSelectDrawRect, paint)
        canvas.drawRect(endSelectDrawRect, paint)

        arrowLeftDrawable.setBounds((startCenterX - arrowWidth / 2).toInt(),
                (startCenterY - arrowHeight / 2).toInt(),
                (startCenterX + arrowWidth / 2).toInt(),
                (startCenterY + arrowHeight / 2).toInt())
        arrowLeftDrawable.draw(canvas)
        arrowRightDrawable.setBounds((endCenterX - arrowWidth / 2).toInt(),
                (endCenterY - arrowHeight / 2).toInt(),
                (endCenterX + arrowWidth / 2).toInt(),
                (endCenterY + arrowHeight / 2).toInt())
        arrowRightDrawable.draw(canvas)
    }

    private fun checkOneBitmap(index: Int, bitmap: Bitmap?) {
        if (bitmap != null) {
            post {
                if (index in 0 until mBitmapList.size) {
                    mBitmapList[index] = bitmap
                    invalidate()
                }
            }
        }
    }

    override fun callBack(index: Int, bitmap: Bitmap) {
        checkOneBitmap(index, bitmap)
    }

    override fun callBack(bitmaps: MutableList<Bitmap?>) {
        mBitmapList = bitmaps
        requestLayout()
    }

    fun clear() {
        effectBean?.removeEffectListener(this)
        mBitmapList.clear()
    }

    interface OnTimeChangeLintener {
        fun timeStart(start: Boolean, end: Boolean)
        fun timeMove(start: Boolean, end: Boolean, offsetTime: Long)
        fun timeEnd(start: Boolean, end: Boolean, startTime: Long, endTime: Long)
    }
}
