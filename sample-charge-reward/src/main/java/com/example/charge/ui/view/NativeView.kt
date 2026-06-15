package com.example.charge.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import com.example.charge.R
import com.example.charge.databinding.ViewNativeBinding
import com.example.charge.init.Constance
import com.example.charge.ui.activity.WebActivity
import com.example.charge.utils.NativeUtil
import com.example.charge.utils.gone
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.setVisible
import com.example.charge.utils.visible
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager

class NativeView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attributeSet, defStyle) {

    private var animationDuration: Long = 800 // 动画持续时间，单位毫秒
    private var animatorSet = AnimatorSet()

    private val binding: ViewNativeBinding =
        ViewNativeBinding.inflate(LayoutInflater.from(context), this)

    private val nativeList by lazy {
        listOf(
            binding.icon1CL,
            binding.icon2CL,
            binding.icon3CL,
            binding.icon4CL
        )
    }

    private val nativeBtList by lazy {
        listOf(
            binding.bt1Img,
            binding.bt2Img,
            binding.bt3Img,
            binding.bt4Img
        )
    }

    fun initView(activity: Activity) {
        binding.apply {
            val clickSize = NativeUtil.getNativeClickSize()
            if (clickSize < 3) {
                iconCl.visible()
                bannerFl.gone()
                val index = NativeUtil.getNativeImage()
                showNativeIcon(index)
                startAnimation(nativeBtList[index])
                iconCl.setOnSingleListener {
                    WebActivity.start(context, Constance.OKSPIN_URL)
                    NativeUtil.addNativeClickSize()
                    initView(activity)
                }
            } else {
                iconCl.gone()
                bannerFl.visible()
                stopAnimation()
                binding.root.postDelayed({
                    val isLoad = AdManager.showNative(activity, bannerFl, R.mipmap.ic_launcher) {
                        initView(activity)
                    }
                    if (!isLoad) {
                        AdManager.loadNative(activity)
                    }
                }
                ,1000
                )
            }
        }
    }


    fun startAnimation(view: View) {
        if (animatorSet.isRunning) animatorSet.cancel()

        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f)

        scaleX.repeatCount = ValueAnimator.INFINITE
        scaleX.repeatMode = ValueAnimator.REVERSE
        scaleY.repeatCount = ValueAnimator.INFINITE
        scaleY.repeatMode = ValueAnimator.REVERSE

        animatorSet = AnimatorSet().apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(scaleX, scaleY)
            start()
        }
    }

    fun showNativeIcon(index: Int) {
        nativeList.forEachIndexed { position, layout ->
            layout.setVisible(index == position)
        }
    }

    fun stopAnimation() {
        if (animatorSet.isRunning) animatorSet.cancel()
    }

    fun getBinding() = binding

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
        AdManager.destroyNative()
    }

}