package com.plinkopro.wincash.ui.dialog

import android.app.Activity
import androidx.core.view.isVisible
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseCenterPopupView
import com.plinkopro.wincash.beans.CurrencyType
import com.plinkopro.wincash.databinding.DialogHomeAwardBinding
import com.plinkopro.wincash.utils.formThousand
import com.plinkopro.wincash.utils.gone
import com.plinkopro.wincash.utils.setOnSingleListener

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