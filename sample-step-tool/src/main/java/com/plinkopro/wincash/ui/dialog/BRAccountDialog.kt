package com.plinkopro.wincash.ui.dialog

import android.content.Context
import android.text.InputType
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.business.withdraw.PaymentName
import com.plinkopro.wincash.business.withdraw.PaymentParams
import com.plinkopro.wincash.business.withdraw.account.AccountBean
import com.plinkopro.wincash.business.withdraw.account.AccountManager
import com.plinkopro.wincash.business.withdraw.account.BrAccountType
import com.plinkopro.wincash.business.withdraw.account.PatternUtils
import com.plinkopro.wincash.business.withdraw.account.TipsPopupHelper
import com.plinkopro.wincash.databinding.DialogAccountInputBrBinding
import com.plinkopro.wincash.utils.setOnSingleListener


//巴西
class BRAccountDialog(
    context: Context,
    val paymentParams: PaymentParams,
    preUnit: () -> Unit,
    nextUnit: (AccountBean) -> Unit
) : BaseAccountInputDialog(context, paymentParams, preUnit, nextUnit) {

    private val binding by lazy { DialogAccountInputBrBinding.bind(this.contentView) }
    private var mPixType: String? = null
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_account_input_br
    }

    private var isFirstGetFocus = true

    override fun onCreate() {
        super.onCreate()

        val accountsBean = AccountManager.findAccountsBean(paymentParams.paymentName)
        //
        val list: MutableList<String> = java.util.ArrayList()
        list.add(BrAccountType.CPF)
        list.add(BrAccountType.PHONE)
        list.add(BrAccountType.EMAIL)

        binding.paymentIcon.setImageResource(paymentParams.paymentIconLong)

        if (isPix()) {
            //类型
            var pixType = BrAccountType.CPF
            accountsBean?.accountType?.let {
                pixType = it
            }
            mPixType = pixType
            binding.account1.isFocusable = true
            binding.account1.isFocusableInTouchMode = true
            binding.account1.inputType = InputType.TYPE_NULL
            binding.account1.setText(pixType)
            changeTextHint(pixType, binding.account2, binding.des2)
            if (pixType == BrAccountType.CPF) {
                binding.account3.isFocusable = true
            } else {
                binding.account2.isFocusable = true
            }
            binding.account1.setOnSingleListener {
                showAccountType(list)
            }
            binding.account1.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    //失去焦点关闭软键盘
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(this.windowToken, 0)
                    if (isFirstGetFocus) {
                        isFirstGetFocus = false
                        return@setOnFocusChangeListener
                    }
                    showAccountType(list)
                }
            }
        } else if (isPageBank()) {
            binding.account1.setHint(R.string.txt_wd_account_email_input)
            binding.account1.isFocusable = true
            binding.account1.isFocusableInTouchMode = true
            binding.account1.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            binding.account1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            binding.des2.setText(R.string.input_confirm_name)
            binding.account2.setHint(R.string.input_confirm_name_hint)
        }



        setInput(accountsBean)
    }

    fun showAccountType(list: List<String>) {
        // 展示/隐藏弹窗
        TipsPopupHelper.showBrAccountType(context, binding.account1, mPixType, list) { type ->
            mPixType = type
            binding.account1.setText(type)
            changeTextHint(type, binding.account2, binding.des2)
        }
    }

    private fun setInput(accountsBean: AccountBean?) {
        accountsBean?.apply {
            if (isPix()) {
                cpfId?.let {
                    binding.account3.setText(it)
                    changeCustomEdit(it, binding.account3, binding.error3Tips)
                }
                account?.let {
                    binding.account2.setText(it)
                    changeCustomEdit(it, binding.account2, binding.error2Tips)
                }
            } else if (isPageBank()) {
                cpfId?.let {
                    binding.account3.setText(it)
                    changeCustomEdit(it, binding.account3, binding.error3Tips)
                }
                account?.let {
                    binding.account1.setText(it)
                    changeCustomEdit(it, binding.account1, binding.error1Tips)
                }
                accountName?.let {
                    binding.account2.setText(it)
                    changeCustomEdit(it, binding.account2, binding.error2Tips)
                }
            }
        }
    }

    private fun changeTextHint(accountType: String, account2: EditText, des2: TextView) {
        if (TextUtils.equals(accountType, BrAccountType.CPF)) {
            binding.account2.visibility = GONE
            binding.error1Tips.visibility = GONE
            binding.error2Tips.visibility = GONE
            des2.visibility = GONE
        } else {
            binding.account2.setText("")
            des2.visibility = VISIBLE
            binding.account2.visibility = VISIBLE
            /* if (TextUtils.equals(accountType, BrAccountType.EVP)) {
                 //请输入32位数字！不限制
                 binding.account2.inputType = InputType.TYPE_CLASS_TEXT
                 binding.account2.setHint(R.string.input_confirm_32_input)
             } else */
            if (TextUtils.equals(accountType, BrAccountType.EMAIL)) {
                //请输入邮箱，不限制
                binding.account2.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                binding.account2.setHint(R.string.txt_wd_account_email_input)
            } else if (TextUtils.equals(accountType, BrAccountType.PHONE)) {
                //请输入手机号，无区号
                binding.account2.inputType = InputType.TYPE_CLASS_PHONE
                binding.account2.setHint(R.string.input_confirm_11_phone_input)
            }
        }
    }

    private fun isPix(): Boolean {
        return TextUtils.equals(paymentParams.paymentName, PaymentName.PIX)
    }

    private fun isPageBank(): Boolean {
        return TextUtils.equals(paymentParams.paymentName, PaymentName.PAGBANK)
    }

    override fun getAccountBean(): AccountBean {
        val account: String = binding.account1.text.toString().trim()
        var accountName: String = binding.account2.text.toString().trim()
        val cpf: String = binding.account3.text.toString().trim()
        if (TextUtils.equals(mPixType, BrAccountType.CPF)) {
            accountName = cpf
        }
        return if (isPix()) {
            AccountBean(accountName, mPixType, context.getString(R.string.app_name), cpf)
        } else {
            AccountBean(account, BrAccountType.EMAIL, accountName, cpf)
        }
    }

    override fun canNext(): Boolean {
        val account1: String = binding.account1.text.toString().trim()
        val account2: String = binding.account2.text.toString().trim()
        val account3: String = binding.account3.text.toString().trim()
        if (TextUtils.equals(mPixType, BrAccountType.CPF)) {
            if (TextUtils.isEmpty(account1) || TextUtils.isEmpty(account3)) {
                return false
            }
        } else {
            if (TextUtils.isEmpty(account1) || TextUtils.isEmpty(account2) || TextUtils.isEmpty(
                    account3
                )
            ) {
                return false
            }
        }
        return true
    }


    override fun changeCustomEdit(content: String?, editText: EditText, tipsView: TextView?) {
        if (TextUtils.isEmpty(content)) {
            changeStatus(NORMAL, editText, tipsView)
            return
        }
        if (editText == binding.account1) {
            if (isPageBank()) {
                changeStatus(
                    if (PatternUtils.isEmail(content)) SUCCESS else ERROR,
                    editText,
                    tipsView
                )
            }
        } else if (editText == binding.account2) {
            if (mPixType != BrAccountType.CPF && isPix()) {
                /*if (TextUtils.equals(mPixType, BrAccountType.EVP)) {
                    //请输入32位！
                    changeStatus(
                        if (PatternUtils.isValidEvp(content)) SUCCESS else ERROR, editText, tipsView
                    )
                } else*/
                if (TextUtils.equals(mPixType, BrAccountType.EMAIL)) {
                    //请输入邮箱，
                    changeStatus(
                        if (PatternUtils.isEmail(content)) SUCCESS else ERROR,
                        editText,
                        tipsView
                    )
                } else if (TextUtils.equals(mPixType, BrAccountType.PHONE)) {
                    //请输入手机号，无区号
                    changeStatus(
                        if (PatternUtils.isPhoneNumber(content)) SUCCESS else ERROR,
                        editText,
                        tipsView
                    )
                }
            } else if (isPageBank()) {
                changeStatus(SUCCESS, editText, tipsView)
            }
        } else if (editText == binding.account3) {
            changeStatus(
                if (PatternUtils.isValidCpf(content)) SUCCESS else ERROR,
                editText,
                tipsView
            )
        }

    }

    override fun getWatcherText(): List<InputBean> {
        val list: MutableList<InputBean> = ArrayList()
        //第一段
        binding.error1Tips.setOnSingleListener {
            if (isPageBank()) {
                TipsPopupHelper.show(
                    context,
                    binding.account1,
                    binding.account1.measuredWidth,
                    context.getString(R.string.input_tips_email)
                )
            }
        }
        list.add(InputBean(binding.account1, binding.error1Tips))
        //第二段
        binding.error2Tips.setOnSingleListener {
            if (isPix()) {
                var resId: Int = R.string.input_tips_phone
                if (TextUtils.equals(mPixType, BrAccountType.CPF)) {
                    binding.account2.inputType = InputType.TYPE_CLASS_NUMBER
                    //请输入11位数字！
                    resId = R.string.input_tips_cpf
                }
                /*                else if (TextUtils.equals(mPixType, BrAccountType.EVP)) {
                                    binding.account2.inputType = InputType.TYPE_CLASS_TEXT
                                    //请输入32位数字！不限制
                                    resId = R.string.input_tips_evp
                                } */
                else if (TextUtils.equals(mPixType, BrAccountType.EMAIL)) {
                    binding.account2.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    //请输入邮箱，不限制
                    resId = R.string.input_tips_email
                } else if (TextUtils.equals(mPixType, BrAccountType.PHONE)) {
                    binding.account2.inputType = InputType.TYPE_CLASS_PHONE
                    //请输入手机号，无区号
                    resId = R.string.input_tips_phone
                }
                TipsPopupHelper.show(
                    context,
                    binding.account2,
                    binding.account2.measuredWidth,
                    context.getString(resId)
                )
            }
        }
        list.add(InputBean(binding.account2, binding.error2Tips))
        //第三段
        binding.error3Tips.setOnSingleListener {
            TipsPopupHelper.show(
                context,
                binding.account3,
                binding.account3.measuredWidth,
                context.getString(R.string.input_tips_cpf)
            )
        }
        list.add(InputBean(binding.account3, binding.error3Tips))
        return list
    }
}