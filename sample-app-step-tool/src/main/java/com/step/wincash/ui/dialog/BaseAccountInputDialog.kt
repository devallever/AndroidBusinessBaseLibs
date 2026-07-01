package com.step.wincash.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import app.allever.android.lib.core.app.App
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.step.wincash.R
import com.step.wincash.business.withdraw.PaymentName
import com.step.wincash.business.withdraw.PaymentParams
import com.step.wincash.business.withdraw.account.AccountBean
import com.step.wincash.business.withdraw.account.AccountManager
import com.step.wincash.business.withdraw.account.PatternUtils.isBankCardAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isDadaAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isLazadaAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isPaparaAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isPayaplAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isPhoneFeeAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isShoppeayAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isTrueMoneyAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isZalopayAccount
import com.step.wincash.utils.SimpleTextWatcher
import com.step.wincash.utils.log
import com.step.wincash.utils.setOnSingleListener

abstract class BaseAccountInputDialog(context: Context, private val paymentParams: PaymentParams, val preUnit:()->Unit, val nextUnit:(account: AccountBean)->Unit) : CenterPopupView(context) {
    private var textWatcherMap: MutableMap<InputBean, SimpleTextWatcher> = HashMap()
    protected val NORMAL: Int = 0
    protected val SUCCESS: Int = 1
    protected val ERROR: Int = -1

    class InputBean(var editText: EditText, var tipsView: TextView?)

    override fun getMaxWidth(): Int {
        return (XPopupUtils.getAppWidth(this.context))
    }
    override fun onCreate() {
        super.onCreate()
        if (App.DEBUG) {
            log("BaseAccountInputDialog", "onCreate: ${this.javaClass.simpleName}")
        }
        findViewById<View>(R.id.close_iv).setOnSingleListener {
            dismiss()
        }
        findViewById<View>(R.id.pre).setOnSingleListener {
            preUnit.invoke()
            dismiss()
        }
        findViewById<View>(R.id.next).setOnSingleListener {
            if(!canNext()){
                Toast.makeText(context,R.string.toast_input_empty, Toast.LENGTH_SHORT).show()
                return@setOnSingleListener
            }

            val accountBean = getAccountBean()
            AccountManager.saveAccountsBean(paymentParams.paymentName,accountBean)
            nextUnit.invoke(accountBean)
            dismiss()
        }
        val editTexts = getWatcherText()
        post { addTextWatcher(editTexts) }
    }

    abstract fun getAccountBean(): AccountBean

    abstract fun canNext(): Boolean

    protected abstract fun getWatcherText():List<InputBean>

    private fun addTextWatcher(editTexts: List<InputBean>) {
        for (inputBean in editTexts) {
            updateEtSingleLine(inputBean.editText)
            inputBean.tipsView?.apply {
                post {
                    paint?.flags = Paint.UNDERLINE_TEXT_FLAG
                    invalidate()
                }
            }
            textWatcherMap[inputBean] = object : SimpleTextWatcher() {
                override fun afterTextChanged(s: Editable) {
                    super.afterTextChanged(s)
                    val editText = inputBean.editText
                    val tipsView = inputBean.tipsView

                    val content = editText.text.toString().trim ()
                    updateEtSingleLine(editText)
                    changeStatus(content,editText,tipsView)
                }
            }
            inputBean.editText.addTextChangedListener( textWatcherMap[inputBean])
        }
    }

    protected fun updateEtSingleLine(et: EditText) {
        et.isSingleLine = et.text.isNotEmpty()
        et.maxLines = if (et.text.isNotEmpty()) 1 else Integer.MAX_VALUE
        if (et.text.length == 1) et.setSelection(et.text.length)
    }

    protected fun changeStatus(content: String?,editText: EditText, tipsView: TextView?){
        if (TextUtils.isEmpty(content)) {
            changeStatus(NORMAL, editText, tipsView)
            return
        }
        if (TextUtils.equals(paymentParams.paymentName, PaymentName.PAPARA)) {
            changeStatus(if (isPaparaAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.PAYPAL)) {
            //请输入邮箱，不限制
            changeStatus(if (isPayaplAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.DANA)) {
            changeStatus(if (isDadaAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.TRUEMONEY)) {
            changeStatus(if (isTrueMoneyAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.LAZADA)) {
            //邮箱 或 09开头11位数字
            changeStatus(if (isLazadaAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.SHOPEEPAY)) {
            changeStatus(if (isShoppeayAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.ZALOPAY)) {
            changeStatus(if (isZalopayAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.BankCard) || TextUtils.equals(paymentParams.paymentName, PaymentName.Clipspay)) {
            changeStatus(if (isBankCardAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.PhoneFee)){
            changeStatus(if (isPhoneFeeAccount(content)) SUCCESS else ERROR, editText, tipsView)
        } else if (TextUtils.equals(paymentParams.paymentName, PaymentName.PAGBANK) || TextUtils.equals(paymentParams.paymentName, PaymentName.PIX)
            || TextUtils.equals(paymentParams.paymentName, PaymentName.BKASH) || TextUtils.equals(paymentParams.paymentName, PaymentName.EASYPAISA)) {
            changeCustomEdit(content, editText, tipsView)
        }

    }

    protected open fun changeCustomEdit(content: String?, editText: EditText, tipsView: TextView?) {
    }

    protected fun changeStatus(status: Int, editText: EditText, tipsView: TextView?) {
        if (status == NORMAL) {
//            editText.setBackgroundResource(R.drawable.shape_dialog_account_input_normal_bg)
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            editText.setTextColor(Color.parseColor("#333333"))
            tipsView?.visibility = GONE
        } else if (status == SUCCESS) {
//            editText.setBackgroundResource(R.drawable.shape_dialog_account_input_success_bg)
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_bingo, 0)
            editText.setTextColor(Color.parseColor("#3EE737"))
            tipsView?.visibility = GONE
        } else if (status == ERROR) {
//            editText.setBackgroundResource(R.drawable.shape_dialog_account_input_error_bg)
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.vector_error, 0)
            editText.setTextColor(Color.parseColor("#FF2E00"))
            tipsView?.visibility = VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (inputBean in textWatcherMap.keys) {
            inputBean.editText.removeTextChangedListener(textWatcherMap[inputBean])
        }
    }


}
