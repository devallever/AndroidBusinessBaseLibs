package com.step.wincash.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import app.allever.android.lib.core.app.App
import com.step.wincash.beans.CurrencyType
import com.step.wincash.beans.ExtraKey
import com.step.wincash.beans.WithdrawRecord
import com.step.wincash.ui.adapter.WithdrawRecordAdapter
import com.step.wincash.utils.log

class WithdrawRecordViewModel: ViewModel() {

    var currencyType: CurrencyType = CurrencyType.GOLD

    val listData = mutableListOf<WithdrawRecord>()
    val recordAdapter by lazy {
        WithdrawRecordAdapter().apply {
            setNewData(listData)
        }
    }

    fun initExtra(intent: Intent) {
        val currencyType = intent.getIntExtra(ExtraKey.CURRENCY_TYPE, CurrencyType.GOLD.type)
        if (App.DEBUG) {
            log("currencyType: $currencyType")
        }
        this.currencyType = CurrencyType.fromValue(currencyType)
    }

}