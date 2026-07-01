package com.example.charge.ui.activity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.base.BaseActivity
import com.example.charge.constant.InputType
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.databinding.ActivityWithdrawBinding
import com.example.charge.event.UpdateCurrencyEvent
import com.example.charge.event.WaitingPlayerUpdateEvent
import com.example.charge.ui.dialog.InputDialog
import com.example.charge.utils.formThousand
import com.example.charge.utils.log
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.showXPopup
import com.example.charge.viewmodel.WithdrawViewModel
import com.example.charge.withdraw.PaymentName
import com.example.charge.withdraw.WithdrawHelper
import com.example.charge.withdraw.WithdrawInputHelper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WithdrawActivity : BaseActivity<ActivityWithdrawBinding>() {
    private val mViewModel by viewModels<WithdrawViewModel>()
    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityWithdrawBinding {
        return ActivityWithdrawBinding.inflate(layoutInflater)
    }

    override fun initView() {
        fixStatusBar(binding.tvTitle)
        updateCurrency()
        initPayment()
        initRank()
        initRedeem()
        initListener()
        initInput()
    }

    private fun initListener() {
        binding.ivClose.setOnSingleListener {
            finish()
        }
    }

    private fun initInput() {

        binding.apply {
            etInputType.text =
                getString(R.string.input_pix_account_type, mViewModel.currentInputType.name)

            updateInput()

            etInputType.setOnSingleListener {
                handleEtInputType()
            }
            ivIconEditSelectAccount.setOnSingleListener {
                handleEtInputType()
            }
            ivIconPaymentSelectAccount.setOnSingleListener {
                handleEtInputType()
            }

            scrollView.setOnScrollChangeListener { scrollView, x, y, oldx, oldy ->
                WithdrawInputHelper.dismissPopupWindow()
            }

            ivIconEditEmail.setOnSingleListener {
                showInputDialog(InputType.EMAIL)
            }
            ivIconEditName.setOnSingleListener {
                showInputDialog(InputType.NAME)
            }
            ivIconEditId.setOnSingleListener {
                showInputDialog(InputType.ID)
            }
            ivIconEditPhone.setOnSingleListener {
                showInputDialog(InputType.PHONE)
            }
            ivIconEditCPF.setOnSingleListener {
                showInputDialog(InputType.CPF)
            }
        }
    }

    private fun handleEtInputType() {
        WithdrawInputHelper.showBrAccountType(
            this@WithdrawActivity,
            binding.containerSelectAccount,
            mViewModel.currentInputType,
            mViewModel.inputTypeList
        ) {
            mViewModel.currentInputType = it
            binding.etInputType.text = getString(R.string.input_pix_account_type, it.name)
            updateInput()
        }
    }

    private fun showInputDialog(type: Int) {
        showXPopup(InputDialog(this@WithdrawActivity, mViewModel.selectPayment(), type) {
            updateInput()
        }, autoDismiss = true)
    }

    private fun initPayment() {
        binding.rvWallet.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvWallet.adapter = mViewModel.paymentAdapter
        mViewModel.paymentAdapter.selectUpdateListener = {
            mViewModel.selectIndex = it
            updateInput()
            if (App.DEBUG)
                log("WithdrawViewModel", "selectIndex: $it")
        }
    }

    private fun initRank() {
        binding.rvRank.layoutManager =
            LinearLayoutManager(this)
        binding.rvRank.adapter = mViewModel.rankAdapter
    }

    private fun initRedeem() {
        binding.rvRedeem.layoutManager =
            LinearLayoutManager(this)
        binding.rvRedeem.adapter = mViewModel.redeemAdapter
    }

    private fun updateCurrency() {
        binding.tvCurrencyGold.text = CurrencyUtils.getCurrencyNum(CurrencyType.GOLD).formThousand()
    }

    @SuppressLint("SetTextI18n")
    private fun updateInput() {
        WithdrawInputHelper.dismissPopupWindow()
        val selectPayment = mViewModel.selectPayment()
        val paymentName = selectPayment.paymentName
        binding.apply {
            etEmail.setText(WithdrawInputHelper.getValue(mViewModel.selectPayment(),InputType.EMAIL))
            etName.setText(WithdrawInputHelper.getValue(mViewModel.selectPayment(),InputType.NAME))
            etId.setText(WithdrawInputHelper.getValue(mViewModel.selectPayment(),InputType.ID))
            etPhone.setText(WithdrawInputHelper.getValue(mViewModel.selectPayment(),InputType.PHONE))
            etCPF.setText(WithdrawInputHelper.getValue(mViewModel.selectPayment(),InputType.CPF))
            when (paymentName) {
                PaymentName.PAYPAL -> {
                    containerSelectAccount.isVisible = false
                    containerEmail.isVisible = true
                    containerName.isVisible = false
                    containerId.isVisible = false
                    containerPhone.isVisible = false
                    containerCPF.isVisible = false
                    ivIconPaymentEmail.setImageResource(R.drawable.ic_pay_paypal)
                }

                PaymentName.VISA, PaymentName.MASTERCARD -> {
                    containerSelectAccount.isVisible = false
                    containerEmail.isVisible = false
                    containerName.isVisible = true
                    containerId.isVisible = true
                    containerPhone.isVisible = false
                    containerCPF.isVisible = false
                    if (paymentName == PaymentName.VISA) {
                        ivIconPaymentName.setImageResource(R.drawable.ic_pay_visa)
                        ivIconPaymentId.setImageResource(R.drawable.ic_pay_visa)
                    } else {
                        ivIconPaymentName.setImageResource(R.drawable.ic_pay_mastercard)
                        ivIconPaymentId.setImageResource(R.drawable.ic_pay_mastercard)
                    }
                }

                PaymentName.PIX -> {
                    containerSelectAccount.isVisible = true
                    containerEmail.isVisible = mViewModel.currentInputType.type == InputType.EMAIL
                    containerName.isVisible = false
                    containerId.isVisible = false
                    containerPhone.isVisible = mViewModel.currentInputType.type == InputType.PHONE
                    containerCPF.isVisible = true
                    ivIconPaymentCPF.setImageResource(R.drawable.ic_pay_pix)
                    ivIconPaymentPhone.setImageResource(R.drawable.ic_pay_pix)
                    ivIconPaymentEmail.setImageResource(R.drawable.ic_pay_pix)
                }

                PaymentName.PAGBANK -> {
                    containerSelectAccount.isVisible = false
                    containerEmail.isVisible = true
                    containerName.isVisible = true
                    containerId.isVisible = false
                    containerPhone.isVisible = false
                    containerCPF.isVisible = true
                    ivIconPaymentEmail.setImageResource(R.drawable.ic_pay_pagbank)
                    ivIconPaymentName.setImageResource(R.drawable.ic_pay_pagbank)
                    ivIconPaymentCPF.setImageResource(R.drawable.ic_pay_pagbank)
                }
            }
        }
    }

    override fun enableEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateGoldEvent(event: UpdateCurrencyEvent) {
        updateCurrency()
        if (event.currencyType == CurrencyType.GREEN) {
            mViewModel.rankAdapter.notifyDataSetChanged()
        }

        //更新Redeem
        mViewModel.redeemList.forEachIndexed { index, item ->
            val enable = CurrencyUtils.getCurrencyNum(CurrencyType.GOLD) >= item.goldCount
            if (enable != item.enable) {
                item.enable = enable
                mViewModel.redeemAdapter.notifyItemChanged(index, index)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveWaitingPlayerUpdateEvent(event: WaitingPlayerUpdateEvent) {
        mViewModel.rankList.forEachIndexed { index, item ->
            item.player = WithdrawHelper.getWaitingPlayerCount(index + 1)
        }
        mViewModel.rankAdapter.notifyDataSetChanged()
    }
}