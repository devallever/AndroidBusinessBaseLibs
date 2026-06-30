package com.plinkopro.wincash.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.PaymentName
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.business.withdraw.account.AccountBean
import com.plinkopro.wincash.business.withdraw.account.AccountManager
import com.plinkopro.wincash.business.withdraw.account.KoBankUtils
import com.plinkopro.wincash.business.withdraw.account.PatternUtils
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isBankCardAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isBkashAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isDadaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isEasyPaisaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isLazadaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isPaparaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isPayaplAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isPhoneFeeAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isShoppeayAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isTrueMoneyAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isZalopayAccount
import com.plinkopro.wincash.business.withdraw.account.TipsPopupHelper
import com.plinkopro.wincash.databinding.DialogAccountInputBdBinding
import com.plinkopro.wincash.databinding.DialogAccountInputKoBinding
import com.plinkopro.wincash.utils.SimpleTextWatcher
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible
import com.lxj.xpopup.core.CenterPopupView
import com.plinkopro.wincash.business.withdraw.CountryUtil
import com.plinkopro.wincash.databinding.DialogWdSuccessBinding
import com.plinkopro.wincash.init.InitManager

@SuppressLint("ViewConstructor")
class WdApplySuccessDialog(context: Context, val moneyNum : Float) : CenterPopupView(context) {

    private val binding by lazy { DialogWdSuccessBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_wd_success
    }
    override fun onCreate() {
        super.onCreate()
        binding.apply {
            closeIv.setOnSingleListener {
                dismiss()
            }
            val unit = CountryUtil.getSymbolByCode(InitManager.getCountryCode())
            titleTv.text = context.getString(R.string.withdraw_apply_success, unit,moneyNum)
            okTv.setOnSingleListener {
                dismiss()
            }
        }
    }
}