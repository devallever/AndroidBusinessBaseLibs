package com.example.charge.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.constant.FloatIconType
import com.example.charge.data.FloatIconData
import com.example.charge.databinding.ActivityChargeMainBinding
import com.example.charge.databinding.ActivityMainBinding
import com.example.charge.init.InitManager
import kotlin.random.Random

class FloatIconHelper {

    //TAG
    private val TAG = "FloatIconHelper"

    private val animSet = AnimatorSet()
    private val list = mutableListOf<View>()

    fun getViewIndex(view: View): Int {
        return list.indexOf(view)
    }
    @SuppressLint("StaticFieldLeak")
    private var homeBinding: ActivityChargeMainBinding? = null

    private var timer: CustomTimer? = null

    fun startFloatAnimation(views: List<View>, homeBinding: ActivityChargeMainBinding, block: (target: View)-> Unit) {
        val firstShow = SpUtil.get(SpKey.FIRST_SHOW_FLOAT_ICON, true)
        SpUtil.put(SpKey.FIRST_SHOW_FLOAT_ICON, false)
        this.homeBinding = homeBinding
        list.forEach {
            it.tag = null
        }
        list.clear()
        list.addAll(views)
        val dp5 = dp2px(5f).toFloat()
        list.forEachIndexed { index, view ->
            if (index == list.lastIndex) {
                view.isVisible = false
            }

            if (firstShow) {
                view.tag = FloatIconData.generateNoAd()
            } else {
                view.tag = FloatIconData.generate()
            }

            if (App.DEBUG) {
                log("气泡View: ${view.hashCode()} ->tag ${view.tag.toJson()}")
            }
            updateFloatIcon(view.tag as FloatIconData, index)

            view.setOnSingleListener {
                it.isVisible = false
                val floatIconData = it.tag as FloatIconData
                if (App.DEBUG) {
                    log(TAG,"点击气泡: ${view.hashCode()}, index = ${index}, data = ${view.tag.toJson()}")
                }
                updateFloatIcon(floatIconData, list.indexOf(view))
                timer?.reset()?.start()
                block.invoke(it)
            }
            val animator = if (Random.nextBoolean()) {
                ValueAnimator.ofFloat(0f, -dp5, 0f, dp5)
            } else {
                ValueAnimator.ofFloat(0f, dp5, 0f, -dp5)
            }
            animator.duration = 3000
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener { animation: ValueAnimator ->
                val value = animation.animatedValue as Float
                view.translationY = value
            }
            animator.startDelay = (100..800).random().toLong()
            animSet.playTogether(animator)
        }

        if (animSet.childAnimations.size == views.size) {
            animSet.start()
        }

        timer = CustomTimer()
            .setInterval(20 * 1000L)
            .setTickCallback {
                list.forEachIndexed { index, view ->
                    if (!view.isVisible) {
                        view.isVisible = true
                        view.tag = FloatIconData.generate()
                        updateFloatIcon(view.tag as FloatIconData, index)
                        if (App.DEBUG) {
                            log(TAG,"更新气泡: ${view.hashCode()}, index = ${index}, data = ${view.tag.toJson()}")
                        }
                        return@setTickCallback
                    }
                }
                if (App.DEBUG) {
                    log(TAG,"tick: $it")
                }
            }
            .start()
    }

    fun destroy() {
        homeBinding = null
        timer?.release()
        list.forEach {
            it.tag = null
        }
        list.clear()
    }

    @SuppressLint("SetTextI18n")
    private fun updateFloatIcon(data: FloatIconData, index: Int) {
        val ivIcon = when (index) {
            0 -> homeBinding?.ivFloatIcon1
            1 -> homeBinding?.ivFloatIcon2
            2 -> homeBinding?.ivFloatIcon3
            3 -> homeBinding?.ivFloatIcon4
            else -> null
        }
        val imageRes = when (data.type) {
            FloatIconType.GOLD -> R.drawable.ic_float_gold
            FloatIconType.GREEN -> R.drawable.ic_float_green
            FloatIconType.SPEED -> R.drawable.ic_float_speed_up
            FloatIconType.GREEN_AD -> R.drawable.ic_float_green
            else -> R.drawable.ic_gold
        }
        ivIcon?.setImageResource(imageRes)

        val tvGoldValue = when (index) {
            0 -> homeBinding?.tvFloatGoldValue1
            1 -> homeBinding?.tvFloatGoldValue2
            2 -> homeBinding?.tvFloatGoldValue3
            3 -> homeBinding?.tvFloatGoldValue4
            else -> null
        }
        tvGoldValue?.text = "x${data.value.toInt()}"
        tvGoldValue?.isVisible = data.type == FloatIconType.GOLD

        val tvGreenValue = when (index) {
            0 -> homeBinding?.tvFloatGreenValue1
            1 -> homeBinding?.tvFloatGreenValue2
            2 -> homeBinding?.tvFloatGreenValue3
            3 -> homeBinding?.tvFloatGreenValue4
            else -> null
        }
        tvGreenValue?.text = "$${data.value}"
        tvGreenValue?.isVisible = data.type == FloatIconType.GREEN || data.type == FloatIconType.GREEN_AD

        val ivAd = when (index) {
            0 -> homeBinding?.ivFloatAd1
            1 -> homeBinding?.ivFloatAd2
            2 -> homeBinding?.ivFloatAd3
            3 -> homeBinding?.ivFloatAd4
            else -> null
        }
        val showAd = data.type == FloatIconType.GREEN_AD
        ivAd?.isVisible = showAd

//        val progressBar = when (index) {
//            0 -> homeBinding?.floatProgress1
//            1 -> homeBinding?.floatProgress2
//            2 -> homeBinding?.floatProgress3
//            3 -> homeBinding?.floatProgress4
//            else -> null
//        }
//        progressBar?.isVisible = data.type == FloatIconType.SPEED
    }

//    fun startCircleProgressBarAnim(targetView: View) {
//        val index = getViewIndex(targetView)
//        if (index == -1) {
//            return
//        }
//        SpeedUpHelper.speedUpFloatIcon()
//        targetView.isVisible = true
//        val valueAnimator = ValueAnimator.ofInt(100, 0)
//        valueAnimator.duration = 30 * 1000
//        valueAnimator.interpolator = LinearInterpolator()
//        val progressBar = when (index) {
//            0 -> homeBinding?.floatProgress1
//            1 -> homeBinding?.floatProgress2
//            2 -> homeBinding?.floatProgress3
//            3 -> homeBinding?.floatProgress4
//            else -> null
//        }
//        valueAnimator.doOnEnd {
//            targetView.isVisible = false
//            targetView.isEnabled = true
//            targetView.isClickable = true
//            progressBar?.progress = 100f
//            SpeedUpHelper.reduceGoldValue()
//        }
//        valueAnimator.addUpdateListener {
//            val progress = it.animatedValue as Int
//            progressBar?.progress = progress.toFloat()
//        }
//        valueAnimator.start()
//        targetView.isClickable = false
//        targetView.isEnabled = false
//
//    }
}