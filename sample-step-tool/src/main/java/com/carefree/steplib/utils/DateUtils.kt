package com.carefree.steplib.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * @Description: 时间工具类（时间格式转换方便类）
 */
object DateUtils {
    private val SIMPLE_DATE_FORMAT = ThreadLocal<SimpleDateFormat>()
    private val calendarH: Calendar = Calendar.getInstance()
    private val dayNames = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
    const val DATE_FORMAT_YMD = "yyyy-MM-dd"
    const val DATE_FORMAT_MD = "MM月dd日"

    private val dateFormat: SimpleDateFormat
        get() {
            var df = SIMPLE_DATE_FORMAT.get()
            if (df == null) {
                df = SimpleDateFormat(DATE_FORMAT_YMD, Locale.US)
                SIMPLE_DATE_FORMAT.set(df)
            }
            return df
        }

    /**
     * 返回一定格式的当前时间
     *
     * @param pattern "yyyy-MM-dd HH:mm:ss E"
     * @return
     */
    fun getCurrentDate(pattern: String = DATE_FORMAT_YMD): String {
        dateFormat.applyPattern(pattern)
        val date = Date(System.currentTimeMillis())
        val dateString = dateFormat.format(date)
        return dateString
    }

    fun getDateMillis(dateString: String, pattern: String): Long {
        var millionSeconds: Long = 0
        dateFormat.applyPattern(pattern)
        try {
            millionSeconds = dateFormat.parse(dateString)?.time ?: 0
        } catch (e: ParseException) {
            e.printStackTrace()
        } // 毫秒


        return millionSeconds
    }

    /**
     * 格式化输入的millis
     *
     * @param millis
     * @param pattern yyyy-MM-dd HH:mm:ss E
     * @return
     */
    fun dateFormat(millis: Long, pattern: String): String {
        dateFormat.applyPattern(pattern)
        val date = Date(millis)
        val dateString = dateFormat.format(date)
        return dateString
    }

    /**
     * 将dateString原来old格式转换成new格式
     *
     * @param dateString
     * @param oldPattern yyyy-MM-dd HH:mm:ss E
     * @param newPattern
     * @return oldPattern和dateString形式不一样直接返回dateString
     */
    fun dateFormat(
        dateString: String, oldPattern: String,
        newPattern: String
    ): String {
        val millis = getDateMillis(dateString, oldPattern)
        if (0L == millis) {
            return dateString
        }
        val date = dateFormat(millis, newPattern)
        return date
    }

    val thisWeekMonday: Long
        /**
         * 获取本周一的零点时间
         *
         * @return
         */
        get() {
            calendarH.timeInMillis = System.currentTimeMillis()
            // 获得当前日期是一个星期的第几天
            val dayWeek = calendarH[Calendar.DAY_OF_WEEK]
            if (1 == dayWeek) {
                calendarH.add(Calendar.DAY_OF_MONTH, -1)
            }
            // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
            calendarH.firstDayOfWeek = Calendar.MONDAY
            // 获得当前日期是一个星期的第几天
            val day = calendarH[Calendar.DAY_OF_WEEK]
            // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
            calendarH.add(Calendar.DATE, calendarH.firstDayOfWeek - day)
            //return cal.getTime();
            calendarH[Calendar.HOUR_OF_DAY] = 0
            calendarH[Calendar.MINUTE] = 0
            calendarH[Calendar.SECOND] = 0
            calendarH[Calendar.MILLISECOND] = 0
            return calendarH.timeInMillis
        }

    val thisWeekSunday: Long
        /**
         * 获取本周日23：59：59
         *
         * @return
         */
        get() {
            val cal = Calendar.getInstance()
            cal[Calendar.DAY_OF_WEEK] = Calendar.SUNDAY
            cal[Calendar.HOUR_OF_DAY] = 23
            cal[Calendar.MINUTE] = 59
            cal[Calendar.SECOND] = 59
            cal[Calendar.MILLISECOND] = 999
            return cal.timeInMillis
        }


    /**
     * 根据时间戳获取星期几
     *
     * @param startDate
     * @return
     */
    fun getThisWeekDay(startDate: String): String {
        calendarH.timeInMillis = getDateMillis(startDate, DATE_FORMAT_YMD)
        return dayNames[calendarH[Calendar.DAY_OF_WEEK] - 1]
    }


    fun getWeekAndDay(time: Long): String {
        calendarH.timeInMillis = time
        val dayName = dayNames[calendarH[Calendar.DAY_OF_WEEK] - 1]
        dateFormat.applyPattern("MMMM d")
        return dayName + "(" + dateFormat.format(calendarH.time) + ")"
    }


    /**
     * 获取《October 21》格式时间
     * @param time 时间戳
     * @return
     */
    fun getMonthDay(time: Long): String {
        calendarH.timeInMillis = time
        dateFormat.applyPattern("MMMM d")
        return dateFormat.format(calendarH.time)
    }


    fun getMonthStr(time: Long): String {
        calendarH.timeInMillis = time
        dateFormat.applyPattern("MMMM")
        return dateFormat.format(calendarH.time)
    }

    /**
     * 获取当前时间小时数
     * @param timestamp
     * @return
     */
    fun getHourFromTimestamp(timestamp: Long): Int {
        calendarH.timeInMillis = timestamp
        return calendarH[Calendar.HOUR_OF_DAY]
    }

    val thisMonthDay: Date
        /**
         * 获取当月第一天
         *
         * @return
         */
        get() {
            calendarH.timeInMillis = System.currentTimeMillis()
            calendarH[Calendar.DAY_OF_MONTH] = 1
            return calendarH.time
        }


    val nextTimesMorning: Long
        /**
         * 获取次日的零点时间
         *
         * @return
         */
        get() {
            calendarH.timeInMillis = System.currentTimeMillis()
            calendarH.add(Calendar.DATE, 1)
            calendarH[Calendar.HOUR_OF_DAY] = 0
            calendarH[Calendar.SECOND] = 0
            calendarH[Calendar.MINUTE] = 0
            calendarH[Calendar.MILLISECOND] = 0
            return (calendarH.timeInMillis)
        }
}
