package com.allever.android.card.text.pic.text.util

import android.view.View
import android.view.ViewGroup

object ViewHelper {

    fun setMarginTop(view: View, marginTop: Int) {
        view.post {
            val lp = view.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = marginTop
            view.layoutParams = lp
        }
    }

    fun setMarginBottom(view: View, marginBotom: Int) {
        view.post {
            val lp = view.layoutParams as ViewGroup.MarginLayoutParams
            lp.bottomMargin = marginBotom
            view.layoutParams = lp
        }
    }

}