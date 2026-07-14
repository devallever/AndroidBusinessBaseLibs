package com.allever.android.card.text.pic.text.model

data class TemplateBgColor(
    var colorDataList: MutableList<ColorData>,
    var type: Int = COLOR_LIGHT,
) {

    companion object {
        const val COLOR_LIGHT = 0
        const val COLOR_DARK = 1
    }
}