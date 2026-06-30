package com.step.wincash.utils

import android.content.Context
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.animator.PopupAnimator
import com.lxj.xpopup.core.BasePopupView

private var xPopup: BasePopupView? = null
fun Context.showXPopup(
    dialog: BasePopupView,
    dismiss: Boolean = true,
    autoDismiss: Boolean = false,
    animator: PopupAnimator? = null
) {
    dismissXPopup(dismiss)
    xPopup = XPopup.Builder(this).apply {
        maxWidth(screenWidth)
        enableDrag(false)
        if (animator != null) {
            customAnimator(animator)
        }
        dismissOnBackPressed(autoDismiss)
        isClickThrough(false)
        isTouchThrough(false)
        autoFocusEditText(false)
        dismissOnTouchOutside(autoDismiss)
        /*setPopupCallback(object : SimpleCallback() {
            override fun onShow(popupView: BasePopupView?) {
                enableShowTips = false
            }

            override fun onDismiss(popupView: BasePopupView?) {
                *//*if (MkvUtil.getBool(AppConstant.KEY_GUIDE_SHOW_2, false)){
                    enableShowTips = true
                }*//*
            }
        })*/
    }.asCustom(dialog)
        .show()
}

fun dismissXPopup(dismiss: Boolean = true) {
    if (xPopup?.isShow == true && dismiss) {
        xPopup?.dismiss()
    }
}