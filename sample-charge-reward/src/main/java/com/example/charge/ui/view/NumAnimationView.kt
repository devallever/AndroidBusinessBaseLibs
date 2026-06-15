package com.example.charge.ui.view

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.example.charge.R
import com.example.charge.utils.dp2px
import com.example.charge.utils.gone
import com.example.charge.utils.visible

class NumAnimationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private var numTextView: TextView
    private var animationDuration: Long = 500 // 动画持续时间，单位毫秒
    private var translateY = ObjectAnimator()
    var endCallback : () -> Unit = {}

    init {
        LayoutInflater.from(context).inflate(R.layout.view_num_anim, this, true)
        numTextView = findViewById(R.id.numTv)
        getAnimationView(0f,  -dp2px(40f).toFloat() , animationDuration)
    }

    /**
     * 开始动画
     */
    fun startAnimation(endCallback : () -> Unit) {
        this.endCallback = endCallback
        translateY.start()
    }

    fun getAnimationView(start : Float , end : Float , mDuration : Long) : ObjectAnimator{
        translateY = ObjectAnimator.ofFloat(numTextView, "translationY", start,  end).apply {
            duration = mDuration
            interpolator = AccelerateDecelerateInterpolator()
            doOnStart {
                numTextView.visible()
            }
            doOnEnd {
                numTextView.gone()
                endCallback.invoke()
            }
        }
        return translateY
    }
    /**
     * 停止动画
     */
    private fun stopAnimation() {
        translateY.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

}