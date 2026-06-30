package com.step.wincash.utils

import android.content.res.Resources
//
//val screenWidth:Int
//    get() = Resources.getSystem().displayMetrics.widthPixels
//
//val screenHeight:Int
//    get() = Resources.getSystem().displayMetrics.heightPixels

/**
 * 获取状态栏高度
 * 使用Resources.getIdentifier查找status_bar_height资源，避免使用过时的API
 */
val statusBarHeight: Int
    get() {
        val resources = Resources.getSystem()
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            // 默认值，通常在大多数设备上足够使用
            0
        }
    }