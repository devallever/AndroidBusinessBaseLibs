package com.allever.android.card.text.pic.text.model

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.viewbinding.ViewBinding

abstract class TemplateModel<VB : ViewBinding> {


    var mBinding: VB? = null
    var name: String = ""
    var cover: Int = -1
    var selected: Boolean = false
    lateinit var bgColor: MutableList<TemplateBgColor>//0:light  1:dark

    fun getBgColorData(): ColorData {
        getTemplateBgColor()[TextCardCore.cardData.getBgColorType()].colorDataList.map {
            if (it.selected) {
                return it
            }
        }
        return getTemplateBgColor()[0].colorDataList[0]
    }

    init {
        initView()
        name = getTemplateName()
        cover = getTemplateCover()
//        bgColor = getTemplateBgColor()
    }

    abstract fun inflateView(): VB

    abstract fun getTemplateName(): String

    abstract fun getTemplateCover(): Int

    abstract fun getTemplateBgColor(): MutableList<TemplateBgColor>

    abstract fun showOrHideIcon(show: Boolean)
    abstract fun showOrHideDate(show: Boolean)
    abstract fun showOrHideTitle(show: Boolean)
    abstract fun showOrHideContent(show: Boolean)
    abstract fun showOrHideAuthor(show: Boolean)
    abstract fun showOrHideWordCount(show: Boolean)
    abstract fun showOrHideQrCode(show: Boolean)
    abstract fun showOrHideMark(show: Boolean)

    abstract fun updateCardBg(isDark: Boolean, data: ColorData)

    abstract fun getIconView(): ImageView
    abstract fun getDateView(): TextView
    abstract fun getTitleView(): EditText
    abstract fun getContentView(): EditText
    abstract fun getAuthorView(): EditText
    abstract fun getWordCountView(): TextView

    //    abstract fun getQrCodeView(): ImageView
//    abstract fun getQrCodeTitleView(): TextView
//    abstract fun getQrCodeDescView(): TextView
    abstract fun getTemplateContentView(): View


    fun initView() {
        mBinding = inflateView()
    }

    fun destroyView() {
        mBinding = null
    }

}
