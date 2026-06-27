package com.clean.wood.utils

import java.text.SimpleDateFormat
import java.util.Locale

object TimeUtils {

    fun formatTimeYYYY_MM_DD(time: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(time)
    }
}