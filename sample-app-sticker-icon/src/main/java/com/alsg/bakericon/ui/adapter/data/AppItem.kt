package com.alsg.bakericon.ui.adapter.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class AppItem(
    var name: String = "",
    var pkg: String = "",
    var iconBitmap: Bitmap? = null,
    var iconDrawable: Drawable? = null,
    var icon: Int = 0,
    var launchActivity: String = ""
)