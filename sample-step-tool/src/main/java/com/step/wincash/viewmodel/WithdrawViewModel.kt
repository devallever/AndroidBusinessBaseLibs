package com.step.wincash.viewmodel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.allever.android.lib.core.app.App
import com.step.wincash.R
import com.step.wincash.beans.CurrencyType
import com.step.wincash.beans.ExtraKey
import com.step.wincash.beans.WithdrawRecord
import com.step.wincash.business.withdraw.CountryUtil
import com.step.wincash.business.withdraw.PaymentParams
import com.step.wincash.business.withdraw.WalletManager
import com.step.wincash.business.withdraw.WithdrawBusiness
import com.step.wincash.init.InitManager
import com.step.wincash.ui.adapter.PaymentAdapter
import com.step.wincash.utils.CurrencyUtils
import com.step.wincash.utils.SpKey
import com.step.wincash.utils.SpUtil
import com.step.wincash.utils.formThousand
import com.step.wincash.utils.log

class WithdrawViewModel : ViewModel() {
    val paymentList: List<PaymentParams> by lazy { WalletManager.getPaymentParamsList(InitManager.getCountryCode()) }
    val paymentAdapter by lazy {
        PaymentAdapter {
            selectIndex = it
            if (App.DEBUG)
                log("WithdrawViewModel", "selectIndex: $it")
        }.apply {
            setNewData(paymentList as MutableList<PaymentParams>?)
        }
    }
    var selectIndex = 0

    var currencyType: CurrencyType = CurrencyType.GOLD

    val balanceLiveData = MutableLiveData<Int>()

    val level1AmountLiveData = MutableLiveData<String>()
    val level2AmountLiveData = MutableLiveData<String>()

    val level1AmountProgressLiveData = MutableLiveData<Int>()
    val level2AmountProgressLiveData = MutableLiveData<Int>()

    var clickRecordLevel = WithdrawBusiness.WITHDRAW_LEVEL_1

    var level1Record: WithdrawRecord? = null
    var level2Record: WithdrawRecord? = null


    fun selectPayment(): PaymentParams {
        if (selectIndex in paymentAdapter.data.indices) {
            return paymentAdapter.data[selectIndex]
        }
        return PaymentParams.DEFAULT
    }

    fun initExtra(intent: Intent) {
        val currencyType = intent.getIntExtra(ExtraKey.CURRENCY_TYPE, CurrencyType.GOLD.type)
        if (App.DEBUG) {
            log("currencyType: $currencyType")
        }
        this@WithdrawViewModel.currencyType = CurrencyType.fromValue(currencyType)

        updateBalance()
    }

    fun getUserId(): String {
        val userId = SpUtil.get(SpKey.USER_ID, "")
        return "ID:${userId}"
    }

    fun getSymbolByCode(level: Int): String {
        return "${CountryUtil.getSymbolByCode(InitManager.getCountryCode())} ${WithdrawBusiness.getWithdrawCurrencyLabelValue(
            InitManager.getCountryCode(),
            level
        ).formThousand()}"
    }

    fun getBalanceContainerBg(): Int {
        return if (currencyType == CurrencyType.GOLD) {
            R.drawable.ic_withdraw_goal_bg
        } else {
            R.drawable.ic_withdraw_green_bg
        }
    }

    fun getCoinTypeIcon(): Int {
        return if (currencyType == CurrencyType.GOLD) {
            R.drawable.ic_withdraw_goal_icon
        } else {
            R.drawable.ic_withdraw_green_icon
        }
    }

    fun updateBalance() {
        balanceLiveData.value = CurrencyUtils.getCurrencyNum(currencyType).toInt()
//        balanceLiveData.value = 100000

        level1AmountLiveData.value = "${balanceLiveData.value?.formThousand()}/${
            WithdrawBusiness.getWithdrawCurrencyLimit(
                currencyType,
                WithdrawBusiness.WITHDRAW_LEVEL_1
            )
        }"
        level2AmountLiveData.value = "${balanceLiveData.value?.formThousand()}/${
            WithdrawBusiness.getWithdrawCurrencyLimit(
                currencyType,
                WithdrawBusiness.WITHDRAW_LEVEL_2
            )
        }"

        val level1Progress = (balanceLiveData.value?.let {
            it.toFloat() / WithdrawBusiness.getWithdrawCurrencyLimit(
                currencyType,
                WithdrawBusiness.WITHDRAW_LEVEL_1
            ) * 100
        })?.toInt() ?: 0
        level1AmountProgressLiveData.value = if (level1Progress > 100) {
            100
        } else {
            level1Progress
        }
        val level2Progress = (balanceLiveData.value?.let {
            it.toFloat() / WithdrawBusiness.getWithdrawCurrencyLimit(
                currencyType,
                WithdrawBusiness.WITHDRAW_LEVEL_2
            ) * 100
        })?.toInt() ?: 0
        level2AmountProgressLiveData.value = if (level2Progress > 100) {
            100
        } else {
            level2Progress
        }

        if (App.DEBUG) {
            log("levelProgress: $level1Progress")
            log("levelProgress: $level2Progress")
        }
    }

    fun handleCashOut(level: Int, notEnough: () -> Unit, enough: () -> Unit) {
        val notEnoughBalance = (balanceLiveData.value ?: 0) < WithdrawBusiness.getWithdrawCurrencyLimit(
            currencyType,
            level
        )
        if (notEnoughBalance) {
            notEnough.invoke()
        } else {
            enough.invoke()
        }
    }

    fun hasWithdrawRecord(level: Int): Boolean {
        WithdrawBusiness.recordListLiveData.value?.let {
            for (record in it) {
                // 确保record是WithdrawRecord类型
                if (record is WithdrawRecord && record.level == level && record.currencyType == currencyType.type) {
                    if (level == WithdrawBusiness.WITHDRAW_LEVEL_1) {
                        level1Record = record
                    }
                    if (level == WithdrawBusiness.WITHDRAW_LEVEL_2) {
                        level2Record = record
                    }
                    return true
                }
            }
        }
        return false
    }
}