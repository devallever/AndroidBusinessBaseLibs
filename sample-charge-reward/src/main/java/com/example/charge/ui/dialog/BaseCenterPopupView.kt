package com.example.charge.ui.dialog

import android.content.Context
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils

open class BaseCenterPopupView(context: Context) : CenterPopupView(context) {

    override fun getMaxWidth(): Int {
        if (popupInfo == null) return 0
        return if (popupInfo.maxWidth == 0) XPopupUtils.getAppWidth(context)  else
            popupInfo.maxWidth
    }
}