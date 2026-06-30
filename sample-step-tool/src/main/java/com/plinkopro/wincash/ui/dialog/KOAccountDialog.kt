package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.PaymentName
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.business.withdraw.account.AccountBean
import com.plinkopro.wincash.business.withdraw.account.AccountManager
import com.plinkopro.wincash.business.withdraw.account.KoBankUtils
import com.plinkopro.wincash.business.withdraw.account.PatternUtils.isBankCardAccount
import com.plinkopro.wincash.business.withdraw.account.TipsPopupHelper
import com.plinkopro.wincash.databinding.DialogAccountInputKoBinding
import com.plinkopro.wincash.utils.SimpleTextWatcher
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible

import kotlin.compareTo

//韩国
class KOAccountDialog(context: Context, val paymentParams: PaymentParams, preUnit: () -> Unit, nextUnit: (AccountBean) -> Unit) : BaseAccountInputDialog(context, paymentParams, preUnit, nextUnit) {

    private val isClipsPay by lazy { paymentParams.paymentName.equals(PaymentName.Clipspay, true) }

    private var mBankName: String = ""

    private var isFirstGetFocus = true

    private val binding by lazy { DialogAccountInputKoBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_account_input_ko
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

        binding.paymentIcon.setImageResource(paymentParams.paymentIconLong)

        if (isClipsPay){
            binding.tvFirstName.visible()
             binding.firstNameInput.visible()
             binding.tvLastName.visible()
             binding.lastNameInput.visible()

            initListener()
        }

        accountsBean?.apply {
            account?.let {
                binding.account2.setText(it)
                changeStatus(if (isBankCardAccount(it)) SUCCESS else ERROR,binding.account2,binding.error2Tips)
            }
            accountName?.let {
                binding.account1.setText(it)
                mBankName = it
            }

            binding.firstNameInput.setText(accountsBean.firstName)
            binding.lastNameInput.setText(accountsBean.lastName)
        }
    }

    fun showAccountType() {
        TipsPopupHelper.showBrAccountType(context, binding.account1, mBankName, KoBankUtils.getBankNameList(paymentParams.paymentName)) { type ->
            binding.account1.setText(type)
            mBankName = type
//            binding.account1.setBackgroundResource(R.drawable.shape_dialog_account_input_success_bg)
            binding.account1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, when{
                type.length > 35 -> 8f
                type.length > 30 -> 10f
                type.length > 25 -> 11f
                else -> 12f
            })
        }
    }

    override fun getAccountBean(): AccountBean {
        val accountNumber: String =  binding.account2.text.toString().trim()
        val bankName: String =  binding.account1.text.toString().trim()

        val accountsBean = AccountBean(accountNumber, bankName)
         if (isClipsPay){
             accountsBean.apply {
                firstName =  binding.firstNameInput.text.toString().trim()
                lastName =  binding.lastNameInput.text.toString().trim()
            }

        }

        return accountsBean
    }

    override fun canNext(): Boolean {
        val bankName: String =  binding.account1.text.toString().trim()
//        if (TextUtils.isEmpty(bankName)) {
//             binding.account1.setBackgroundResource(R.drawable.shape_dialog_account_input_error_bg)
//        } else {
//             binding.account1.setBackgroundResource(R.drawable.shape_dialog_account_input_normal_bg)
//        }
        val account: String =  binding.account2.text.toString().trim()

        val firstName: String =  binding.firstNameInput.text.toString().trim()
        val lastName: String =  binding.lastNameInput.text.toString().trim()


        return !(TextUtils.isEmpty(bankName) || TextUtils.isEmpty(account) || (isClipsPay && (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName))))
    }

    override fun getWatcherText(): List<InputBean> {
        val list: MutableList<InputBean> = ArrayList()
         binding.error2Tips.setOnSingleListener {
            TipsPopupHelper.show(context,  binding.account2,  binding.account2.measuredWidth, context.getString(R.string.input_tips_bank_card))
        }
        list.add(InputBean( binding.account2,  binding.error2Tips))
        return list
    }

    private fun initListener(){
         binding.firstNameInput.addTextChangedListener(object : SimpleTextWatcher(){
            override fun afterTextChanged(s: Editable) {
                super.afterTextChanged(s)
                if (s.toString().isBlank()){
                    changeStatus(NORMAL,  binding.firstNameInput, null)
                }else{
                    changeStatus(SUCCESS,  binding.firstNameInput, null)
                }
            }
        })

         binding.lastNameInput.addTextChangedListener(object : SimpleTextWatcher(){
            override fun afterTextChanged(s: Editable) {
                super.afterTextChanged(s)
                if (s.toString().isBlank()){
                    changeStatus(NORMAL,  binding.lastNameInput, null)
                }else{
                    changeStatus(SUCCESS,  binding.lastNameInput, null)
                }
            }
        })
    }
}