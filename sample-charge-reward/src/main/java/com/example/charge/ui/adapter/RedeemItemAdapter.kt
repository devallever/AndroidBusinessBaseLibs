package com.example.charge.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.base.BaseBindingAdapter
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.data.RedeemItem
import com.example.charge.databinding.RvRedeemBinding
import com.example.charge.event.UpdateCurrencyEvent
import com.example.charge.ui.dialog.RedeemDialog
import com.example.charge.utils.dp2px
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.showXPopup
import com.example.charge.utils.toast
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import org.greenrobot.eventbus.EventBus

class RedeemItemAdapter() : BaseBindingAdapter<RedeemItem, RvRedeemBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RvRedeemBinding {
        return RvRedeemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun convert(
        helper: BaseBindViewHolder<RvRedeemBinding>,
        item: RedeemItem
    ) {
        helper.binding.apply {
            tvGold.text = "x${item.goldCount}"
            tvGreen.text = "$${item.greenValue.toInt()}"
            val canRedeem = CurrencyUtils.getCurrencyNum(CurrencyType.GOLD) >= item.goldCount
            tvRedeem.isVisible = canRedeem
            tvRedeemDisable.isVisible = !canRedeem
            if (canRedeem) {
                rootView.setBackgroundResource(R.drawable.ic_redeem_item_bg)
                line.setBackgroundColor(App.context.getColor(R.color.color_BE69FF))
                ivChange.setImageResource(R.drawable.ic_redeem_item_change)
                btnRedeem.setBackgroundResource(R.drawable.ic_btn_redeem)
            } else {
                rootView.setBackgroundResource(R.drawable.ic_redeem_item_bg_disable)
                line.setBackgroundColor(App.context.getColor(R.color.color_9D9D9D))
                ivChange.setImageResource(R.drawable.ic_redeem_item_change_disable)
                btnRedeem.setBackgroundResource(R.drawable.ic_btn_redeem_disable)
            }
            btnRedeem.setOnSingleListener {
                if (canRedeem) {
                    CurrencyUtils.updateCurrencyNum(CurrencyType.GOLD, -item.goldCount.toFloat())
                    CurrencyUtils.updateCurrencyNum(CurrencyType.GREEN, item.greenValue)
                    EventBus.getDefault().post(UpdateCurrencyEvent(currencyType = CurrencyType.GOLD))
                    EventBus.getDefault().post(UpdateCurrencyEvent(currencyType = CurrencyType.GREEN))
                    //1:10000, 2:100000
                    SdkManager.dot("cion_redeem", mapOf("cionredeem_level" to data.indexOf( item) + 1))
                    App.context.showXPopup(RedeemDialog(App.context,  item.greenValue), autoDismiss = true)
                    notifyDataSetChanged()
                }
            }
        }
    }
}