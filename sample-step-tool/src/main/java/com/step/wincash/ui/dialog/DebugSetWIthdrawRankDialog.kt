package com.step.wincash.ui.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import com.lxj.xpopup.core.CenterPopupView
import com.step.wincash.R
import com.step.wincash.beans.CurrencyType
import com.step.wincash.beans.WithdrawRecord
import com.step.wincash.business.withdraw.WithdrawBusiness
import com.step.wincash.event.UpdateCurrencyEvent
import com.step.wincash.utils.CurrencyUtils
import com.step.wincash.utils.toast
import org.greenrobot.eventbus.EventBus


class DebugSetWIthdrawRankDialog(val withdrawRecord: WithdrawRecord, val currencyType: CurrencyType,
                                 context: Context
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.debug_set_withdraw_rank
    }

    override fun onCreate() {
        super.onCreate()
        val etInput = findViewById<EditText>(R.id.etInput)
        findViewById<View>(R.id.btnSet).setOnClickListener {
            val valueString = etInput.text.toString()
            if (valueString.isEmpty()) {
                return@setOnClickListener
            }
            if (!isNumeric(valueString)) {
                return@setOnClickListener
            }
            val value = valueString.toInt()
//            CurrencyUtils.appendCurrencyNum(currencyType, value)
            if (value < withdrawRecord.endRank) {
                toast("当前记录终点是: ${withdrawRecord.endRank}")
                return@setOnClickListener
            }
            WithdrawBusiness.recordListLiveData.value?.forEach { item ->
                // 确保item是WithdrawRecord类型
                if (item is WithdrawRecord && item.time == withdrawRecord.time) {
                    item.rank = value
                    item.finish = value <= item.endRank
                }
            }
            WithdrawBusiness.saveRecordList()
            dismiss()
        }

    }

    //判断字符串是否数字
    fun isNumeric(str: String): Boolean {
        return str.matches("[0-9]+\\.?[0-9]*".toRegex())
    }
}