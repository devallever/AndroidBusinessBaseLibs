package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.view.View
import com.lxj.xpopup.core.CenterPopupView
import com.plinkopro.wincash.R

class WithdrawWaitingDialog(
    context: Context
) : CenterPopupView(context) {
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_waiting_withdraw
    }

    override fun onCreate() {
        super.onCreate()
        findViewById<View>(R.id.ivClose).setOnClickListener {
            dismiss()
        }
        findViewById<View>(R.id.btnConfirm).setOnClickListener {
            dismiss()
        }
    }
}