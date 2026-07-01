package com.allever.video.editor.ui.widget

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

import com.airbnb.lottie.LottieAnimationView
import com.allever.video.editor.R

/**
 *
 */

class LoadingView : FrameLayout, View.OnTouchListener {
    private lateinit var mAnimView: LottieAnimationView

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        mAnimView = findViewById(R.id.anim_view)
        setOnTouchListener(this)
    }

    private val listener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            if (visibility == View.GONE) {
                return
            }
            mAnimView.setAnimation("anim/loading/loading_2.json")
            mAnimView.loop(true)
            mAnimView.playAnimation()
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }

    fun startLoading() {
        stopLoading()
        visibility = View.VISIBLE
        mAnimView.addAnimatorListener(listener)
        mAnimView.setAnimation("anim/loading/loading_1.json")
        mAnimView.loop(false)
        mAnimView.playAnimation()
    }

    fun stopLoading() {
        visibility = View.GONE
        mAnimView.removeAnimatorListener(listener)
        mAnimView.cancelAnimation()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return true
    }
}
