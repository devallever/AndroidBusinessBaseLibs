package app.flash.tunnel.vpn.lib.common.util

import android.annotation.SuppressLint
import java.util.concurrent.TimeUnit

object TimeManager {
    @SuppressLint("DefaultLocale")
    fun formatTimeStampToHMS(timestamp: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(timestamp)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)

    }
}