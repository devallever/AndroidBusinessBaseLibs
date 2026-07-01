package com.allever.video.editor.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter
import androidx.annotation.ColorRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout

import com.android.absbase.App
import com.allever.video.editor.R
import com.allever.video.editor.function.font.FontHelper
import com.allever.video.editor.function.online.LocalDataBean


class FontView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var mSelView: CircleBgView
    private var mIcon: ImageView
    private var mProgress: CircleFillProgressView
    private var mDownloadTipIcon: ImageView
    private var mHighlightColor: Int = 0

    var fontBean: LocalDataBean? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.font_item, this, true)
        mIcon = findViewById(R.id.iv_icon)
        mProgress = findViewById<View>(R.id.progress_view) as CircleFillProgressView
        mSelView = findViewById(R.id.sel_effect)
        mSelView.setColor(R.color.edit_font_item_bg_normal, R.color.edit_font_item_bg_select)
        mSelView.setFillStyle(true)
        mDownloadTipIcon = findViewById(R.id.iv_download_icon)
        mDownloadTipIcon.alpha = 0.5f
        if (isHardwareAccelerated) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        mHighlightColor = App.getContext().resources.getColor(R.color.accent_color)
    }

    fun updateData(bean: LocalDataBean, sel: Boolean) {
        fontBean = bean
        FontHelper.loadFontIcon(bean, mIcon)
        if (sel) {
            mSelView.setChecked(true)
        } else {
            mSelView.setChecked(false)
        }
    }

    fun setSel(sel: Boolean) {
        mSelView.setChecked(sel)
    }

    fun setColor(@ColorRes normalColor: Int, @ColorRes checkedColor: Int) {
        mSelView.setColor(normalColor, checkedColor)
    }

    fun updateProgress(percentage: Int) {
        var percentage = percentage
        if (percentage == 0) {
            percentage = 1
        }
        when {
            percentage < 0 -> {
                // 未下载状态
                mProgress.setProgress(0)
                mProgress.visibility = View.VISIBLE
                mDownloadTipIcon.visibility = View.VISIBLE
            }
            percentage < 100 -> {
                // 下载中
                mProgress.setProgress(percentage)
                mDownloadTipIcon.visibility = View.GONE
            }
            else -> {
                // 下载完成
                mProgress.visibility = View.GONE
                mDownloadTipIcon.visibility = View.GONE
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //内置的，已下载的
//        val fontBean = fontBean
//        if (fontBean == null
//                || fontBean.isBuildin
//                || FontHelper.checkExistTTF(fontBean)
//                || FontHelper.checkExistFontZip(fontBean)) {
//            mProgress.visibility = View.GONE
//            mDownloadTipIcon.visibility = View.GONE
//        } else {
//            mProgress.visibility = View.VISIBLE
//            mDownloadTipIcon.visibility = View.VISIBLE
//        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        super.dispatchDraw(canvas)
    }
}
