package com.example.charge.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.setVisible(visibility: Boolean) {
    if (visibility) visible() else gone()
}

fun View.setOnSingleListener(
    isSound: Boolean = true,
    isScale: Boolean = true,
    intervalTime: Long = 500L,
    callback: (v: View) -> Unit
) {
    if (isScale) {
        addClickScale()
    }

    setOnClickListener(object : OnSingleClickListener(isSound, intervalTime) {
        override fun onSingleClick(v: View) {
            callback(v)
        }
    })
}

@SuppressLint("ClickableViewAccessibility")
// 参数为：缩小比例、缩小的变化时间
fun View.addClickScale(scale: Float = 0.9f, duration: Long = 150) {
    this.setOnTouchListener { _, event ->
        if (isClickable) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    this.animate().scaleX(scale).scaleY(scale).setDuration(duration).start()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    this.animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
                }
            }
        }
        this.onTouchEvent(event)
    }
}

/**
 * 单击，防止UI卡或者机器响应慢时出现多次点击
 */
abstract class OnSingleClickListener(val isSound: Boolean = true, val intervalTime: Long = 500L) :
    View.OnClickListener {
    //最后的点击时间
    private var mLastTouchTime: Long = 0L

    override fun onClick(v: View?) {
        v ?: return
        if (isSound) {
            SoundUtil.play(SoundRawId.CLICK.id)
        }
        val time = System.currentTimeMillis()
        if (time - mLastTouchTime > intervalTime) {
            mLastTouchTime = time
            onSingleClick(v)
        }
    }

    abstract fun onSingleClick(v: View)
}