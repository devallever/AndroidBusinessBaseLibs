package com.step.wincash.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import com.step.wincash.base.BaseApplication
import kotlin.math.ceil

/**
 * dip to px
 */
fun dp2px(dipValue: Float, context: Context = BaseApplication.instance): Int {

    return if (dipValue == 0f) 0 else ceil(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dipValue, context.resources.displayMetrics
        )).toInt()
}

/**
 * px to dip
 */
fun px2dp(pxValue: Int, context: Context = BaseApplication.instance): Float {
    if (pxValue == 0)
        return 0f
    val scale = context.resources.displayMetrics.density
    return pxValue / scale + 0.5f
}

/**
 * sp to px
 */
fun sp2px(spValue: Float, context: Context = BaseApplication.instance): Int {
    return if (spValue == 0f) 0 else ceil(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, spValue, context.resources.displayMetrics
        )).toInt()
}

/**
 * px to sp
 */
fun spFromPx(pxValue: Int, context: Context = BaseApplication.instance): Float {
    if (pxValue == 0)
        return 0f
    val fontScale = context.resources.displayMetrics.scaledDensity
    return pxValue / fontScale + 0.5f
}


fun getWindowWidth(context: Context = BaseApplication.instance): Int {
    return context.resources.displayMetrics.widthPixels
}

val screenWidth: Int
    get() = Resources.getSystem().displayMetrics.widthPixels
//
//val screenHeight: Int
//    get() = Resources.getSystem().displayMetrics.heightPixels
//
//fun Context.realScreenHeight(): Int {
//    val displayMetrics = resources.displayMetrics
//    (this as Activity).windowManager.defaultDisplay.getRealMetrics(displayMetrics)
//    return if (displayMetrics.heightPixels==0) screenHeight else displayMetrics.heightPixels
//}
//
//fun Context.realScreenWidth(): Int {
//    val displayMetrics = resources.displayMetrics
//    (this as Activity).windowManager.defaultDisplay.getRealMetrics(displayMetrics)
//    return if (displayMetrics.widthPixels==0) screenWidth else displayMetrics.widthPixels
//}

@SuppressLint("DiscouragedApi", "InternalInsetResource")
fun getStatusBarHeight(context: Context = BaseApplication.instance): Int {
    var height = 0
    val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resId > 0) {
        height = context.resources.getDimensionPixelSize(resId)
    }

    if (height == 0) {
        height = dp2px(20f, context)
    }

    return height
}

fun getNavigationBarHeight(context: Context = BaseApplication.instance): Int {
    var height = 0
    val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0){
        height = context.resources.getDimensionPixelSize(resourceId)
    }
    return height
}