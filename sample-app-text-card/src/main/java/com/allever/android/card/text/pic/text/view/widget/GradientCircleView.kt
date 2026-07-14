package com.allever.android.card.text.pic.text.view.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader.TileMode
import android.util.AttributeSet
import android.util.Log
import android.view.View

class GradientCircleView(
    context: Context?,
    attrs: AttributeSet?
) : View(context, attrs) {

    private var mSize = 0
    private val mPaint by lazy {
        Paint().apply {
            isAntiAlias = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mSize = measuredWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(mSize / 2f, mSize / 2f, mSize / 2f, mPaint)
    }

    fun setColor(color: String) {
        mPaint.color = Color.parseColor(color)
        postInvalidate()
    }

    fun setColorList(color: List<String>) {
        post {
            val colorList = mutableListOf<Int>()
            color.map {
                colorList.add(Color.parseColor(it))
            }
            val shader = LinearGradient(
                measuredWidth.toFloat(),
                0f,
                measuredWidth.toFloat() * 0.1f,
                measuredWidth.toFloat() * 0.9f,
                colorList.toIntArray(),
                null,
                TileMode.CLAMP
            )
            mPaint.setShader(shader)
            postInvalidate()
        }
    }

    private fun log(msg: String) {
        Log.d("GiantCircleView", msg)
    }
}