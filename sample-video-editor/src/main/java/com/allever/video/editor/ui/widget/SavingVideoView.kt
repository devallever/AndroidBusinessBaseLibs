package com.allever.video.editor.ui.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.android.absbase.App
import com.android.absbase.utils.ResourcesUtils
import com.android.absbase.utils.TaskRunnable
import com.allever.video.editor.R
import com.allever.video.editor.utils.FontUtil
import java.util.*


/**
 *
 */

class SavingVideoView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var mAnimationView: LottieAnimationView
    private lateinit var mProgressText: TextView
    private var mBgView: ImageView? = null
    private var mBtnCancel: View? = null
    private var mCurrentProgress: Int = 0
    private var mProgressAnimator: ValueAnimator? = null

    var CRITICAL_PROGRESS = 80

    private val listener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {

        }

        @SuppressLint("WrongConstant")
        override fun onAnimationEnd(animation: Animator) {
            val composition = LottieCompositionFactory.fromAssetSync(App.getContext(), "anim/save_loading/save_loading_02.json").value
            composition.let {
                mAnimationView.setComposition(it!!)
                mAnimationView.repeatMode = LottieDrawable.INFINITE
                mAnimationView.playAnimation()
            }
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }
    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        LayoutInflater.from(context).inflate(R.layout.layout_video_saving, this)

        mAnimationView = findViewById(R.id.animator_view)
        mAnimationView.addAnimatorListener(listener)
        mProgressText = findViewById(R.id.progress_text)
        mBgView = findViewById(R.id.bg_view)
        mBtnCancel = findViewById(R.id.btn_cancel)
        mBtnCancel?.visibility = View.GONE
        val tvTips = findViewById<TextView>(R.id.tips)
        mProgressText.typeface = FontUtil.CUSTOM_FONT

        tvTips.text = resources.getText(R.string.video_edit_saving_tips)
    }

    fun setCancelClickListener(listener: View.OnClickListener) {
        mBtnCancel?.setOnClickListener(listener)
    }

    /***
     * @param transitionDuration 过渡值
     * @param totalDuration 总时长
     */
    fun startProgressSmooth(totalDuration: Long, transitionDuration: Long = 5_000) {
        // 关键位置80-90，之前会很快，大概5秒钟，到达关键位置后如果还没完成则调整速度
        val random = Random()
        CRITICAL_PROGRESS = random.nextInt(10) + 80

        val runnable = Runnable {
            val duration = if (totalDuration > transitionDuration) totalDuration - transitionDuration else totalDuration
            //最大值随机从90-99中取
            val endProgress = random.nextInt(10) + 90
            smoothChangeProgress2(mCurrentProgress, endProgress, duration, null)
        }

        smoothChangeProgress2(mCurrentProgress, CRITICAL_PROGRESS, totalDuration, runnable)
    }

    fun endProgressSmooth(runnable: Runnable) {
        smoothChangeProgress2(mCurrentProgress, 100, 2000, runnable)
    }

    fun setProgress(progress: Int) {
        if (progress < mCurrentProgress || progress > 100) {
            return
        }
        TaskRunnable.run(Runnable {
            mCurrentProgress = progress
            mProgressText.text = "${ResourcesUtils.getText(R.string.video_edit_saving_loading_text)}$progress%"
        }, 0, TaskRunnable.TYPE_MAIN)
        if (progress == 100) {
            cancelProgressAnimator()
        }
    }

    private fun cancelProgressAnimator() {
        mProgressAnimator?.cancel()
        mProgressAnimator?.removeAllUpdateListeners()
        mProgressAnimator?.removeAllListeners()
    }

    private fun smoothChangeProgress2(start: Int, end: Int, duration: Long, runnable: Runnable?) {
        cancelProgressAnimator()
        val progressAnimator = ValueAnimator.ofInt(start, end)
        mProgressAnimator = progressAnimator
        progressAnimator.duration = duration
        progressAnimator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            setProgress(progress)
        }
        progressAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                runnable?.run()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })
        post { progressAnimator.start() }
    }

    fun startAnimation() {
        visibility = View.VISIBLE
        mCurrentProgress = 0
        mAnimationView.setAnimation("anim/save_loading/save_loading_01.json")
        mAnimationView.loop(false)
        mAnimationView.playAnimation()
//        post { mAnimationView.playAnimation() }
    }

    fun  stopAnimation() {
        visibility = View.GONE
        mAnimationView.cancelAnimation()
        cancelProgressAnimator()
    }

    fun setBackgroundBitmap(bitmap: Bitmap) {
        mBgView?.setImageBitmap(bitmap)
    }

    override fun setBackgroundColor(color: Int) {
        mBgView?.setImageBitmap(null)
        mBgView?.setBackgroundColor(color)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAnimationView.cancelAnimation()
        cancelProgressAnimator()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    fun destroy() {
    }

}
