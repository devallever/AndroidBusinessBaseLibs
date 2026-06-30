package com.plinkopro.wincash.ui.dialog.guide

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.lxj.xpopup.impl.FullScreenPopupView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.databinding.DialogGuide1Binding
import com.plinkopro.wincash.utils.setOnSingleListener

class Guide1Dialog(context: Context, val targetView: View, val actionCallback: () -> Unit) :
    FullScreenPopupView(context) {

    //binding
    private lateinit var binding: DialogGuide1Binding
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_guide_1
    }

    override fun onCreate() {
        super.onCreate()
        binding = DialogGuide1Binding.bind(this.contentView)

        targetView.post {
            val t = IntArray(2)
            val p = IntArray(2)
            targetView.getLocationInWindow(t)
            (binding.imageView16.parent as View).getLocationInWindow(p)

            val start = t[0] - p[0]
            val top = t[1] - p[1]

            binding.imageView16.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    marginStart = start
                    topMargin = top

                    rightToRight = ConstraintLayout.LayoutParams.UNSET
                    bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                }
                layoutParams.width = targetView.width
                layoutParams.height = targetView.height
            }

            binding.root.requestLayout()
        }



        binding.getCoinsLL.setOnSingleListener {
            actionCallback.invoke()
            dismiss()
        }
    }
}