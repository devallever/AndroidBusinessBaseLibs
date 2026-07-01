package com.step.wincash.ui.dialog.guide

import android.content.Context
import com.lxj.xpopup.impl.FullScreenPopupView
import com.step.wincash.R
import com.step.wincash.databinding.DialogGuide4Binding
import com.step.wincash.utils.setOnSingleListener
import com.step.wincash.utils.visible

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