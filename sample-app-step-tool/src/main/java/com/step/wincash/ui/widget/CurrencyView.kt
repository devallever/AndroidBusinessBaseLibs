package com.step.wincash.ui.widget

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import app.allever.android.lib.core.app.App
import com.step.wincash.BuildConfig
import com.step.wincash.beans.CurrencyType
import com.step.wincash.databinding.ViewCurrencyBinding
import com.step.wincash.event.UpdateCurrencyEvent
import com.step.wincash.utils.CurrencyUtils
import com.step.wincash.utils.formThousand
import com.step.wincash.utils.setOnSingleListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CurrencyView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null, defStyle: Int = 0) :
    ConstraintLayout(context, attributeSet, defStyle) {

    private val binding: ViewCurrencyBinding = ViewCurrencyBinding.inflate(LayoutInflater.from(context), this)
    private var goldChangeAnimator: ValueAnimator? = null
    private var greenChangeAnimator: ValueAnimator? = null
    private var onCoinClickCallback: ((coinType: Int) -> Unit)? = null

    init {

        EventBus.getDefault().register(this)
        refreshData()
        binding.tvGold.setOnSingleListener {
            onCoinClickCallback?.invoke(CurrencyType.GOLD.type)
        }
        binding.tvBanknote.setOnSingleListener {
            onCoinClickCallback?.invoke(CurrencyType.GREEN.type)
        }
        if(App.DEBUG){
            binding.flGold.setOnLongClickListener { view ->
                handleGoldDebug(CurrencyType.GOLD)
                false
            }
            binding.flBanknote.setOnLongClickListener { view ->
                handleGoldDebug(CurrencyType.GREEN)
                false
            }
        }
    }

    private fun handleGoldDebug(currencyType: CurrencyType) {
        if (App.DEBUG) {
            val editText = EditText(context)
            editText.setTextColor(Color.BLACK)
            editText.hint = "Input count"
            AlertDialog.Builder(context).setTitle("Input count")
                .setView(editText)
                .setPositiveButton("Confirm", { dialog, which ->
                    val number = editText.text?.toString()?.toDoubleOrNull()
                    if (number != null) {
                        CurrencyUtils.updateCurrencyNum(currencyType, number.toFloat())
                        refreshData()
                        EventBus.getDefault().post(UpdateCurrencyEvent(currencyType, this))
                    }
                    dialog.dismiss()
                })
                .setNegativeButton("Cancel", { dialog, which ->
                    dialog.dismiss()
                }).show()
        }
    }

    fun getBinding() = binding

    fun setOnCoinClickCallback(onCoinClickCallback: (coinType: Int) -> Unit) {
        this.onCoinClickCallback = onCoinClickCallback
    }

    private fun canNotClick(): Boolean {
        return false//!isCanClick || !GuideFeedDialog.isGuide() || !GuideCashGoldDialog.isGuide()
    }

    fun refreshData() {
        binding.tvGold.text = CurrencyUtils.getCurrencyNum(CurrencyType.GOLD).formThousand()
        binding.tvBanknote.text = CurrencyUtils.getCurrencyNum(CurrencyType.GREEN).formThousand()
        invalidate()
    }

    fun updateGoldAnima(oldCount: Double, newCount: Double, isShowAnima: Boolean = false) {
        if (!isShowAnima) {
            //不显示动画
            binding.tvGold.text = newCount.formThousand()
            return
        }
        //播放自增动画
        if (goldChangeAnimator != null && goldChangeAnimator?.isRunning == true) {
            goldChangeAnimator?.cancel()
        }
        goldChangeAnimator = ValueAnimator.ofFloat(oldCount.toFloat(), newCount.toFloat()).apply {
            this.duration = 500
        }
        goldChangeAnimator?.addUpdateListener {
            val evaluate = FloatEvaluator().evaluate(it.animatedFraction, oldCount, newCount)
            binding.tvGold.text = evaluate.formThousand()
        }

        goldChangeAnimator?.addListener(onEnd = {
            binding.tvGold.text = newCount.formThousand()
            goldChangeAnimator?.removeAllUpdateListeners()
            goldChangeAnimator?.removeAllListeners()
        })
        goldChangeAnimator?.start()

    }

    fun updateGreenAnima(oldCount: Double, newCount: Double, isShowAnima: Boolean = false) {
        if (!isShowAnima) {
            //不显示动画
            binding.tvBanknote.text = newCount.formThousand()
            return
        }
        //播放自增动画
        if (greenChangeAnimator != null && greenChangeAnimator?.isRunning == true) {
            greenChangeAnimator?.cancel()
        }
        greenChangeAnimator = ValueAnimator.ofFloat(oldCount.toFloat(), newCount.toFloat()).apply {
            this.duration = 500
        }
        greenChangeAnimator?.addUpdateListener {
            val evaluate = FloatEvaluator().evaluate(it.animatedFraction, oldCount, newCount)
            binding.tvBanknote.text = evaluate.formThousand()
        }

        greenChangeAnimator?.addListener(onEnd = {
            binding.tvBanknote.text = newCount.formThousand()
            greenChangeAnimator?.removeAllUpdateListeners()
            greenChangeAnimator?.removeAllListeners()
        })
        greenChangeAnimator?.start()
    }


    fun cancelAnima() {
        goldChangeAnimator?.cancel()
        greenChangeAnimator?.cancel()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateGoldEvent(event: UpdateCurrencyEvent) {
        if (event.sender != this){ //用于更新其他页面的金额数值，自身的已经在发事件时自主更新好了
            refreshData()
        }
    }

}