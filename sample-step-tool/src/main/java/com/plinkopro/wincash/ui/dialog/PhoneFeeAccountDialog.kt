package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.text.InputType
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.CountryUtil
import com.plinkopro.wincash.business.withdraw.PaymentName
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.business.withdraw.account.AccountBean
import com.plinkopro.wincash.business.withdraw.account.AccountManager
import com.plinkopro.wincash.business.withdraw.account.PatternUtils
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isBankCardAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isBkashAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isDadaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isEasyPaisaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isLazadaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isPaparaAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isPayaplAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isPhoneFeeAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isShoppeayAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isTrueMoneyAccount
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isZalopayAccount
import com.plinkopro.wincash.business.withdraw.account.TipsPopupHelper
import com.plinkopro.wincash.databinding.DialogAccountInputBaseBinding
import com.plinkopro.wincash.databinding.DialogAccountInputBdBinding
import com.plinkopro.wincash.databinding.DialogAccountInputPhoeeFeeBinding
import com.plinkopro.wincash.init.InitManager
import com.plinkopro.wincash.utils.SimpleTextWatcher
import com.plinkopro.wincash.utils.gone
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible

class PhoneFeeAccountDialog(context: Context, val paymentParams: PaymentParams, preUnit: () -> Unit, nextUnit: (AccountBean) -> Unit) : BaseAccountInputDialog(context, paymentParams, preUnit, nextUnit){

    private val binding by lazy { DialogAccountInputPhoeeFeeBinding.bind(this.contentView) }
    private var mOperatorName: String = ""

    private var isFirstGetFocus = true

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_account_input_phoee_fee
    }

    override fun onCreate() {
        super.onCreate()
        val accountsBean = AccountManager.findAccountsBean(paymentParams.paymentName)

        binding.account1.isFocusable = false
        binding.account1.inputType = InputType.TYPE_NULL
        binding.account1.setOnSingleListener {
            showAccountType()
        }
        binding.account1.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                //失去焦点关闭软键盘
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(this.windowToken, 0)
                if (isFirstGetFocus) {
                    isFirstGetFocus = false
                    return@setOnFocusChangeListener
                }
                showAccountType()
            }
        }

        binding.account2.setHint(getHintStrResId())

        accountsBean?.apply {
            account?.let {
                binding.account2.setText(it)
                changeStatus(it, binding.account2, binding.error2Tips)
            }
            accountName?.let {
                binding.account1.setText(it)
                mOperatorName = it
            }
        }
    }

    fun showAccountType(){
        TipsPopupHelper.showBrAccountType(context, binding.account1, mOperatorName, getPopupList()) { type ->
            binding.account1.setText(type)
            mOperatorName = type
//            binding.account1.setBackgroundResource(R.drawable.shape_dialog_account_input_success_bg)
        }
    }

    override fun getAccountBean(): AccountBean {
        val accountNumber: String = binding.account2.text.toString().trim()
        val operatorName: String = binding.account1.text.toString().trim()

        return AccountBean(accountNumber, operatorName)
    }

    override fun canNext(): Boolean {
        val operatorName: String = binding.account1.text.toString().trim()
//        if (TextUtils.isEmpty(operatorName)) {
//            binding.account1.setBackgroundResource(R.drawable.shape_dialog_account_input_error_bg)
//        } else {
//            binding.account1.setBackgroundResource(R.drawable.shape_dialog_account_input_normal_bg)
//        }
        val account: String = binding.account2.text.toString().trim()

        return !(TextUtils.isEmpty(operatorName) || TextUtils.isEmpty(account))
    }

    override fun getWatcherText(): List<InputBean> {
        val list: MutableList<InputBean> = ArrayList()

        binding.error2Tips.setOnSingleListener {

            val strResId = when(InitManager.getCountryCode()){
                CountryUtil.NG -> R.string.input_tips_10_digits
                CountryUtil.UZ ,CountryUtil.ZA -> R.string.input_tips_9_digits
                else -> R.string.input_tips_bkash
            }
            TipsPopupHelper.show(context, binding.account2, binding.account2.measuredWidth, context.getString(strResId))
        }
        list.add(InputBean(binding.account2, binding.error2Tips))
        return list
    }


    private fun getHintStrResId(): Int{
        return when(InitManager.getCountryCode()){
            CountryUtil.NG -> R.string.txt_account_10_digits_hint
            CountryUtil.UZ ,CountryUtil.ZA -> R.string.txt_account_9_digits_hint
            else -> R.string.input_confirm_bn_number_hint
        }
    }

    private fun getPopupList(): List<String>{
        val list = mutableListOf<String>()

        when(InitManager.getCountryCode()){
            CountryUtil.NG ->{
                list.apply {
                    add("MTN")
                    add("Airtel")
                    add("Globacom")
                    add("9mobile")
                }
            }

            CountryUtil.UZ ->{
                list.apply {
                    add("Beeline")
                    add("Mobiuz")
                    add("Perfectum")
                    add("Ucell")
                    add("UzMobile")
                }
            }

            CountryUtil.ZA ->{
                list.apply {
                    add("Vodacom")
                    add("MTN")
                    add("CellC")
                    add("Telkom Mobile")
                }
            }

            else -> {
                list.apply {
                    add("GrameenPhone")
                    add("Robi")
                    add("Banglalink")
                    add("Teletalk")
                    add("Airtel")
                }
            }
        }
        return list
    }

}