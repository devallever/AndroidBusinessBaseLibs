package com.plinkopro.wincash.ui.dialog

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.init.InitManager
import com.plinkopro.wincash.utils.setOnSingleListener
import com.lxj.xpopup.core.CenterPopupView
import com.plinkopro.wincash.business.withdraw.WalletManager
import com.plinkopro.wincash.databinding.DialogSelectPaymentBinding
import com.plinkopro.wincash.ui.adapter.PaymentAdapter2
import com.plinkopro.wincash.ui.widget.SpaceItemDecoration
import com.plinkopro.wincash.utils.setVisible

@SuppressLint("ViewConstructor")
class SelectPaymentDialog(context: Context, val next:(paymentParams: PaymentParams?)->Unit) : CenterPopupView(context) {

    val binding  by lazy { DialogSelectPaymentBinding.bind(this.contentView) }
    val paymentList : List<PaymentParams> by lazy { WalletManager.getPaymentParamsList(InitManager.getCountryCode()) }

    var selectPosition = 0
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_select_payment
    }
    override fun onCreate() {
        super.onCreate()
        binding.apply {
            closeIv.setOnSingleListener {
                dismiss()
            }
            paymentRv.apply {
                layoutManager = GridLayoutManager(context,2)
                addItemDecoration(SpaceItemDecoration(context, 2, 11, 11))
                adapter = PaymentAdapter2 { position ->
                    selectPosition = position
                    updateViewShow()
                }.apply {
                    setNewData(paymentList as MutableList<PaymentParams>?)
                }
            }

            setNextClickListener(next)
            setNextClickListener(nextTv)

            signUp.setOnSingleListener {
                openUrlWithChooser(context, paymentList[selectPosition].registerUrl)
            }
        }
        updateViewShow()
    }

    fun setNextClickListener(view : View){
        view.setOnSingleListener {
            next.invoke(paymentList[selectPosition])
            dismiss()
        }
    }

    fun updateViewShow(){
        if (selectPosition in paymentList.indices){
            val showSignUp = paymentList[selectPosition].registerUrl.isNotEmpty()
            binding.apply {
                showSignUpCL.setVisible(showSignUp)
                nextTv.setVisible(!showSignUp)
                tipsTv.text = context.getString(R.string.tips_select_payment, paymentList[selectPosition].paymentName )
            }
        }
    }

    fun openUrlWithChooser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            val chooser = Intent.createChooser(intent, context.getString(R.string.select_a_browser))
            context.startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.no_browser), Toast.LENGTH_SHORT).show()
        }
    }

}