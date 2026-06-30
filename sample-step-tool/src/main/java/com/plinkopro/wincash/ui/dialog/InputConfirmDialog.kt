package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.PaymentName
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.business.withdraw.account.AccountBean
import com.plinkopro.wincash.utils.setOnSingleListener

import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.plinkopro.wincash.business.withdraw.BalanceFormatUtils
import com.plinkopro.wincash.business.withdraw.CountryUtil
import com.plinkopro.wincash.business.withdraw.account.BrAccountType
import com.plinkopro.wincash.business.withdraw.bean.ItemBean
import com.plinkopro.wincash.init.InitManager
import com.plinkopro.wincash.ui.adapter.ConfirmAdapter

class InputConfirmDialog(context: Context, val paymentParams: PaymentParams, val accountBean: AccountBean, val amount : Float ,val preUnit:()->Unit, val next:()->Unit) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_confirm_input
    }

    override fun onCreate() {
        super.onCreate()
        findViewById<ImageView>(R.id.close_iv).setOnSingleListener {
            dismiss()
        }
        findViewById<TextView>(R.id.pre).setOnSingleListener {
            preUnit.invoke()
            dismiss()
        }

        findViewById<TextView>(R.id.next).setOnSingleListener {
            next.invoke()
            dismiss()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        initRv(context, recyclerView, paymentParams, accountBean,amount,false)
    }

    private fun getString(resId:Int):String{
        return context.getString(resId)
    }

    override fun getMaxWidth(): Int {
          return (XPopupUtils.getAppWidth(this.context))
    }

    companion object{

        fun initRv(context: Context, recyclerView: RecyclerView, paymentParams: PaymentParams,  accountBean: AccountBean,amount : Float , isFailed: Boolean ,applyTime : String = ""){
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = ConfirmAdapter()
            recyclerView.adapter = adapter
            val itemBeans: MutableList<ItemBean> = ArrayList()
            //提现方式
            val paymentName = paymentParams.paymentName
            itemBeans.add(
                ItemBean(
                    context.getString(R.string.input_confirm_payment),
                    paymentParams.paymentIconLong,
                    isFailed
                )
            )
            if (TextUtils.equals(paymentName, PaymentName.BKASH) || TextUtils.equals(paymentName, PaymentName.EASYPAISA)) {
                accountBean.account?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_account), it, isFailed))
                }
                accountBean.accountName?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_name), it, isFailed))
                }

                if (TextUtils.equals(paymentName, PaymentName.BKASH)){
                    accountBean.email?.let {
                        itemBeans.add(ItemBean(context.getString(R.string.txt_email), it, isFailed))
                    }
                }

            } else if (TextUtils.equals(paymentName, PaymentName.PAGBANK)) {
                accountBean.cpfId?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_cpf_number), it, isFailed))
                }
                accountBean.accountName?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_name), it, isFailed))
                }
                accountBean.account?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_account), it, isFailed))
                }
            } else if (TextUtils.equals(paymentName, PaymentName.PIX)) {
                accountBean.cpfId?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_cpf_number), it, isFailed))
                }
                accountBean.accountType?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_pix_account), it, isFailed))

                }
                if (!accountBean.accountType.equals(BrAccountType.CPF)) {
                    accountBean.account?.let {
                        itemBeans.add(ItemBean(context.getString(R.string.input_confirm_pix_account_des), it, isFailed))
                    }
                }
            } else if (TextUtils.equals(paymentName, PaymentName.BankCard) || TextUtils.equals(paymentName, PaymentName.Clipspay) ) {
                accountBean.accountName?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_bank_name), it, isFailed))
                }
                accountBean.account?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_account), it, isFailed))
                }

                if (TextUtils.equals(paymentName, PaymentName.Clipspay)){
                    accountBean.firstName?.let {
                        itemBeans.add(ItemBean(context.getString(R.string.txt_first_name), it, isFailed))
                    }

                    accountBean.lastName?.let {
                        itemBeans.add(ItemBean(context.getString(R.string.txt_last_name), it, isFailed))
                    }

                }

            } else {
                accountBean.account?.let {
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_account), it, isFailed))
                }

                if (!accountBean.accountName.isNullOrBlank()){
                    itemBeans.add(ItemBean(context.getString(R.string.input_confirm_name), accountBean.accountName!!, isFailed))
                }
            }
            if (isFailed){
                itemBeans.add(ItemBean(context.getString(R.string.apply_time), applyTime, isFailed))
            }
            itemBeans.add(ItemBean(context.getString(R.string.input_confirm_money),
                CountryUtil.getSymbolByCode(InitManager.getCountryCode())+ BalanceFormatUtils.getFormatBalance(amount), isFailed))

            adapter.setNewData(itemBeans)
        }
    }
}