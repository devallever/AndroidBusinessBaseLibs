package com.plinkopro.wincash.base

import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils

open class BaseCenterPopupView(context: Context) : CenterPopupView(context) {
    // 铺满宽度 而不是原来的铺满0.85的宽度
    override fun getMaxWidth(): Int {
        if (popupInfo == null) return 0
        return if (popupInfo.maxWidth == 0) XPopupUtils.getAppWidth(context) else popupInfo.maxWidth
    }
}