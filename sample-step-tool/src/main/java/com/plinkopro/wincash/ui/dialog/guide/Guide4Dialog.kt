package com.plinkopro.wincash.ui.dialog.guide

import android.content.Context
import com.lxj.xpopup.impl.FullScreenPopupView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.databinding.DialogGuide4Binding
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible

class Guide4Dialog(context: Context, val actionCallback: () -> Unit) :
    FullScreenPopupView(context) {
    //binding
    private lateinit var binding: DialogGuide4Binding

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_guide_4
    }

    override fun onCreate() {
        super.onCreate()
        binding = DialogGuide4Binding.bind(this.contentView)
        binding.tvTips.setOnSingleListener {
            actionCallback.invoke()
            dismiss()
        }
        binding.root.postDelayed({
                binding.fingerView.visible()
            }, 2000)
    }
}