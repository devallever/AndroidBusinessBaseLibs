package com.step.wincash.ui.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.step.wincash.R
import com.step.wincash.utils.dp2px

class FingerAnimationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private var fingerImageView: ImageView
    private var animationDuration: Long = 800 // 动画持续时间，单位毫秒
    private val animatorSet = AnimatorSet()

    init {
        LayoutInflater.from(context).inflate(R.layout.finger_animation_view, this, true)
        fingerImageView = findViewById(R.id.fingerImageView)
        postDelayed({
            startAnimation()
        }, 100)
    }

    /**
     * 开始动画
     */
    private fun startAnimation() {
        val dp10 = dp2px(10f).toFloat()
        // 创建 X 轴移动动画
        val translateX = ObjectAnimator.ofFloat(fingerImageView, "x", dp10, 0f)
        translateX.duration = animationDuration
        translateX.interpolator = AccelerateDecelerateInterpolator()
        translateX.repeatMode = ValueAnimator.REVERSE
        translateX.repeatCount = ValueAnimator.INFINITE

        // 创建 Y 轴移动动画
        val translateY = ObjectAnimator.ofFloat(fingerImageView, "y", dp10, 0f)
        translateY.duration = animationDuration
        translateY.interpolator = AccelerateDecelerateInterpolator()
        translateY.repeatMode = ValueAnimator.REVERSE
        translateY.repeatCount = ValueAnimator.INFINITE

        animatorSet.playTogether(translateX, translateY)
        animatorSet.start()
    }

    /**
     * 停止动画
     */
    private fun stopAnimation() {
        animatorSet.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

}