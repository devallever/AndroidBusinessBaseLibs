package com.step.wincash.ui.dialog

import android.app.Activity
import androidx.core.view.isVisible
import com.step.wincash.R
import com.step.wincash.base.BaseCenterPopupView
import com.step.wincash.beans.CurrencyType
import com.step.wincash.databinding.DialogHomeAwardBinding
import com.step.wincash.utils.formThousand
import com.step.wincash.utils.gone
import com.step.wincash.utils.setOnSingleListener

class HomeAwareDialog(
    val mActivity: Activity,
    val award: Int,
    val currencyType: CurrencyType = CurrencyType.GREEN,
    val isGuide: Boolean = false,
    val disMissCallBack: () -> Unit
) :
    BaseCenterPopupView(mActivity) {

    private val binding by lazy { DialogHomeAwardBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_home_award
    }

    override fun onCreate() {
        super.onCreate()
        binding.apply {
            if (currencyType == CurrencyType.GOLD) {
                limitIcon.setImageResource(R.drawable.ic_aware_gold)
            }
            awareTv.text = "+${award.formThousand()}"
            getTv.setOnSingleListener {
                disMissCallBack.invoke()
                dismiss()
            }

            binding.fingerView.isVisible = isGuide
        }
        if (!isGuide) {
            initNative()
        }else{
            binding.bottomView.gone()
        }

    }

    fun initNative() {
        binding.naviteView.initView(mActivity)
    }

}