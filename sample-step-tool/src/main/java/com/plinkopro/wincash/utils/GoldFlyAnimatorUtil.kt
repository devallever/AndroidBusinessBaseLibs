package com.plinkopro.wincash.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.plinkopro.wincash.R
import com.plinkopro.wincash.beans.CurrencyType
import com.plinkopro.wincash.event.UpdateCurrencyEvent
import com.plinkopro.wincash.ui.dialog.GoldFlyAnimaDialog
import com.plinkopro.wincash.ui.widget.CurrencyView
import com.plinkopro.wincash.utils.CurrencyUtils.updateCurrencyNum
import org.greenrobot.eventbus.EventBus

object GoldFlyAnimatorUtil {
    /**
     * @param context 当前页面context
     * @param currencyView  金币绿纱控件
     * @param rootView  金币绿钞控件所在页面布局的根视图
     * @param currencyType  金币类型
     * @param amount 增加的金额
     * @param animationEndCallback 回调
     * */
    fun start(
        context: Context,
        currencyView: CurrencyView,
        rootView: ViewGroup,
        currencyType: CurrencyType,
        amount: Float,
        animationEndCallback: (() -> Unit)? = null
    ) {
        if (context is Activity) {
            val targetView: View
            val resId: Int
            when (currencyType) {
                CurrencyType.GOLD -> {
                    targetView = currencyView.getBinding().flGold
                    resId = R.drawable.ic_gold
                }

                else -> {
                    targetView = currencyView.getBinding().flBanknote
                    resId = R.drawable.ic_green
                }
            }

            val oldCount = CurrencyUtils.getCurrencyNum(currencyType)
            updateCurrencyNum(currencyType, amount)
            val newCount = CurrencyUtils.getCurrencyNum(currencyType)

            //播放金币上飞动画
            val goldFlyAnimaDialog = GoldFlyAnimaDialog(
                context, currencyView.rootView.findViewById(android.R.id.content)!!, 10,
                targetView, resId
            ) {

                val goalView: View = when (currencyType) {
                    CurrencyType.GOLD -> {
                        currencyView.getBinding().tvGold
                    }
                    else -> {
                        currencyView.getBinding().tvBanknote
                    }
                }
                //自增漂浮文案
                floatTextAnimator(
                    rootView,
                    goalView,
                    Color.parseColor("#2EA340"),
                    amount.formThousand(),
                    animationEndCallback
                )
                if (currencyType == CurrencyType.GOLD) {
                    currencyView.updateGoldAnima(oldCount.toDouble(), newCount.toDouble())
                } else {
                    currencyView.updateGreenAnima(oldCount.toDouble(), newCount.toDouble())
                }

                EventBus.getDefault().post(UpdateCurrencyEvent(currencyType, currencyView))
            }

            PopupHelper.createDialog(context, goldFlyAnimaDialog, hasShadowBg = false).show()

            SoundUtil.play(R.raw.get_reward)
        }
    }


    private fun floatTextAnimator(
        group: ViewGroup,
        target: View,
        textColor: Int,
        goldCount: String?,
        animationEndCallback: (() -> Unit)? = null
    ) {
        val location = IntArray(2)
        target.getLocationInWindow(location)
        val targetX = (location[0]).toFloat()
        val targetY = (location[1]).toFloat()
        val layoutParams = ViewGroup.LayoutParams(target.width, ViewGroup.LayoutParams.WRAP_CONTENT)
        val textView = TextView(group.context).apply {
            this.text = "+$goldCount"

            this.x = targetX
            this.y = targetY
            this.textSize = 15f
            this.gravity = Gravity.CENTER
            this.setTextColor(textColor)

        }
        group.addView(textView, layoutParams)
        val y = ObjectAnimator.ofFloat(textView, "y", targetY, targetY - 50)
        val alpha = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(y, alpha)
        animatorSet.setDuration(800)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animationEndCallback?.invoke()
                group.removeView(textView)
            }
        })
        animatorSet.start()
    }
}