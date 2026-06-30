package com.plinkopro.wincash.ui.dialog.guide

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.createBitmap
import androidx.core.view.updateLayoutParams
import com.lxj.xpopup.impl.FullScreenPopupView
import com.plinkopro.wincash.R
import com.plinkopro.wincash.databinding.DialogGuide1Binding
import com.plinkopro.wincash.databinding.DialogGuide2Binding
import com.plinkopro.wincash.databinding.DialogGuide5Binding
import com.plinkopro.wincash.ui.widget.CurrencyView
import com.plinkopro.wincash.utils.setOnSingleListener

class Guide2Dialog(context: Context, val targetView: View, val actionCallback: () -> Unit): FullScreenPopupView(context) {

    //binding
    private lateinit var binding: DialogGuide2Binding
    override fun getImplLayoutId(): Int {
        return R.layout.dialog_guide_2
    }

    override fun onCreate() {
        super.onCreate()
        binding = DialogGuide2Binding.bind(this.contentView)

        handleHighLightViewBaseOnTarget(targetView)

        binding.highLightView.setOnSingleListener {
            actionCallback.invoke()
            dismiss()
        }
    }

    private fun handleHighLightViewBaseOnTarget(targetView: View){
        //显示高亮图片
        val bitmap = createBitmap(targetView.width, targetView.height)
        targetView.draw(Canvas(bitmap))
        binding.highLightView.setImageBitmap(bitmap)


        //计算摆放的位置
        val targetLocation = IntArray(2)
        targetView.getLocationOnScreen(targetLocation)
        val targetX = targetLocation[0]
        val targetY = targetLocation[1]

        val parentLocation = IntArray(2)
        val parentView  = binding.highLightView.parent as ConstraintLayout
        parentView.getLocationOnScreen(parentLocation)
        val parentX = parentLocation[0]
        val parentY = parentLocation[1]

        val relativeX = targetX - parentX
        val relativeY = targetY - parentY

        // 使用ConstraintSet动态设置约束
        val constrainSet = ConstraintSet()
        constrainSet.clone(parentView)

        //设置新的约束
        constrainSet.connect(binding.highLightView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, relativeX)
        constrainSet.connect(binding.highLightView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, relativeY)

        //应用新约束
        constrainSet.applyTo(parentView)

    }
}