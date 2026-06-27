package com.clean.wood.data.model

import android.graphics.drawable.Drawable

/**
 * @param usageSize Unit is KB
 */
data class AppInfo(
    val icon: Drawable,
    val appName: String,
    val installTime: Long,
    val usageSize: Long,
)