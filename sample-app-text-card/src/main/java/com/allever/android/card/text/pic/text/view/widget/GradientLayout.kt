package com.allever.android.card.text.pic.text.view.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class GradientLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (mColorList.isEmpty()) {
            return
        }
        background = GradientBackgroundDrawable().apply {
            setColorList(mColorList, w.toFloat(), h.toFloat())
        }
    }

    private var mColorList = mutableListOf<String>()
    fun setColorList(color: List<String>) {
        mColorList.clear()
        mColorList.addAll(color)
        postInvalidate()
    }
}