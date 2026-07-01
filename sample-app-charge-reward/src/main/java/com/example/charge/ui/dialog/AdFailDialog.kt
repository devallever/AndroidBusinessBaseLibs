package com.example.charge.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.example.charge.R
import com.example.charge.databinding.DialogAdFailBinding
import com.example.charge.utils.setOnSingleListener
import com.lxj.xpopup.core.CenterPopupView

class AdFailDialog(
    context: Context,
    val cb: () -> Unit
) : CenterPopupView(context) {

    private val binding by lazy { DialogAdFailBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_ad_fail
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        binding.apply {
            btnOk.setOnSingleListener {
                cb.invoke()
                dismiss()
            }
        }
    }
}