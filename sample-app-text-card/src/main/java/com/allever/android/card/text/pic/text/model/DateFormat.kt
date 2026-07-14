package com.allever.android.card.text.pic.text.model

import com.allever.android.card.text.pic.text.util.TimeHelper
import com.allever.android.card.text.pic.text.util.log

class DateFormat {
    companion object {
        const val FORMAT_EE_M_D_Y_HH_MM = "EE, MMMM dd, yyyy, HH:mm"
        const val FORMAT_M_D_Y_HH_MM = "MMMM dd, yyyy, HH:mm"
        const val FORMAT_M_D_Y_EE = "MMMM dd, yyyy EE"
        const val FORMAT_M_D_Y = "MMMM dd, yyyy"
        const val FORMAT_M_Y = "MMMM yyyy"
        const val FORMAT_M_D = "MMMM dd"
        const val FORMAT_M_D_HH_MM = "MMMM dd, HH:mm"
        const val FORMAT_E = "EEEE"
        const val FORMAT_HH_MM = "HH:mm"
        const val FORMAT_HH_MM_Z = "HH:mm ZZZZ"

        val FORMAT_LIST = mutableListOf(
            FORMAT_EE_M_D_Y_HH_MM,
            FORMAT_M_D_Y_HH_MM,
            FORMAT_M_D_Y_EE,
            FORMAT_M_D_Y,
            FORMAT_M_Y,
            FORMAT_M_D,
            FORMAT_M_D_HH_MM,
            FORMAT_E,
            FORMAT_HH_MM,
            FORMAT_HH_MM_Z
        )

        fun format(pattern: String, time: Long = System.currentTimeMillis()): String {
            val result = TimeHelper.formatTime(time, pattern)
            log("format time: $result")
            return result
        }
    }
}