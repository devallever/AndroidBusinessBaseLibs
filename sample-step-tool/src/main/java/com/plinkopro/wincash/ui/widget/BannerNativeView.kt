package com.plinkopro.wincash.ui.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import com.plinkopro.wincash.R
import com.plinkopro.wincash.databinding.ViewBannerNativeBinding
import com.plinkopro.wincash.event.ChangeShowPage
import com.plinkopro.wincash.init.Constance
import com.plinkopro.wincash.ui.activity.WebActivity
import com.plinkopro.wincash.utils.BannerNativeUtil
import com.plinkopro.wincash.utils.gone
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible
import gjofg.frytfkrqy.hxrdk.gddrjgra.admob.AdManager
import org.greenrobot.eventbus.EventBus

class BannerNativeView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    LinearLayout(context, attributeSet, defStyle) {

    private var animationDuration: Long = 800 // 动画持续时间，单位毫秒
    private var animatorSet = AnimatorSet()

    private val binding: ViewBannerNativeBinding =
        ViewBannerNativeBinding.inflate(LayoutInflater.from(context), this)

    fun initView(type: ViewType, activity: Activity) {
        binding.apply {

            val clickSize =  BannerNativeUtil.getBannerClickSize()
            if (clickSize < 3) {
                iconCl.visible()
                bannerFl.gone()
                startAnimation()
                val icon =  BannerNativeUtil.getBannerImage()
                btImg.setImageResource(icon.second)
                bgImg.apply {
                    setImageResource(icon.first)
                    btImg
                    visible()
                    setOnSingleListener {
                        EventBus.getDefault().post(ChangeShowPage(3))
                        BannerNativeUtil.addBannerClickSize()
                        initView(type, activity)
                    }
                }
            } else {
                iconCl.gone()
                bannerFl.visible()
                stopAnimation()
                if (type == ViewType.BANNER) {
                    AdManager.showBanner(bannerFl) {
                        AdManager.showBanner(bannerFl) {}
                    }
                } else {
                    val isLoad = AdManager.showNative(activity, bannerFl, R.mipmap.ic_launcher) {
                        initView(type, activity)
                    }
                    if (!isLoad) {
                        AdManager.loadNative(activity)
                    }
                }
            }
        }
    }


    fun startAnimation() {
        if (animatorSet.isRunning) animatorSet.cancel()

        val scaleX = ObjectAnimator.ofFloat(binding.btImg, "scaleX", 1.0f, 1.1f)
        val scaleY = ObjectAnimator.ofFloat(binding.btImg, "scaleY", 1.0f, 1.1f)

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

    fun stopAnimation() {
        if (animatorSet.isRunning) animatorSet.cancel()
    }

    fun getBinding() = binding

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    enum class ViewType {
        BANNER,
        NATIVE
    }

}