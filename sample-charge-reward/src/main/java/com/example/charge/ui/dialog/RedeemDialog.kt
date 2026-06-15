package com.example.charge.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import app.allever.android.lib.core.app.App
import com.example.charge.R
import com.example.charge.ad.AdIndex
import com.example.charge.currency.CurrencyFlyAnimatorUtil
import com.example.charge.currency.CurrencyType
import com.example.charge.currency.CurrencyUtils
import com.example.charge.databinding.DialogGameAwareBinding
import com.example.charge.databinding.DialogRedeemBinding
import com.example.charge.event.AnimEvent
import com.example.charge.event.DismissAdEvent
import com.example.charge.event.UpdateCurrencyEvent
import com.example.charge.init.InitManager
import com.example.charge.utils.CountryUtil
import com.example.charge.utils.LogUtil
import com.example.charge.utils.formThousand
import com.example.charge.utils.gone
import com.example.charge.utils.setOnSingleListener
import com.example.charge.utils.toast
import com.example.charge.utils.visible
import com.lxj.xpopup.core.CenterPopupView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RedeemDialog(
    context: Context,
    val greenNum : Float
) : CenterPopupView(context) {

    private val binding by lazy { DialogRedeemBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_redeem
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate() {
        super.onCreate()
        binding.apply {
            tvGreen.text = "${CountryUtil.getSymbolByCode(InitManager.getCountryCode())} ${greenNum.formThousand()}"
            btnOk.setOnSingleListener {
                dismiss()
            }
        }
    }
}