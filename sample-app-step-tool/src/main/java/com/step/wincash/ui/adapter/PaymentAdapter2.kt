package com.step.wincash.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.step.wincash.base.BaseBindingAdapter
import com.step.wincash.business.withdraw.PaymentParams
import com.step.wincash.databinding.ItemPaymentBinding
import kotlin.apply

class PaymentAdapter2(val selectUpdateListener : (Int) -> Unit) : BaseBindingAdapter<PaymentParams, ItemPaymentBinding>() {
    var selectPosition = 0
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemPaymentBinding {
        return ItemPaymentBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun convert(
        helper: BaseBindViewHolder<ItemPaymentBinding>,
        item: PaymentParams
    ) {
        helper.binding.payIv.apply {
            isSelected = selectPosition == helper.adapterPosition
            setImageResource(item.paymentIconLong)
            setOnClickListener {
                selectPosition = helper.adapterPosition
                selectUpdateListener.invoke(selectPosition)
                notifyDataSetChanged()
            }
        }
    }
}