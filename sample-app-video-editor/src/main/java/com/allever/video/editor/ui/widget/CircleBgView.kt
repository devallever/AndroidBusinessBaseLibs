package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import android.util.AttributeSet
import android.view.View

import com.android.absbase.App
import com.android.absbase.utils.DeviceUtils


class CircleBgView : View {

    private var mStrokePaint: Paint? = null
    @ColorInt
    private var mNormalColor: Int = 0
    @ColorInt
    private var mCheckedColor: Int = 0
    private var mChecked: Boolean = false
    private var mFillStyle: Boolean = false

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    fun setColor(@ColorRes normalColor: Int, @ColorRes checkedColor: Int) {
        mNormalColor = App.getContext().resources.getColor(normalColor)
        mCheckedColor = App.getContext().resources.getColor(checkedColor)
    }

    override fun onAttachedToWindow() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        super.onAttachedToWindow()
        checkPaint()
    }

    private fun checkPaint() {
        if (mStrokePaint == null) {
            mStrokePaint = Paint()
            mStrokePaint!!.color = mNormalColor
            mStrokePaint!!.strokeJoin = Paint.Join.ROUND
            mStrokePaint!!.strokeWidth = STROKE_PAINT_WIDTH.toFloat()
            mStrokePaint!!.style = Paint.Style.STROKE
            mStrokePaint!!.isAntiAlias = true
        }
    }

    fun setChecked(checked: Boolean) {
        mChecked = checked
        invalidate()
    }

    fun setFillStyle(fillStyle: Boolean) {
        mFillStyle = fillStyle
        checkPaint()
        if (fillStyle) {
            mStrokePaint!!.style = Paint.Style.FILL
        } else {
            mStrokePaint!!.style = Paint.Style.STROKE
        }
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (mChecked) {
            mStrokePaint!!.color = mCheckedColor
        } else {
            mStrokePaint!!.color = mNormalColor
        }
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        if (mFillStyle) {
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(),
                    (width / 2).toFloat(), mStrokePaint!!)
        } else {
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(),
                    (width / 2).toFloat() - (STROKE_PAINT_WIDTH / 2).toFloat() - 0.5f, mStrokePaint!!)
        }

    }

    companion object {
        val STROKE_PAINT_WIDTH = DeviceUtils.dip2px(App.getContext(), 3f)
    }
}
