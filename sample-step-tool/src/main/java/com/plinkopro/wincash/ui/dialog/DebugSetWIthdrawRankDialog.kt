package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.view.View
import android.widget.EditText
import com.lxj.xpopup.core.CenterPopupView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.beans.CurrencyType
import com.plinkopro.wincash.beans.WithdrawRecord
import com.plinkopro.wincash.business.withdraw.WithdrawBusiness
import com.plinkopro.wincash.event.UpdateCurrencyEvent
import com.plinkopro.wincash.utils.CurrencyUtils
import com.plinkopro.wincash.utils.toast
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