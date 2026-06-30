package com.step.wincash.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.step.wincash.R
import com.step.wincash.base.BaseBindingAdapter
import com.step.wincash.business.withdraw.PaymentParams
import com.step.wincash.databinding.StRvWalletBinding

class PaymentAdapter(val selectUpdateListener : (Int) -> Unit): BaseBindingAdapter<PaymentParams, StRvWalletBinding>() {
    private var mSelectPosition = 0
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): StRvWalletBinding {
        return StRvWalletBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        helper: BaseBindViewHolder<StRvWalletBinding>,
        item: PaymentParams
    ) {

        val binding = helper.binding
        binding.apply {
            root.apply {
                isSelected = mSelectPosition == helper.adapterPosition
                if (isSelected) {
                    binding.tvWalletName.setTextColor(Color.parseColor("#000000"))
                    binding.rootItem.setBackgroundResource(R.drawable.bg_payment_selected)
                } else {
                    binding.tvWalletName.setTextColor(Color.parseColor("#aaaaaa"))
                    binding.rootItem.setBackgroundResource(R.drawable.bg_payment_un_selected)
                }
                binding.ivSelect.isVisible = isSelected
                tvWalletName.text = item?.paymentName
                ivPayment.setImageResource(item?.paymentIconLong!!)
                setOnClickListener {
                    mSelectPosition = helper.adapterPosition
                    selectUpdateListener.invoke(mSelectPosition)
                    notifyDataSetChanged()
                }
            }

        }

    }
}