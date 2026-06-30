package com.plinkopro.wincash.utils

import android.R.attr.text
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.plinkopro.wincash.BuildConfig
import java.util.Locale
import kotlin.random.Random

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun View.setVisible(visibility: Boolean) {
    if (visibility) visible() else gone()
}

fun View.setOnSingleListener(isSound: Boolean = false, isScale: Boolean = true, intervalTime: Long = 500L ,callback: (v: View) -> Unit){
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
        if(isClickable){
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
abstract class OnSingleClickListener(val isSound: Boolean = true,  val intervalTime: Long = 500L): View.OnClickListener{
    //最后的点击时间
    private var mLastTouchTime: Long = 0L

    override fun onClick(v: View?) {
        v ?: return
        if (isSound) {
//            SoundUtil.play(SoundRawId.CLICK.id)
        }
        val time = System.currentTimeMillis()
        if (time - mLastTouchTime > intervalTime){
            mLastTouchTime = time
            onSingleClick(v)
        }
    }

    abstract fun onSingleClick(v: View)
}

//float的扩展函数，作用保留小数点后两位
fun Float.format2f(): Float {
    val df = DecimalFormat("#.##")
    df.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    return df.format(this).toFloat()
}
//float的扩展函数，作用保留小数点后4位
fun Float.format4f(): Float {
    val df = DecimalFormat("#.####")
    df.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    return df.format(this).toFloat()
}

/**
 * 将 MutableLiveData 转换为只读的 LiveData
 */
val <T> MutableLiveData<T>.asLiveData: LiveData<T>
    get() = this

fun Any.toJson(): String {
    return GsonUtil.toJson(this)
}

/**
 * 让 View 从屏幕右侧滑入并穿过屏幕左侧，整个过程匀速完成。
 *
 * @param yRangePx 进入时随机的 Y 坐标范围（像素）
 * @param durationMs 动画总时长（毫秒）
 * @param endVisibility 动画结束后的可见性：View.GONE 或 View.INVISIBLE
 */
fun View.slideAcrossScreen(
    yRangePx: IntRange,
    durationMs: Long = 2000L,
    endVisibility: Int = View.GONE
) {
    post {
        val parentView = parent as? ViewGroup ?: return@post

        val parentWidth = parentView.width
        val parentHeight = parentView.height

        // 随机一个 Y 坐标（防止越界）
        val minY = yRangePx.first.coerceAtLeast(0)
        val maxY = (yRangePx.last).coerceAtMost(parentHeight - height)
        val randomY = Random.nextInt(minY, maxY + 1).toFloat()

        // 起点 X 在屏幕右外侧，终点 X 在屏幕左外侧
        val startX = parentWidth.toFloat()
        val endX = -width.toFloat()

//        if (BuildConfig.LOG_OUTPUT) {
//            log("slideAcrossScreen: startX = $startX")
//            log("slideAcrossScreen: endX = $endX")
//        }

        // 初始化位置并显示
        x = startX
        y = randomY
        isVisible = true

        // 匀速滑动动画
        ObjectAnimator.ofFloat(this, View.X, startX, endX).apply {
            duration = durationMs
            interpolator = null // 匀速
            addListener(onEnd = {
                visibility = endVisibility
            })
            start()
        }
    }
}