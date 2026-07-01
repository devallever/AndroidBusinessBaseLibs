package com.example.charge.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.graphics.toColorInt
import com.example.charge.R
import com.example.charge.databinding.DialogInputBinding
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.toast
import com.example.charge.withdraw.PaymentName
import com.example.charge.withdraw.PaymentParams
import com.example.charge.withdraw.WithdrawInputHelper
import com.lxj.xpopup.core.CenterPopupView

class InputDialog(
    context: Context,
    val paymentParams: PaymentParams,
    val inputType: Int,
    val cb: (value: String) -> Unit
) : CenterPopupView(context) {

    private val binding by lazy { DialogInputBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_input
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        binding.apply {
            val ivIconStrokeColor= when(paymentParams.paymentName){
                PaymentName.PAYPAL -> "#FF009DE3"
                PaymentName.VISA -> "#FF00569D"
                PaymentName.MASTERCARD ->"#FFE3001F"
                PaymentName.PIX -> "#FF00BDAD"
                PaymentName.PAGBANK -> "#FFC9D665"
                else -> "#FF009DE3"
            }.toColorInt()
            ivIcon.apply {
                setImageResource(paymentParams.paymentIcon)
                shapeDrawableBuilder
                    .setStrokeColor(ivIconStrokeColor)
                    .intoBackground();
            }
            etInput.hint = WithdrawInputHelper.getName(inputType)
            etInput.setText(WithdrawInputHelper.getValue(paymentParams, inputType))
            btnOk.setOnSingleListener {
                val input = binding.etInput.text.toString()
                WithdrawInputHelper.setValue(paymentParams, inputType, input)
                cb.invoke(input)
                dismiss()
            }
        }
    }
}