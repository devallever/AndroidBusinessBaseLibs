package com.step.wincash.ui.dialog

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.step.wincash.R
import com.step.wincash.business.withdraw.PaymentName
import com.step.wincash.business.withdraw.PaymentParams
import com.step.wincash.business.withdraw.account.AccountBean
import com.step.wincash.business.withdraw.account.AccountManager
import com.step.wincash.business.withdraw.account.KoBankUtils
import com.step.wincash.business.withdraw.account.PatternUtils
import com.step.wincash.business.withdraw.account.PatternUtils.isBankCardAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isBkashAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isDadaAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isEasyPaisaAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isLazadaAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isPaparaAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isPayaplAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isPhoneFeeAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isShoppeayAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isTrueMoneyAccount
import com.step.wincash.business.withdraw.account.PatternUtils.isZalopayAccount
import com.step.wincash.business.withdraw.account.TipsPopupHelper
import com.step.wincash.databinding.DialogAccountInputBdBinding
import com.step.wincash.databinding.DialogAccountInputKoBinding
import com.step.wincash.utils.SimpleTextWatcher
import com.step.wincash.utils.setOnSingleListener
import com.step.wincash.utils.visible
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.step.wincash.databinding.DialogWdErrorBinding
import com.step.wincash.utils.gone

class WdErrorDialog(context: Context, val paymentParams: PaymentParams, val accountBean: AccountBean, val amount : Float ,val errorMsg: String, val applyTime : String, val nextUnit:(paymentParams: PaymentParams?)->Unit) : CenterPopupView(context) {

    private val binding by lazy { DialogWdErrorBinding.bind(this.contentView) }

    private var isOneMode = false

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_wd_error
    }

    override fun onCreate() {
        super.onCreate()
        binding.closeIv.setOnSingleListener {
            dismiss()
        }

       binding.signUp.setOnSingleListener {
            if (isOneMode){
                onNext()
            }else{
                openUrlWithChooser(context, paymentParams.registerUrl)
            }
        }

       binding.next.setOnSingleListener {
           onNext()
        }
        binding.nextTv.setOnSingleListener {
            onNext()
        }

        initView()
    }

    override fun getMaxWidth(): Int {
        return (XPopupUtils.getAppWidth(this.context))
    }


    private fun initView(){
        val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        InputConfirmDialog.initRv(context, recyclerView, paymentParams, accountBean, amount,true ,applyTime)

        binding.tvTips.text = errorMsg

        if (paymentParams.registerUrl.isNullOrEmpty()){
            isOneMode = true
            binding.showSignUpCL.gone()
        }else{
            isOneMode = false
            binding.nextTv.gone()
        }
    }

    private fun onNext(){
        nextUnit.invoke(paymentParams)
        dismiss()
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