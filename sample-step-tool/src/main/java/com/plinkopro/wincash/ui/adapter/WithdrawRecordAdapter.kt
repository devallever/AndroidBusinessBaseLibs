package com.plinkopro.wincash.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseApplication
import com.plinkopro.wincash.base.BaseBindingAdapter
import com.plinkopro.wincash.beans.WithdrawRecord
import com.plinkopro.wincash.business.withdraw.CountryUtil
import com.plinkopro.wincash.business.withdraw.WalletManager.findPaymentParams2
import com.plinkopro.wincash.business.withdraw.WithdrawBusiness
import com.plinkopro.wincash.databinding.RvWithdrawRecordBinding
import com.plinkopro.wincash.init.InitManager
import com.plinkopro.wincash.utils.TimeUtil

class WithdrawRecordAdapter: BaseBindingAdapter<WithdrawRecord, RvWithdrawRecordBinding>() {
    override fun createViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): RvWithdrawRecordBinding {
        return RvWithdrawRecordBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun convert(
        helper: BaseBindViewHolder<RvWithdrawRecordBinding>,
        item: WithdrawRecord
    ) {
        val binding = helper.binding
        binding.apply {
            item.paymentName?.let {
                findPaymentParams2(item.paymentName, InitManager.getCountryCode())?.let {
                    ivIcon.setImageResource(it.paymentIcon)
                }
            }
            tvTime.text = TimeUtil.formatTimeYYYY_MM_dd(item.time)
            tvRank.text = BaseApplication.instance.getString(R.string.withdraw_rank_info, item.rank)
            tvAmount.text = "${CountryUtil.getSymbolByCode(InitManager.getCountryCode())}${WithdrawBusiness.getWithdrawCurrencyLabelValue(InitManager.getCountryCode(), item.level)}"
        }
    }
}