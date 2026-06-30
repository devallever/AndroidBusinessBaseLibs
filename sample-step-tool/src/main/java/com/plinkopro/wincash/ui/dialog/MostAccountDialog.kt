package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.widget.ImageView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.PaymentName
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.business.withdraw.account.AccountBean
import com.plinkopro.wincash.business.withdraw.account.AccountManager
import com.plinkopro.wincash.business.withdraw.account.TipsPopupHelper
import com.plinkopro.wincash.databinding.DialogAccountInputBaseBinding
import com.plinkopro.wincash.utils.SimpleTextWatcher
import com.plinkopro.wincash.utils.gone
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible


//默认的填写弹窗
class MostAccountDialog(context: Context, val paymentParams: PaymentParams, preUnit: () -> Unit, nextUnit: (AccountBean) -> Unit) : BaseAccountInputDialog(context,paymentParams, preUnit, nextUnit) {
     val binding  by lazy { DialogAccountInputBaseBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_account_input_base
    }

    override fun onCreate() {
        super.onCreate()
        val accountsBean = AccountManager.findAccountsBean(paymentParams.paymentName)
        val paymentIcon = findViewById<ImageView>(R.id.payment_icon)
        paymentIcon.setImageResource(paymentParams.paymentIconLong)
        accountsBean?.apply {
            account?.let {
                binding.accountEvInput.setText(it)
                changeStatus(it,binding.accountEvInput,binding.accountErrorTips)
            }
        }

        inputHintText()

        if (hasNameOption(paymentParams.paymentName)){
            updateEtSingleLine(binding.accountNameInput)
            binding.accountNameInput.addTextChangedListener(object : SimpleTextWatcher(){
                override fun afterTextChanged(s: Editable) {
                    super.afterTextChanged(s)
                    changeNameStatus()
                    updateEtSingleLine(binding.accountEvInput)
                }
            })

           if (!accountsBean?.accountName.isNullOrBlank()) binding.accountNameInput.setText(accountsBean.accountName)

            binding.tvName.visible()
            binding.accountNameInput.visible()

        }else{
            binding.tvName.gone()
            binding.accountNameInput.gone()
        }
    }

    override fun getAccountBean(): AccountBean {
        val account: String = binding.accountEvInput.text.toString().trim()
        var realName: String? = null
        if (!binding.accountNameInput.text.isNullOrBlank()){
            realName = binding.accountNameInput.text.toString().trim()
        }
       return AccountBean(account, realName)
    }

    override fun canNext(): Boolean {
        val account: String = binding.accountEvInput.text.toString().trim()
        val name: String = binding.accountNameInput.text.toString().trim()
        return if (hasNameOption(paymentParams.paymentName)){
            !(TextUtils.isEmpty(account) || TextUtils.isEmpty(name))

        }else{
            !TextUtils.isEmpty(account)
        }
    }

    override fun getWatcherText(): List<InputBean> {
        val list: MutableList<InputBean> = mutableListOf()
        binding.accountErrorTips.setOnSingleListener {
            //
            TipsPopupHelper.show(context,binding.accountEvInput,binding.accountEvInput.measuredWidth,context.getString(getResId()))
        }

        list.add(InputBean(binding.accountEvInput, binding.accountErrorTips))
        return list
    }

    private fun getResId(): Int {
        if (TextUtils.equals(paymentParams.paymentName, PaymentName.PAPARA)) {
            return R.string.input_tips_papara
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.PAYPAL)) {
            //请输入邮箱，不限制
            return R.string.input_tips_email
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.DANA)) {
            return R.string.input_tips_dana
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.TRUEMONEY)) {
            return R.string.input_tips_papara
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.LAZADA)) {
            //邮箱 或 09开头11位数字
            return R.string.input_tips_lazada
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.SHOPEEPAY)) {
            return R.string.input_tips_shopeepay
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.ZALOPAY)) {
            return R.string.input_tips_zalopay
        }
        return R.string.input_tips_common
    }

    private fun inputHintText(){
        val strId = if (TextUtils.equals(paymentParams.paymentName, PaymentName.PAPARA)) {
            binding.accountEvInput.inputType = InputType.TYPE_CLASS_NUMBER
            R.string.txt_account_papara_hint
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.PAYPAL)) {
            binding.accountEvInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            R.string.txt_paypal_hint
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.DANA)) {
            binding.accountEvInput.inputType = InputType.TYPE_CLASS_NUMBER
            R.string.txt_account_dana_hint
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.TRUEMONEY)) {
            binding.accountEvInput.inputType = InputType.TYPE_CLASS_NUMBER
            R.string.txt_account_truemoney_hint
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.LAZADA)) {
            R.string.txt_account_lazada_hint
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.SHOPEEPAY)) {
            binding.accountEvInput.inputType = InputType.TYPE_CLASS_NUMBER
            R.string.txt_account_shopee_hint
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.ZALOPAY)) {
            binding.accountEvInput.inputType = InputType.TYPE_CLASS_NUMBER
            R.string.txt_account_zalopay_hint
        } else {
            R.string.input_tips_common
        }
        binding.accountEvInput.setHint(strId)
    }


    private fun hasNameOption(paymentName: String): Boolean {
        var result = false
        when (paymentName.lowercase()) {
            PaymentName.TRUEMONEY.lowercase(), PaymentName.DANA.lowercase(), PaymentName.PAPARA.lowercase(), PaymentName.LAZADA.lowercase(), PaymentName.SHOPEEPAY.lowercase(), PaymentName.ZALOPAY.lowercase() -> result = true
            else -> {}
        }

        return result
    }

    private fun changeNameStatus(){
        if (binding.accountNameInput.text.isNullOrBlank()){
//            binding.accountNameInput.setBackgroundResource(R.drawable.shape_dialog_account_input_normal_bg)
            binding.accountNameInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            binding.accountNameInput.setTextColor(Color.parseColor("#333333"))
        }else{
//            binding.accountNameInput.setBackgroundResource(R.drawable.shape_dialog_account_input_success_bg)
            binding.accountNameInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_bingo, 0)
            binding.accountNameInput.setTextColor(Color.parseColor("#3EE737"))
        }
    }
}
