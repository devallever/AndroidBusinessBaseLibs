package com.allever.android.card.text.pic.text.util

import java.text.SimpleDateFormat
import java.util.Locale

object TimeHelper {

    /**
     * 将毫秒格式化
     */
    fun formatTime(time: Long, pattern: String): String {
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.US)
        return simpleDateFormat.format(time)
    }
}