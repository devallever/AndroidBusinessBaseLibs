package com.allever.android.card.text.pic.text.util

import android.content.Context
import android.util.TypedValue

object BarHelper {


    fun getActionBarHeight(context: Context): Int {
        val tv = TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            val result = TypedValue.complexToDimensionPixelSize(
                tv.data, context.getResources().getDisplayMetrics()
            );
            return result
        }
        return 0;
    }

}