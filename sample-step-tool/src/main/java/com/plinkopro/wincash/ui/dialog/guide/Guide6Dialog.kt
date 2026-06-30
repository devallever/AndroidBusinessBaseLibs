package com.plinkopro.wincash.ui.dialog.guide

import android.content.Context
import com.lxj.xpopup.impl.FullScreenPopupView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.databinding.DialogGuide6Binding
import com.plinkopro.wincash.utils.setOnSingleListener
import com.plinkopro.wincash.utils.visible

class Guide6Dialog(context: Context, val actionCallback: () -> Unit): FullScreenPopupView(context) {
    //binding
    private lateinit var binding: DialogGuide6Binding

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_guide_6
    }

    override fun onCreate() {
        super.onCreate()
        binding = DialogGuide6Binding.bind(this.contentView)
        binding.btnConfirm.setOnSingleListener {
            actionClose()
        }
        binding.ivClose.setOnSingleListener {
            actionClose()
        }
        binding.root.postDelayed({
            binding.fingerView.visible()
        }, 2000)
    }

    private fun actionClose() {
        actionCallback.invoke()
        dismiss()
    }
}