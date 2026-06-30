package com.plinkopro.wincash.ui.dialog.guide

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import com.lxj.xpopup.impl.FullScreenPopupView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.databinding.DialogGuide5Binding
import com.plinkopro.wincash.utils.setOnSingleListener

class Guide5Dialog(context: Context, val targetView: View, val actionCallback: () -> Unit): FullScreenPopupView(context) {

    //binding
    private lateinit var binding: DialogGuide5Binding
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_guide_5
    }

    override fun onCreate() {
        super.onCreate()
        binding = DialogGuide5Binding.bind(this.contentView)

        targetView.post {
            val location = IntArray(2)
            targetView.getLocationOnScreen(location)
            val targetX = (location[0]).toFloat()
            val targetY = (location[1]).toFloat()
            val params = RelativeLayout.LayoutParams(targetView.width, targetView.height)
            params.leftMargin = targetX.toInt()
            params.topMargin = targetY.toInt()
            binding.tvCashOut1.layoutParams = params
        }

        binding.tvCashOut1.setOnSingleListener {
            actionCallback.invoke()
            dismiss()
        }
    }
}