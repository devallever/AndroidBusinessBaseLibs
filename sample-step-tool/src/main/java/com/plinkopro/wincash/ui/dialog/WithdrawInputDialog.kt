//package com.plinkopro.wincash.ui.dialog
//
//import android.content.Context
//import android.view.View
//import android.widget.EditText
//import android.widget.ImageView
//import androidx.core.widget.addTextChangedListener
//import com.lxj.xpopup.core.CenterPopupView
//import com.plinkopro.wincash.R
//import com.plinkopro.wincash.base.BaseApplication
//import com.plinkopro.wincash.beans.CurrencyType
//import com.plinkopro.wincash.business.withdraw.PaymentParams
//import com.plinkopro.wincash.utils.showXPopup
//import com.plinkopro.wincash.utils.toast
//
//class WithdrawInputDialog(
//    context: Context,
//    val paymentParams: PaymentParams,
//    val currencyType: CurrencyType,
//    val level: Int,//档位
//) : CenterPopupView(context) {
//
//    private val mEditText: EditText by lazy { findViewById(R.id.etAccount) }
//    override fun getImplLayoutId(): Int {
//        return R.layout.dialog_withdraw_input
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        findViewById<View>(R.id.ivClose).setOnClickListener {
//            dismiss()
//        }
//        findViewById<View>(R.id.btnConfirm).setOnClickListener {
//            val account = mEditText.text.toString()
//            if (account.isEmpty()) {
//                toast(BaseApplication.instance.getString(R.string.enter_your_account))
//                return@setOnClickListener
//            }
//            context.showXPopup(WithdrawConfirmDialog(
//                context,
//                paymentParams,
//                currencyType,
//                level,
//                account
//            ))
//            dismiss()
//        }
//        findViewById<ImageView>(R.id.ivPayment).setImageResource(paymentParams.paymentIconLong)
//        mEditText.addTextChangedListener {
//
//        }
//    }
//}