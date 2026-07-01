package com.example.charge.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import com.example.charge.R
import com.example.charge.databinding.DialogNewUserBinding
import com.example.charge.init.FpManger
import com.example.charge.init.InitManager
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.setOnSingleListener
import com.lxj.xpopup.core.CenterPopupView
import kotlin.random.Random
import kotlin.random.nextInt

class NewUserDialog(
    context: Context,
    val dismissCb: (value: Int) -> Unit
) : CenterPopupView(context) {

    private val binding by lazy { DialogNewUserBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_new_user
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        binding.apply {
            val randomGreen = Random.nextInt(FpManger.chargeConfig.newUserMinAware..FpManger.chargeConfig.newUserMaxAware)
            tvGreen.text = "${CountryUtil.getSymbolByCode(InitManager.getCountryCode())} $randomGreen"
            btnOk.setOnSingleListener {
                dismiss()
                dismissCb.invoke(randomGreen)
            }
        }
    }
}