package com.example.charge.ui.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.isVisible
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.base.BaseBindingAdapter
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.data.RankItem
import com.example.charge.databinding.RvRankBinding
import com.example.charge.utils.dp2px
import com.example.charge.utils.formThousand
import com.example.charge.withdraw.WithdrawHelper

class RankItemAdapter : BaseBindingAdapter<RankItem, RvRankBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RvRankBinding {
        return RvRankBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun convert(
        helper: BaseBindViewHolder<RvRankBinding>,
        item: RankItem
    ) {
        helper.binding.apply {
            rootView.setBackgroundResource(when(item.level) {
                1 -> R.drawable.ic_wd_level_bg_1
                2 -> R.drawable.ic_wd_level_bg_2
                3 -> R.drawable.ic_wd_level_bg_3
                else -> R.drawable.ic_wd_level_bg_1
            })
            bgLeft.setBackgroundResource(when(item.level) {
                1 -> R.drawable.ic_wd_level_bg_left_1
                2 -> R.drawable.ic_wd_level_bg_left_2
                3 -> R.drawable.ic_wd_level_bg_left_3
                else -> R.drawable.ic_wd_level_bg_left_1
            })
            tvLevel.text = "$${WithdrawHelper.getWithdrawLevelValue(item.level)}"
            val showRankInfo = WithdrawHelper.showWaitingPlayer()
            val showRedeem = WithdrawHelper.showRedeem(item.level)

            val amount = CurrencyUtils.getCurrencyNum(CurrencyType.GREEN)
            val amountString = amount.formThousand()
            val limit = WithdrawHelper.getWithdrawLevelValue(item.level)
            tvCurrencyProgress.text = "$${amountString}/$${limit}"
            progressBar.progress = (amount / limit * 100).toInt()

            tvCurrencyProgress.isVisible = !showRankInfo
            progressBar.isVisible = !showRankInfo
            progressBg.isVisible = !showRankInfo
            tvRankInfo.isVisible = showRankInfo
            btnRedeem.isVisible = showRankInfo

            if (showRankInfo) {
                // 获取玩家数量
                val playerCount = WithdrawHelper.getWaitingPlayerCount(item.level).toString()
                // 获取完整文本
                val fullText = App.context.getString(R.string.rank_info, playerCount)
                // 使用SpannableStringBuilder设置部分文字颜色
                val spannableStringBuilder = android.text.SpannableStringBuilder(fullText)
                // 找到数字在文本中的位置
                val numberStartIndex = fullText.indexOf(playerCount)
                val numberEndIndex = numberStartIndex + playerCount.length
                // 设置数字部分为红色
                spannableStringBuilder.setSpan(
                    android.text.style.ForegroundColorSpan(App.context.getColor(R.color.color_FF3706)),
                    numberStartIndex,
                    numberEndIndex,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // 设置到TextView
                tvRankInfo.text = spannableStringBuilder

                btnRedeem.isVisible = showRedeem
                tvCurrencyProgress.isVisible = !showRedeem
                tvCurrencyProgress.gravity = Gravity.CENTER
            } else {
                tvCurrencyProgress.gravity = Gravity.START
            }

            val infoLp = tvRankInfo.layoutParams as ViewGroup.MarginLayoutParams
            if (showRankInfo && !showRedeem) {
                infoLp.topMargin = dp2px(-10f)
            } else {
                infoLp.topMargin = dp2px(0f)
            }
            tvRankInfo.layoutParams = infoLp

            if (App.DEBUG) {
                rootView.setOnLongClickListener {
                    val playerCount = WithdrawHelper.getWaitingPlayerCount(item.level).toString()
                    val editText = EditText(App.context)
                    editText.setTextColor(Color.BLACK)
                    editText.hint = "Input count"
                    editText.setText(playerCount)
                    editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
                    AlertDialog.Builder(App.context).setTitle("Input count")
                        .setView(editText)
                        .setPositiveButton("Confirm", { dialog, which ->
                            val number = editText.text?.toString()?.toIntOrNull()
                            if (number != null) {
                                WithdrawHelper.debugUpdateWaitingPlayer(item.level, number)
                            }
                            dialog.dismiss()
                        })
                        .setNegativeButton("Cancel", { dialog, which ->
                            dialog.dismiss()
                        }).show()
                    true
                }
            }
        }
    }
}