package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View

class ColorView : View {

    @ColorInt
    @get:ColorInt
    var color: Int = 0
        private set
    private var mChecked: Boolean = false

    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mUncheckedRadius: Int = 0
    private var mCheckedOuterRadius: Int = 0
    private var mCheckedOuterWidth: Int = 0
    private var mCheckedInsideRadius: Int = 0

    constructor(context: Context) : this(context, null) {
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val x = (width / 2).toFloat()
        val y = (height / 2).toFloat()

        if (mChecked) {
            mPaint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, mCheckedInsideRadius.toFloat(), mPaint)
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = mCheckedOuterWidth.toFloat()
            canvas.drawCircle(x, y, (mCheckedOuterRadius - mCheckedOuterWidth / 2).toFloat(), mPaint)
        } else {
            mPaint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, mUncheckedRadius.toFloat(), mPaint)
        }
    }

    fun setRadius(uncheckedRadius: Int, checkedOuterRadius: Int, checkedOuterWidth: Int, checkedInsideRadius: Int) {
        mUncheckedRadius = uncheckedRadius
        mCheckedOuterRadius = checkedOuterRadius
        mCheckedOuterWidth = checkedOuterWidth
        mCheckedInsideRadius = checkedInsideRadius
    }

    fun setColor(@ColorInt color: Int, @ColorInt showColor: Int = color) {
        this.color = color
        mPaint.color = showColor
        invalidate()
    }

    fun setChecked(checked: Boolean) {
        mChecked = checked
        invalidate()
    }
}
