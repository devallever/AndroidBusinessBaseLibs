package com.step.wincash.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.step.wincash.R
import com.step.wincash.utils.setOnSingleListener
import com.lxj.xpopup.core.CenterPopupView
import com.step.wincash.business.withdraw.CountryUtil
import com.step.wincash.databinding.DialogWdSuccessBinding
import com.step.wincash.init.InitManager

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