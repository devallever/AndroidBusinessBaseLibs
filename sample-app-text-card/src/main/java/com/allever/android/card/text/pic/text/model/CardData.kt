package com.allever.android.card.text.pic.text.model

import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.util.log

class CardData {
    //card info
    var iconResId: Int = R.drawable.icon_abstract_01
    var dateFormatType = DateFormat.FORMAT_EE_M_D_Y_HH_MM
    var title = ""
    var text = ""
    var author = ""
    var wordCountFormatType = WordCountFormat.FORMAT_COUNT_WORD
    var qrTitle = ""
    var qrDesc = ""

    //template
    var templateName = ""

    //bgColor
    private var bgColorNameMap = mutableMapOf<String, String>()
    private var bgColorTypeMap = mutableMapOf<String, Int>()

    fun setBgColorName(colorName: String) {
        bgColorNameMap[TemplateManager.currentTemplate.getTemplateName()] = colorName
    }

    fun setBgColorType(colorType: Int) {
        log("setBgColorType: ${colorType}")
        bgColorTypeMap[TemplateManager.currentTemplate.getTemplateName()] = colorType
    }

    fun getBgColorName(): String {
        return bgColorNameMap[TemplateManager.currentTemplate.getTemplateName()] ?: ""
    }

    fun getBgColor(): String {
        return TemplateManager.currentTemplate.getBgColorData().colorValue[1]
    }

    fun getBgColorType(): Int {
        val result = bgColorTypeMap[TemplateManager.currentTemplate.getTemplateName()]
        log("getBgColorType: ${result}")
        return result ?: 0
    }

    //swich
    var switchIcon = true
    var switchDate = true
    var switchTitle = true
    var switchText = true
    var switchQuote = true
    var switchCount = true
    var switchQrCode = true
    var switchWaterMark = true

}