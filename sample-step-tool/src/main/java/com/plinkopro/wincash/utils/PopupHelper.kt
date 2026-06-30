package com.plinkopro.wincash.utils

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView

object PopupHelper {
    fun createDialog(context: Context?, popupView: BasePopupView?, hasShadowBg: Boolean = true, dismissOnTouchOutside: Boolean = false): BasePopupView {
        return XPopup.Builder(context)
            .isDestroyOnDismiss(true)
            .positionByWindowCenter(true)
            .moveUpToKeyboard(true)
            .dismissOnBackPressed(false)
            .dismissOnTouchOutside(dismissOnTouchOutside)
            .hasShadowBg(hasShadowBg)
            .hasNavigationBar(false)
            .shadowBgColor("#BF000000".toColorInt())
            .asCustom(popupView)


    }
}