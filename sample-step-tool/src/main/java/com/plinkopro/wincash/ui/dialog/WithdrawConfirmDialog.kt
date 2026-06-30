//package com.plinkopro.wincash.ui.dialog
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.View
//import android.widget.ImageView
//import android.widget.TextView
//import com.lxj.xpopup.core.CenterPopupView
//import com.plinkopro.wincash.BuildConfig
//import com.plinkopro.wincash.R
//import com.plinkopro.wincash.beans.CurrencyType
//import com.plinkopro.wincash.beans.WithdrawRecord
//import com.plinkopro.wincash.business.withdraw.CountryUtil
//import com.plinkopro.wincash.business.withdraw.PaymentParams
//import com.plinkopro.wincash.business.withdraw.WithdrawBusiness
//import com.plinkopro.wincash.event.UpdateCurrencyEvent
//import com.plinkopro.wincash.init.InitManager
//import com.plinkopro.wincash.utils.CurrencyUtils
//import com.plinkopro.wincash.utils.log
//import com.plinkopro.wincash.utils.showXPopup
//import org.greenrobot.eventbus.EventBus
//
//class WithdrawConfirmDialog(
//    context: Context,
//    val paymentParams: PaymentParams,
//    val currencyType: CurrencyType,
//    val level: Int,//档位
//    val account: String//账号
//) : CenterPopupView(context) {
//    override fun getImplLayoutId(): Int {
//        return R.layout.dialog_withdraw_confirm
//    }
//
//    @SuppressLint("SetTextI18n")
//    override fun onCreate() {
//        super.onCreate()
//        findViewById<ImageView>(R.id.ivPayment).setImageResource(paymentParams.paymentIconLong)
//        val currencySymbols =  CountryUtil.getSymbolByCode(InitManager.getCountryCode())
//        val limit = WithdrawBusiness.getWithdrawCurrencyLabelValue(InitManager.getCountryCode(), level)
//        findViewById<TextView>(R.id.etWithdrawAmount).text = "$currencySymbols $limit"
//        findViewById<TextView>(R.id.etAccount).text = account
//        findViewById<View>(R.id.ivClose).setOnClickListener {
//            dismiss()
//        }
//        findViewById<View>(R.id.btnConfirm).setOnClickListener {
//            //减少减少金币
//            val reduceValue = WithdrawBusiness.getWithdrawCurrencyLimit(currencyType, level)
//            CurrencyUtils.reduceCurrencyNum(currencyType, reduceValue)
//            val record = WithdrawRecord(WithdrawBusiness.getStartRank(), System.currentTimeMillis(), limit, InitManager.getCountryCode(), currencyType.type, level)
//            WithdrawBusiness.insertRecord(record)
//            EventBus.getDefault().post(UpdateCurrencyEvent(currencyType, this))
//            //
//            context.showXPopup(WithdrawRankDialog(context, record), autoDismiss = true)
//            dismiss()
//            if (BuildConfig.LOG_OUTPUT) {
//                log("reduceValue = $reduceValue")
//            }
//        }
//        findViewById<View>(R.id.btnCompile).setOnClickListener {
//            //回到InputDialog
//            context.showXPopup(
//                WithdrawInputDialog(
//                    context,
//                    paymentParams,
//                    currencyType,
//                    level
//                )
//            )
//            dismiss()
//        }
//    }
//}