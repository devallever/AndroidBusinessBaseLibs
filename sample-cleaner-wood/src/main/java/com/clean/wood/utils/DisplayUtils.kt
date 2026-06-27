package com.clean.wood.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import com.clean.wood.WoodApp

object DisplayUtils {

    @SuppressLint("InternalInsetResource")
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    @SuppressLint("InternalInsetResource")
    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun dip2px(dip: Int): Int {
        val displayMetrics = WoodApp.context.resources.displayMetrics
        val density = displayMetrics.density
        return (dip * density + 0.5f).toInt()
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        val resource = WoodApp.context.resources
        return resource.displayMetrics
    }

    fun getScreenWidth(): Int {
        return getDisplayMetrics().widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            return metrics.bounds.height()
        } else {
            val display = windowManager.defaultDisplay
            val point = Point()
            display.getRealSize(point)
            val mScreenH = point.y
            return mScreenH
        }

    }

}