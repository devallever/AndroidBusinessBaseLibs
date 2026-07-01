package com.step.wincash.business.withdraw.account

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.shape.layout.ShapeRecyclerView
import com.step.wincash.R
import com.step.wincash.ui.adapter.BrAccountTypeAdapter
import com.step.wincash.utils.dp2px
import com.step.wincash.utils.setOnSingleListener

object TipsPopupHelper {

    private var mTipsPopWindow: PopupWindow? = null
    fun show(activity: Context, targetView: View?, width: Int, content: String?) {
        if (mTipsPopWindow?.isShowing == true) {
            mTipsPopWindow?.dismiss()
        }
        mTipsPopWindow = PopupWindow(activity)
        val popupWindow = mTipsPopWindow?: return
        val tv = LayoutInflater.from(activity).inflate(R.layout.popup_wd_tips, null) as TextView
        popupWindow.contentView = tv
        popupWindow.width = width
        popupWindow.isFocusable = false
        popupWindow.isTouchable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popupWindow.setOnDismissListener {
            mTipsPopWindow = null
        }
        tv.text = content
        tv.setOnSingleListener {
            popupWindow.dismiss()
        }
        val measuredHeight = getTurTextViewHeight(tv,width,  dp2px(12f,activity), dp2px(18f,activity))
        popupWindow.showAsDropDown(targetView, dp2px(20f), -measuredHeight)
    }

    fun dismiss() {
        mTipsPopWindow?.dismiss()
        mTipsPopWindow = null
    }

    /**
     * 提前获取textView高度
     *
     * @param textView
     * @param textViewWidth
     * @param paddingTop
     * @param paddingBottom
     * @return
     */
    fun getTurTextViewHeight(textView: TextView, textViewWidth: Int, paddingTop: Int, paddingBottom: Int): Int {
        var measuredHeight = 0
        try {
            textView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            measuredHeight = textView.measuredHeight
            val lineCount = getTextViewLines(textView, textViewWidth)
            val lineHeight = textView.lineHeight
            val allLineHeight = lineCount * lineHeight
            val newMeasuredHeight = paddingTop + paddingBottom + allLineHeight
            if (newMeasuredHeight >= measuredHeight) {
                measuredHeight = newMeasuredHeight
            }
        } catch (e: Exception) {
        }
        return measuredHeight
    }

    /**
     * 提前获取textview行数
     */
    private fun getTextViewLines(textView: TextView, textViewWidth: Int): Int {
        val width = textViewWidth - textView.compoundPaddingLeft - textView.compoundPaddingRight
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getStaticLayout23(textView, width)
        } else {
            getStaticLayout(textView, width)
        }
        val lines = staticLayout.lineCount
        val maxLines = textView.maxLines
        if (maxLines > lines) {
            return lines
        }
        return maxLines
    }

    /**
     * sdk>=23
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun getStaticLayout23(textView: TextView, width: Int): StaticLayout {
        val builder = StaticLayout.Builder.obtain(
            textView.text,
            0, textView.text.length, textView.paint, width
        )
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
            .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
            .setIncludePad(textView.includeFontPadding)
            .setBreakStrategy(textView.breakStrategy)
            .setHyphenationFrequency(textView.hyphenationFrequency)
            .setMaxLines(if (textView.maxLines == -1) Int.MAX_VALUE else textView.maxLines)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setJustificationMode(textView.justificationMode)
        }
        if (textView.ellipsize != null && textView.keyListener == null) {
            builder.setEllipsize(textView.ellipsize)
                .setEllipsizedWidth(width)
        }
        return builder.build()
    }

    /**
     * sdk<23
     */
    private fun getStaticLayout(textView: TextView, width: Int): StaticLayout {
        return StaticLayout(
            textView.text,
            0, textView.text.length,
            textView.paint, width, Layout.Alignment.ALIGN_NORMAL,
            textView.lineSpacingMultiplier,
            textView.lineSpacingExtra, textView.includeFontPadding, textView.ellipsize,
            width
        )
    }

    var accountTypePopupWindow: PopupWindow ?= null
    var allTypes = listOf<String>()
    fun showBrAccountType(context: Context, targetView: View,currentType: String?, allTypes: List<String>,listener: (String) -> Unit):PopupWindow?{

        if (accountTypePopupWindow == null || this.allTypes != allTypes) {
            val x: Int = dp2px(4f, context)
            val popupWindow: PopupWindow = PopupWindow(context)
            val recyclerView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_br_account_type, null) as ShapeRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            popupWindow.contentView = recyclerView
            popupWindow.width = targetView.measuredWidth - x * 2
            // 设置高度
            if (allTypes.size <= 7) {
                // 如果item数量小于等于7个，使用WRAP_CONTENT
                popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT
            } else {
                popupWindow.height = dp2px(31.7f, context) * 7
            }
//            popupWindow.isFocusable = true
//            popupWindow.isTouchable = true
            popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            val typeAdapter = BrAccountTypeAdapter { type ->
                listener.invoke(type)
                popupWindow.dismiss()
                accountTypePopupWindow = null
                this.allTypes = listOf()
            }
            recyclerView.adapter = typeAdapter
            typeAdapter.show(currentType, allTypes)
            popupWindow.showAsDropDown(targetView, x, 0)
            this.allTypes = allTypes
            this.accountTypePopupWindow = popupWindow
        }else{
            if (accountTypePopupWindow?.isShowing == true) {
                // 如果弹窗正在显示，则关闭它
                accountTypePopupWindow?.dismiss()
                accountTypePopupWindow = null
            }
        }
        return accountTypePopupWindow
    }

}
