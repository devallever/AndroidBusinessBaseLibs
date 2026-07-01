package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.R
import com.allever.video.editor.utils.TimeUtils
import com.allever.video.editor.utils.rectCenterExpansion

class SoundTrimView : View {

    private lateinit var paint: Paint
    private lateinit var textPaint: Paint
    private lateinit var gestureDetector: GestureDetector
    private val mainToneColor = resources.getColor(R.color.main_color_tone)
    private var selectTimeColor = resources.getColor(R.color.video_edit_bottom_music_trim_select_time_color)
    private val noselectTimeColor = resources.getColor(R.color.video_edit_bottom_music_trim_noselect_time_color)
    private val arrowLeftDrawable = resources.getDrawable(R.drawable.arrow_left)
    private val arrowRightDrawable = resources.getDrawable(R.drawable.arrow_right)
    private val arrowWidth = arrowLeftDrawable.intrinsicWidth
    private val arrowHeight = arrowLeftDrawable.intrinsicHeight
    private val marginLeftAndRight = DeviceUtils.dip2pxF(20f)
    private val paddingInterval = DeviceUtils.dip2pxF(2f)
    private val trackLineWidth = DeviceUtils.dip2pxF(2f)
    private val selectWidth = DeviceUtils.dip2pxF(15f)
    private val timeHeight = DeviceUtils.dip2pxF(13f)
    private val triangleHeight = DeviceUtils.dip2pxF(2f)
    private val intervalHeight = DeviceUtils.dip2pxF(3f)
    private val selectPadding = DeviceUtils.dip2pxF(3f)
    private val timeSize = DeviceUtils.dip2pxF(8f)
    private val timeColor = ResourcesUtils.getColor(R.color.video_edit_bottom_music_trim_time_text_color)
    private val timeTextPadding = DeviceUtils.dip2pxF(3f)
    private val heights = arrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private val minHeight = DeviceUtils.dip2pxF(4f)
    private val maxHeight = DeviceUtils.dip2pxF(24f)
    private var originalStartOffset = 0f
    private var originalEndOffset = 0f
    private var startMaxOffset = 0f
    private var endMinOffset = 0f

    private var startSelectDrawRect = RectF()
    private var endSelectDrawRect = RectF()
    private var startSelectRect = RectF()
    private var endSelectRect = RectF()
    private var timeRect = RectF()
    private var trianglePath = Path()
    private var speed = (paddingInterval + trackLineWidth) * 9 / 1000

    var onTimeChangeLintener: OnTimeChangeLintener? = null

    var startOffset = 0f
    var endOffset = 0f

    var needEdit = true

    var selectHeight = DeviceUtils.dip2pxF(30f)

    var arrowColor = ResourcesUtils.getColor(R.color.main_color_tone)
        set(value) {
            field = value
            arrowLeftDrawable.setColorFilter(value, PorterDuff.Mode.SRC_IN)
            arrowRightDrawable.setColorFilter(value, PorterDuff.Mode.SRC_IN)
        }

    var duration: Long = 0
        set(value) {
            field = value
            originalStartOffset = marginLeftAndRight
            originalEndOffset = marginLeftAndRight + speed * duration
            startOffset = originalStartOffset
            endOffset = originalEndOffset
            startMaxOffset = originalEndOffset - speed * minDuration
            endMinOffset = originalStartOffset + speed * minDuration
            requestLayout()
        }

    var startTime: Long = 0
        set(value) {
            field = value
            startOffset = time2distance(value) + marginLeftAndRight
            invalidate()
        }
    var endTime: Long = 0
        set(value) {
            field = value
            endOffset = time2distance(value) + marginLeftAndRight
            invalidate()
        }

    var minDuration: Int = 1000
        set(value) {
            field = value
            startMaxOffset = endOffset - speed * value
            endMinOffset = startOffset + speed * value
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
        paint = Paint()
        paint.strokeWidth = trackLineWidth

        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = timeSize
        textPaint.color = timeColor

        startSelectDrawRect.set(0f, 0f, selectWidth, selectHeight)
        endSelectDrawRect.set(0f, 0f, selectWidth, selectHeight)
        timeRect.set(0f, 0f, 0f, timeHeight)

        for (i in 0 until 5) {
            heights[i] = ((maxHeight - minHeight / 2) * i * (paddingInterval + trackLineWidth) / ((paddingInterval + trackLineWidth) * 4) * 2 + minHeight) / 2
            heights[9 - 1 - i] = heights[i]
        }

        arrowColor = ResourcesUtils.getColor(R.color.main_color_tone)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = if (needEdit) {
            val oneSecondWidth = (trackLineWidth + paddingInterval) * 9
            marginLeftAndRight * 2 + oneSecondWidth * duration / 1000
        } else {
            widthMeasureSpec.toFloat()
        }
        val height = if (needEdit) {
            timeHeight + triangleHeight + intervalHeight + selectHeight
        } else {
            selectHeight
        }
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    private var prevX = 0f
    private var prevY = 0f
    private var selectLeft = false
    private var selectRight = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!needEdit) {
            return super.onTouchEvent(event)
        }

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
            MotionEvent.ACTION_UP -> {
                if (selectLeft || selectRight) {
                    onTimeChangeLintener?.timeEnd(selectLeft, selectRight)
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas ?: return

        paint.color = mainToneColor
        // 画开始时间
        if (needEdit) {
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
        if (needEdit) {
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
        val left = if (needEdit) {
            marginLeftAndRight
        } else 0f
        val startCenterX = startSelectDrawRect.centerX()
        val startCenterY = if (needEdit) {
            startSelectDrawRect.centerY()
        } else height.toFloat() / 2
        val endCenterX = endSelectDrawRect.centerX()
        val endCenterY = endSelectDrawRect.centerY()
        for (i in 0 until (duration.toFloat() / 1000 * 9 + 1).toInt()) {
            val height = heights[i % 9]
            val startX = left + i * (paddingInterval + trackLineWidth)
            val startY = startCenterY - height / 2
            val stopY = startY + height
            if (!needEdit || startX > startOffset && startX < endOffset) {
                paint.color = selectTimeColor
            } else {
                paint.color = noselectTimeColor
            }
            canvas.drawLine(startX, startY, startX, stopY, paint)
        }
        if (needEdit) {
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
    }

    fun setSelectTimeColor(color: Int) {
        selectTimeColor = color
        invalidate()
    }

    interface OnTimeChangeLintener {
        fun timeStart(start: Boolean, end: Boolean)
        fun timeEnd(start: Boolean, end: Boolean)
        fun timeMove(start: Boolean, end: Boolean, offsetTime: Long)
    }
}
