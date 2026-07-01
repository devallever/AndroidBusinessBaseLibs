package com.example.charge.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.example.charge.currency.CurrencyType
import com.example.charge.utils.log

class FloatUpAnimLayout  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
//`        // 设置为不可见，因为我们只需要它作为容器，不需要显示自身
//        setWillNotDraw(false)
//        // 设置为不需要布局优化，因为我们经常添加和移除视图
//        isAnimationCacheEnabled = true`
    }


    private val viewMap = mutableMapOf<Long, View>()

    fun playFloat(value: Float, currencyType: CurrencyType) {
        val view = LayoutCreateCurrency(context)
        view.tag = System.currentTimeMillis()
        view.setValue(value, currencyType)
        viewMap[view.tag as Long] = view
        //添加到底部
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.BOTTOM or Gravity.START
        view.layoutParams = layoutParams
        addView(view)
        view.post {
            playAnim(view)
        }
    }

    private fun playAnim(view: View) {
        var animatorSet: AnimatorSet = AnimatorSet()
        // 创建动画
        // 平移动画 - 上升150像素
        val translateYAnimator = ObjectAnimator.ofFloat(view, "translationY", view.translationY, view.translationY - 150f)

        // 缩小动画 - 缩放到0.3倍
        val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f)
        val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f)

        // 透明度动画 - 渐变到0
        val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)

        val duration = 3000L

        // 配置动画
        translateYAnimator.duration = duration
        scaleXAnimator.duration = duration
        scaleYAnimator.duration = duration
        alphaAnimator.duration = duration

        // 设置插值器
        val interpolator = LinearInterpolator()
        translateYAnimator.interpolator = interpolator
        scaleXAnimator.interpolator = interpolator
        scaleYAnimator.interpolator = interpolator
        alphaAnimator.interpolator = interpolator

        // 组合动画
        animatorSet.playTogether(translateYAnimator, scaleXAnimator, scaleYAnimator, alphaAnimator)

        // 设置动画监听器
        animatorSet?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                viewMap.remove(view.tag as Long)
//                log("viewMap.size = ${viewMap.size}")
            }

            override fun onAnimationCancel(animation: Animator) {
                viewMap.remove(view.tag as Long)
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })

        // 启动动画
        animatorSet.start()
    }

}