package com.allever.android.card.text.pic.text.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.allever.android.card.text.pic.text.App

class StatusBarUtil {
    companion object {
        fun fixStatusBar(view: View) {
            view.post {
                val lp = view.layoutParams as ViewGroup.MarginLayoutParams
                lp.topMargin = getStatusBarHeight(App.context)
                view.layoutParams = lp
            }
        }

        fun getStatusBarHeight(context: Context): Int {
            var result = 0
            val resourceId =
                context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }
    }


}