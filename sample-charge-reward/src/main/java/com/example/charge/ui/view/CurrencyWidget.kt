package com.example.charge.ui.view

import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import app.allever.android.lib.core.app.App
import com.example.charge.currency.CurrencyType
import com.example.charge.databinding.WidgetCurrencyBinding
import com.example.charge.event.UpdateCurrencyEvent
import com.example.charge.currency.CurrencyUtils
import com.example.charge.init.InitManager
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.formThousand
import com.example.charge.utils.setOnSingleListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CurrencyWidget @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    ConstraintLayout(context, attributeSet, defStyle) {

    private val binding: WidgetCurrencyBinding =
        WidgetCurrencyBinding.inflate(LayoutInflater.from(context), this)
    private var goldChangeAnimator: ValueAnimator? = null
    private var greenChangeAnimator: ValueAnimator? = null
    private var onCoinClickCallback: ((coinType: Int) -> Unit)? = null

    init {
        EventBus.getDefault().register(this)
        refreshData()
        binding.apply {
            flGold.setOnSingleListener {
                onCoinClickCallback?.invoke(CurrencyType.GOLD.type)
            }
            flGreen.setOnSingleListener {
                onCoinClickCallback?.invoke(CurrencyType.GREEN.type)
            }
        }
        if (App.DEBUG) {
            binding.flGold.setOnLongClickListener { view ->
                handleGoldDebug(CurrencyType.GOLD)
                true
            }
            binding.flGreen.setOnLongClickListener { view ->
                handleGoldDebug(CurrencyType.GREEN)
                true
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

    @SuppressLint("SetTextI18n")
    fun refreshData() {
        binding.tvGold.text = CurrencyUtils.getCurrencyNum(CurrencyType.GOLD).formThousand()
        binding.tvGreen.text = CountryUtil.getSymbolByCode(InitManager.getCountryCode())+CurrencyUtils.getCurrencyNum(CurrencyType.GREEN).formThousand()
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
            binding.tvGold.text = addSymbol(newCount.formThousand())
            goldChangeAnimator?.removeAllUpdateListeners()
            goldChangeAnimator?.removeAllListeners()
        })
        goldChangeAnimator?.start()

    }

    fun updateGreenAnima(oldCount: Double, newCount: Double, isShowAnima: Boolean = false) {
        if (!isShowAnima) {
            //不显示动画
            binding.tvGreen.text = addSymbol(newCount.formThousand())
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
            binding.tvGreen.text = addSymbol(evaluate.formThousand())
        }

        greenChangeAnimator?.addListener(onEnd = {
            binding.tvGreen.text = addSymbol(newCount.formThousand())
            greenChangeAnimator?.removeAllUpdateListeners()
            greenChangeAnimator?.removeAllListeners()
        })
        greenChangeAnimator?.start()
    }


    fun cancelAnima() {
        goldChangeAnimator?.cancel()
        greenChangeAnimator?.cancel()
    }

    fun addSymbol(num : String): String{
        return "${CountryUtil.getSymbolByCode()}$num"
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateGoldEvent(event: UpdateCurrencyEvent) {
        if (event.sender != this) { //用于更新其他页面的金额数值，自身的已经在发事件时自主更新好了
            refreshData()
        }
    }

}