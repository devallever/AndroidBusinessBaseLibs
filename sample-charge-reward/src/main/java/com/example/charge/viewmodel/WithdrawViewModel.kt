package com.example.charge.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.charge.constant.InputType
import com.example.charge.data.InputTypeItem
import com.example.charge.data.RankItem
import com.example.charge.data.RedeemItem
import com.example.charge.init.InitManager
import com.example.charge.ui.adapter.PaymentAdapter
import com.example.charge.ui.adapter.RankItemAdapter
import com.example.charge.ui.adapter.RedeemItemAdapter
import com.example.charge.withdraw.PaymentParams
import com.example.charge.withdraw.WalletManager
import com.example.charge.withdraw.WithdrawHelper
import com.example.charge.withdraw.WithdrawInputHelper

class WithdrawViewModel: ViewModel() {
    var selectIndex = 0
    val paymentList: List<PaymentParams> by lazy { WalletManager.getPaymentParamsList(InitManager.getCountryCode()) }
    val paymentAdapter by lazy {
        PaymentAdapter().apply {
            setNewData(paymentList as MutableList<PaymentParams>?)
        }
    }

    val rankList by lazy {
        mutableListOf<RankItem>()
    }
    val rankAdapter by lazy {
        RankItemAdapter().apply {
            setNewData(rankList)
        }
    }

    val redeemList by lazy {
        mutableListOf<RedeemItem>()
    }
    val redeemAdapter by lazy {
        RedeemItemAdapter().apply {
            setNewData(redeemList)
        }
    }

    val inputTypeList by lazy {
        mutableListOf<InputTypeItem>().apply {
            add(InputTypeItem(InputType.CPF, WithdrawInputHelper.getName(InputType.CPF)))
            add(InputTypeItem(InputType.PHONE, WithdrawInputHelper.getName(InputType.PHONE)))
            add(InputTypeItem(InputType.EMAIL, WithdrawInputHelper.getName(InputType.EMAIL)))
        }
    }

    var currentInputType: InputTypeItem = inputTypeList[0]

    fun selectPayment(): PaymentParams {
        if (selectIndex in paymentAdapter.data.indices) {
            return paymentAdapter.data[selectIndex]
        }
        return PaymentParams.DEFAULT
    }

    init {
        initRankList()
        initRedeemList()
    }

    private fun initRankList() {
        rankList.clear()
        WithdrawHelper.chargeConfig.withdraw.forEachIndexed { index, obj ->
            rankList.add(RankItem(index + 1, 0))
        }
        rankAdapter.notifyDataSetChanged()
    }

    private fun initRedeemList() {
        redeemList.clear()
        redeemList.add(RedeemItem(10000, 1f, false))
        redeemList.add(RedeemItem(100000, 11f, false))
        redeemAdapter.notifyDataSetChanged()
    }

    fun initExtra(intent: Intent) {

    }

}