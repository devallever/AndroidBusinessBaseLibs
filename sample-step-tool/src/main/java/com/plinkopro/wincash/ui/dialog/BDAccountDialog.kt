package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.text.InputType
import android.text.TextUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.PaymentName
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.business.withdraw.account.AccountBean
import com.plinkopro.wincash.business.withdraw.account.AccountManager
import com.plinkopro.wincash.business.withdraw.account.PatternUtils
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isBkashAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isEasyPaisaAccount
import com.plinkopro.wincash.business.withdraw.account.TipsPopupHelper
import com.plinkopro.wincash.databinding.DialogAccountInputBdBinding
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible

//孟加拉国(Bkash)、巴基斯坦(EASYPAISA)
class BDAccountDialog(context: Context, val paymentParams: PaymentParams, preUnit: () -> Unit, nextUnit: (AccountBean) -> Unit) : BaseAccountInputDialog(context, paymentParams, preUnit, nextUnit) {
    private val isFromBkash by lazy {
        paymentParams.paymentName == PaymentName.BKASH
    } 

    private val binding by lazy { DialogAccountInputBdBinding.bind(this.contentView) }

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_account_input_bd
    }

    override fun onCreate() {
        super.onCreate()
        val accountsBean = AccountManager.findAccountsBean(paymentParams.paymentName)
        
        
        accountsBean?.apply {
            account?.let {
               binding.account1.setText(it)
                changeCustomEdit(it, binding.account1, binding.error1Tips)
            }
            accountName?.let {
                binding.account2.setText(it)
                changeCustomEdit(it, binding.account2, null)
            }

            email?.let {
                if (isFromBkash){
                    binding.account3.setText(it)
                    changeCustomEdit(it, binding.account3, binding.error3Tips)
                }
            }
        }

        if (isFromBkash){
            binding.account3.visible()
            binding.des3.visible()
        }else{
            binding.account1.setHint(R.string.input_confirm_pk_number_hint)
            findViewById<ImageView>(R.id.payment_icon).setImageResource(R.drawable.ic_pay_easypaisa_long)
        }

    }

    override fun getAccountBean(): AccountBean {
        val account: String = binding.account1.text.toString().trim()
        val realName: String = binding.account2.text.toString().trim()
        val email: String = binding.account3.text.toString().trim()
        val accountsBean = AccountBean(account, realName)

        if (isFromBkash){
            accountsBean.email = email
        }
        return accountsBean
    }

    override fun canNext(): Boolean {
        val realName: String = binding.account2.text.toString().trim()
        val account: String = binding.account1.text.toString().trim()
        val email: String = binding.account3.text.toString().trim()

//        if (TextUtils.isEmpty(realName)) {
//            binding.account2.setBackgroundResource(R.drawable.shape_dialog_account_input_error_bg)
//        } else {
//            binding.account2.setBackgroundResource(R.drawable.shape_dialog_account_input_normal_bg)
//        }


        return !(TextUtils.isEmpty(realName) || TextUtils.isEmpty(account) || (isFromBkash && TextUtils.isEmpty(email)))
    }

    override fun getWatcherText(): List<InputBean> {
        val list: MutableList<InputBean> = ArrayList()
        binding.account1.inputType = InputType.TYPE_CLASS_NUMBER
        binding.account3.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.error1Tips.setOnSingleListener {
            val tipId = if (isFromBkash) R.string.input_tips_bkash  else R.string.input_tips_easypaisa
            TipsPopupHelper.show(context, binding.account1, binding.account1.measuredWidth, context.getString(tipId))
        }


        list.add(InputBean(binding.account1, binding.error1Tips))
        list.add(InputBean(binding.account2, null))

        if (isFromBkash){
            binding.error3Tips.setOnSingleListener {
                TipsPopupHelper.show(context, binding.account3, binding.account3.measuredWidth, context.getString(R.string.input_tips_email))
            }
            list.add(InputBean(binding.account3, binding.error3Tips))
        }

        return list
    }


    override fun changeCustomEdit(content: String?, editText: EditText, tipsView: TextView?) {
        if (TextUtils.isEmpty(content)) {
            changeStatus(NORMAL, editText, tipsView)
            return
        }
        if (editText == binding.account1) {
            val isPass = if (isFromBkash) isBkashAccount(content) else isEasyPaisaAccount(content)
            changeStatus(if (isPass) SUCCESS else ERROR, editText, tipsView)
        } else if (editText == binding.account2) {
            changeStatus(SUCCESS, editText, tipsView)
        } else if (editText == binding.account3) {
            changeStatus(if (PatternUtils.isEmail(content)) SUCCESS else ERROR, editText, tipsView)
        }

    }
}