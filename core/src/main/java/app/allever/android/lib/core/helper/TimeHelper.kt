package app.allever.android.lib.core.helper

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeHelper {

    /**
     * 根据传入的分钟转化为对应的天数小时分钟 如 : 1天12小时23分
     *
     * @param minute
     * 分钟
     * @return
     */
    fun getNumTime(minute: Long): String? {
        var days = ""
        days = when {
            minute < 60 -> {
                minute.toString() + "分钟"
            }

            minute < 1440 -> {
                val value = minute % 60
                (((minute - value) / 60).toString() + "小时"
                        + value.toString() + "分钟")
            }

            else -> {
                val minuteValue = minute % 60
                val value = (minute - minuteValue) / 60
                val hourValue = value % 24
                val dayValue = (value - hourValue) / 24
                (dayValue.toString() + "天" + hourValue.toString() + "小时"
                        + minuteValue + "分钟")
            }
        }
        return days
    }

    /**
     * 将毫秒格式化成 00:00:00
     */
    fun formatTime(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("HH:mm:ss")
        return simpleDateFormat.format(time)
    }

    /**
     * 将毫秒格式化成 00:00:00
     */
    fun formatTimeYYYY_MM_DD_HH_MM_SS(time: Long): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return simpleDateFormat.format(time)
    }

    @SuppressLint("DefaultLocale")
    fun formatTimeStampToHMS(timestamp: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timestamp) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timestamp) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(timestamp)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)

    }

    fun getSecondByDay(day: Int): Int {
        if (day < 0) {
            return 0
        }
        return day * getSecondByHour(24)
    }

    fun getSecondByHour(hour: Int): Int {
        if (hour < 0) {
            return 0
        }
        return hour * getSecondByMinute(60)
    }

    fun getSecondByMinute(minute: Int): Int {
        if (minute < 0) {
            return 0
        }
        return minute * 60
    }

    fun getSecondByTime(day: Int = 0, hour: Int = 0, minute: Int = 0, second: Int = 0): Int {
        return getSecondByDay(day) + getSecondByHour(hour) + getSecondByMinute(minute) + second
    }

    fun formatDateTime(time: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(time)
    }

    fun secondsToTimeString(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }


    fun formatSecondsToDaysHoursMinutesSeconds(seconds: Long): String {
        var seconds = seconds
        val days: Long = TimeUnit.SECONDS.toDays(seconds)
        seconds -= TimeUnit.DAYS.toSeconds(days)
        val hours: Long = TimeUnit.SECONDS.toHours(seconds)
        seconds -= TimeUnit.HOURS.toSeconds(hours)
        val minutes: Long = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d天%02d时%02d分%02d秒", days, hours, minutes, seconds)
    }

    fun secondsToTimeInterval(seconds: Int): String {
        val calendar = Calendar.getInstance()
        // 将当前时间减去秒数
        calendar.add(Calendar.SECOND, -seconds.toInt())

        val now = Calendar.getInstance()
        val years = now.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)
        val months = now.get(Calendar.MONTH) - calendar.get(Calendar.MONTH)
        val days = now.get(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)

        return when {
            years > 0 -> "$years 年前"
            months > 0 -> "$months 月前"
            days > 0 -> "$days 天前"
            else -> "1天前" // 或者其他适当的表示
        }
    }

    fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val timeDiff = now - timestamp

        val months = TimeUnit.MILLISECONDS.toDays(timeDiff) / 30
        val days = TimeUnit.MILLISECONDS.toDays(timeDiff) % 30

        return when {
            months > 0 -> "$months 个月前"
            days > 0 -> "$days 天前"
            else -> "刚刚"
        }
    }
}