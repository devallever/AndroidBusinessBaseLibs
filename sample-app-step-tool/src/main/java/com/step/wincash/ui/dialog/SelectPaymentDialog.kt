package com.step.wincash.ui.dialog

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.step.wincash.R
import com.step.wincash.business.withdraw.PaymentParams
import com.step.wincash.init.InitManager
import com.step.wincash.utils.setOnSingleListener
import com.lxj.xpopup.core.CenterPopupView
import com.step.wincash.business.withdraw.WalletManager
import com.step.wincash.databinding.DialogSelectPaymentBinding
import com.step.wincash.ui.adapter.PaymentAdapter2
import com.step.wincash.ui.widget.SpaceItemDecoration
import com.step.wincash.utils.setVisible

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