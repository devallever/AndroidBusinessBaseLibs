package com.plinkopro.wincash.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.plinkopro.wincash.R
import com.plinkopro.wincash.utils.setOnSingleListener
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