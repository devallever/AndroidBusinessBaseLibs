package com.example.charge.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import kotlin.apply

object TimeUtil {

    @SuppressLint("SimpleDateFormat")
    fun  getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd")
        return dateFormat.format(Calendar.getInstance().time)
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeStrByTimeMillis(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return dateFormat.format(timeMillis)
    }

    @SuppressLint("SimpleDateFormat")
    fun  getCurrentTimeYMDHM(): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return dateFormat.format(Calendar.getInstance().time)
    }

    /**
     * 判断两个时间戳是否是同一天
     * @param timestamp1 第一个时间戳（毫秒）
     * @param timestamp2 第二个时间戳（毫秒）
     * @param timeZone 时区，默认为系统默认时区
     * @return 如果是同一天返回true，否则返回false
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long = System.currentTimeMillis(), timeZone: TimeZone = TimeZone.getDefault()): Boolean {
        val calendar1 = Calendar.getInstance(timeZone).apply {
            timeInMillis = timestamp1
        }

        val calendar2 = Calendar.getInstance(timeZone).apply {
            timeInMillis = timestamp2
        }

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
                calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * 格式化时间戳为yyyy-MM-dd
     * @param time 时间戳（毫秒）
     * @return 格式化后的时间字符串
     */
    fun formatTimeYYYY_MM_dd(time: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(time)
    }

}