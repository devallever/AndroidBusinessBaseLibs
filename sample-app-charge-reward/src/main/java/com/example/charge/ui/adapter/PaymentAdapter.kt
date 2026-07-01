package com.example.charge.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.charge.base.BaseBindingAdapter
import com.example.charge.databinding.RvWalletBinding
import com.example.charge.utils.setOnSingleListener
import com.example.charge.withdraw.PaymentParams
import kotlin.apply

class PaymentAdapter(): BaseBindingAdapter<PaymentParams, RvWalletBinding>() {
    private var mSelectPosition = 0

    var selectUpdateListener : ((Int) -> Unit)? = null

    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RvWalletBinding {
        return RvWalletBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        helper: BaseBindViewHolder<RvWalletBinding>,
        item: PaymentParams
    ) {

        val binding = helper.binding
        binding.apply {
            root.apply {
                isSelected = mSelectPosition == helper.adapterPosition
                ivSelect.isVisible = isSelected
                ivSelectFrame.isVisible = isSelected
                ivPayment.setImageResource(item?.paymentIconLong!!)
                setOnSingleListener {
                    mSelectPosition = helper.adapterPosition
                    selectUpdateListener?.invoke(mSelectPosition)
                    notifyDataSetChanged()
                }
            }

        }

    }
}