package com.step.wincash.ui.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import com.lxj.xpopup.core.CenterPopupView
import com.step.wincash.R
import com.step.wincash.beans.CurrencyType
import com.step.wincash.event.UpdateCurrencyEvent
import com.step.wincash.utils.CurrencyUtils
import org.greenrobot.eventbus.EventBus


class DebugAddBalanceDialog(val currencyType: CurrencyType,
    context: Context
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.debug_add_balance
    }

    override fun onCreate() {
        super.onCreate()
        val etInput = findViewById<EditText>(R.id.etInput)
        findViewById<View>(R.id.btnAdd).setOnClickListener {
            val valueString = etInput.text.toString()
            if (valueString.isEmpty()) {
                return@setOnClickListener
            }
            if (!isNumeric(valueString)) {
                return@setOnClickListener
            }
            val value = valueString.toInt()
            CurrencyUtils.appendCurrencyNum(currencyType, value)
            EventBus.getDefault().post(UpdateCurrencyEvent(currencyType, this))
            dismiss()
        }

    }

    //判断字符串是否数字
    fun isNumeric(str: String): Boolean {
        return str.matches("[0-9]+\\.?[0-9]*".toRegex())
    }
}