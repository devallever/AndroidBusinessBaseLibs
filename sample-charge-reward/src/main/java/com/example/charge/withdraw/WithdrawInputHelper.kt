package com.example.charge.withdraw

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.charge.ChargeApp
import com.example.charge.R
import com.example.charge.constant.InputType
import com.example.charge.data.InputTypeItem
import com.example.charge.ui.adapter.InputTypeAdapter
import com.example.charge.utils.SpKey
import com.example.charge.utils.SpUtil
import com.example.charge.utils.dp2px

object WithdrawInputHelper {
    fun getName(type: Int): String {
        return when (type) {
            InputType.EMAIL -> ChargeApp.instance.getString(R.string.input_email)
            InputType.NAME -> ChargeApp.instance.getString(R.string.input_name)
            InputType.ID -> ChargeApp.instance.getString(R.string.input_id)
            InputType.PHONE -> ChargeApp.instance.getString(R.string.input_phone)
            InputType.CPF -> ChargeApp.instance.getString(R.string.input_cpf)
            else -> ChargeApp.instance.getString(R.string.input_email)
        }
    }

    fun getValue(paymentParams: PaymentParams, type: Int): String {
        return SpUtil.get(getPaymentKey(paymentParams, type), "")
    }

    fun setValue(paymentParams: PaymentParams, type: Int, value: String) {
        SpUtil.put(getPaymentKey(paymentParams, type), value)
    }

    private fun getPaymentKey(paymentParams: PaymentParams, type: Int): String{
        return when (type) {
            InputType.EMAIL -> SpKey.INPUT_PAYMENT_EMAIL + paymentParams.paymentName
            InputType.NAME -> SpKey.INPUT_PAYMENT_NAME + paymentParams.paymentName
            InputType.ID -> SpKey.INPUT_PAYMENT_ID + paymentParams.paymentName
            InputType.PHONE -> SpKey.INPUT_PAYMENT_PHONE + paymentParams.paymentName
            InputType.CPF -> SpKey.INPUT_PAYMENT_CPF + paymentParams.paymentName
            else -> SpKey.INPUT_PAYMENT_EMAIL + paymentParams.paymentName

        }

    }

    private var accountTypePopupWindow: PopupWindow? = null
    private var allTypes = listOf<InputTypeItem>()
    fun showBrAccountType(
        context: Context,
        targetView: View,
        currentType: InputTypeItem?,
        allTypes: List<InputTypeItem>,
        listener: (InputTypeItem) -> Unit
    ): PopupWindow? {

        if (accountTypePopupWindow == null || this.allTypes != allTypes) {
            val x: Int = dp2px(4f, context)
            val popupWindow: PopupWindow = PopupWindow(context)
            val recyclerView = LayoutInflater.from(context)
                .inflate(R.layout.layout_account_type, null) as RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            popupWindow.contentView = recyclerView
            popupWindow.width = targetView.measuredWidth - x * 2
            // 设置高度
            if (allTypes.size <= 7) {
                // 如果item数量小于等于7个，使用WRAP_CONTENT
                popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                popupWindow.height = dp2px(31.7f, context) * 7
            }
//            popupWindow.isFocusable = true
//            popupWindow.isTouchable = true
            popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            val typeAdapter = InputTypeAdapter { type ->
                listener.invoke(type)
                popupWindow.dismiss()
                accountTypePopupWindow = null
                this.allTypes = listOf()
            }
            recyclerView.adapter = typeAdapter
            typeAdapter.show(currentType, allTypes)
            popupWindow.showAsDropDown(targetView, x, 0)
            this.allTypes = allTypes
            this.accountTypePopupWindow = popupWindow
        } else {
            if (accountTypePopupWindow?.isShowing == true) {
                // 如果弹窗正在显示，则关闭它
                accountTypePopupWindow?.dismiss()
                accountTypePopupWindow = null
            }
        }
        return accountTypePopupWindow
    }

    fun dismissPopupWindow() {
        accountTypePopupWindow?.dismiss()
        accountTypePopupWindow = null
    }
}