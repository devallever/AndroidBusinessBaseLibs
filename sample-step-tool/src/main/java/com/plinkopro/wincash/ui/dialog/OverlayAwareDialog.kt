package com.plinkopro.wincash.ui.dialog

import android.content.Context
import androidx.annotation.DrawableRes
import com.plinkopro.wincash.R
import com.plinkopro.wincash.base.BaseCenterPopupView
import com.plinkopro.wincash.databinding.DialogOverlayAwardBinding
import com.plinkopro.wincash.utils.formThousand

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