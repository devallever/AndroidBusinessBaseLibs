package com.step.wincash.ui.dialog

import android.content.Context
import android.view.View
import com.lxj.xpopup.core.CenterPopupView
import com.step.wincash.R
import com.step.wincash.utils.toast

class WithdrawNotSufficientDialog(
    context: Context
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_balance_not_suffcient
    }

    override fun onCreate() {
        super.onCreate()
        findViewById<View>(R.id.ivClose).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.btnConfirm).setOnClickListener {
            toast("confirm")
            dismiss()
        }
    }
}