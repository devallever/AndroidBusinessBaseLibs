package com.step.wincash.ui.dialog

import android.content.Context
import androidx.annotation.DrawableRes
import com.step.wincash.R
import com.step.wincash.base.BaseCenterPopupView
import com.step.wincash.databinding.DialogOverlayAwardBinding
import com.step.wincash.utils.formThousand

class OverlayAwareDialog(context: Context, @DrawableRes val iconRes : Int ,val award: Int, val disMissCallBack: () -> Unit) :
    BaseCenterPopupView(context) {

    private val binding by lazy { DialogOverlayAwardBinding.bind(this.contentView) }
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_overlay_award
    }

    override fun onCreate() {
        super.onCreate()
        binding.apply {
            numTv.text = "+${award.formThousand()}"
            icon.setImageResource(iconRes)
            postDelayed({
                disMissCallBack.invoke()
                dismiss()
            }, 500)
        }
    }

}