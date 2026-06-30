package com.plinkopro.wincash.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import app.allever.android.lib.core.app.App
import com.plinkopro.wincash.beans.CurrencyType
import com.plinkopro.wincash.beans.ExtraKey
import com.plinkopro.wincash.beans.WithdrawRecord
import com.plinkopro.wincash.ui.adapter.WithdrawRecordAdapter
import com.plinkopro.wincash.utils.log

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