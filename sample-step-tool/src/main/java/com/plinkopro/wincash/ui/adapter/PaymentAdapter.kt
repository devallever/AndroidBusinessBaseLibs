package com.plinkopro.wincash.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseBindingAdapter
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.databinding.RvWalletBinding

class PaymentAdapter(val selectUpdateListener : (Int) -> Unit): BaseBindingAdapter<PaymentParams, RvWalletBinding>() {
    private var mSelectPosition = 0
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