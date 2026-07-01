package com.clean.wood.data.model

import android.graphics.drawable.Drawable

/**
 * @param usageSize Unit is KB
 */
data class AppItem(
    val icon: Drawable,
    val appName: String,
    val installTime: Long,
    val usageSize: Long,
    var select: Boolean
)