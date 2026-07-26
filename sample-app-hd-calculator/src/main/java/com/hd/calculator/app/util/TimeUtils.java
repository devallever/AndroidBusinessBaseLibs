package com.hd.calculator.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    public static String formatTimestampToHHmm(long timestamp) {
        var date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatTimestampToYYYYDDMM(long timestamp) {
        var date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    //格式化时间 13.06.2025 23:45
    public static String formatTimestampToDDMMYYYYHHmm(long timestamp) {
        var date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return sdf.format(date);
    }

    /**
     * 获取当天的年、月、日信息
     *
     * @return int[3] 数组：0=年, 1=月（1-12）, 2=日（1-31）
     */
    public static int[] getTodayYMD() {
        return getYMD(System.currentTimeMillis(), TimeZone.getDefault());
    }

    /**
     * 获取指定时间戳的年、月、日信息（指定时区）
     *
     * @param timestamp 时间戳（毫秒）
     * @param timeZone  时区对象
     * @return int[3] 数组：0=年, 1=月（1-12）, 2=日（1-31）
     */
    public static int[] getYMD(long timestamp, TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(timestamp);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // Calendar的月份是0基的（0=1月）
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return new int[]{year, month, day};
    }

    /**
     * 获取指定时间戳的年、月、日信息（系统默认时区）
     *
     * @param timestamp 时间戳（毫秒）
     * @return int[3] 数组：0=年, 1=月（1-12）, 2=日（1-31）
     */
    public static int[] getYMD(long timestamp) {
        return getYMD(timestamp, TimeZone.getDefault());
    }

    /**
     * 获取当天07:00的时间戳（毫秒）- 使用系统默认时区
     *
     * @return 当天07:00的Unix时间戳（毫秒）
     */
    public static long getToday7amTimestamp() {
        return getToday7amTimestamp(TimeZone.getDefault());
    }

    /**
     * 获取当天07:00的时间戳（毫秒）- 指定时区
     *
     * @param timeZone 时区对象
     * @return 当天07:00的Unix时间戳（毫秒）
     */
    public static long getToday7amTimestamp(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);

        // 重置为今天07:00:00.000
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    /**
     * 获取当天07:00的时间戳（毫秒）- 使用时区ID
     *
     * @param timeZoneId 时区ID字符串（如"Asia/Shanghai"）
     * @return 当天07:00的Unix时间戳（毫秒）
     */
    public static long getToday7amTimestamp(String timeZoneId) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        return getToday7amTimestamp(timeZone);
    }

    /**
     * 获取指定日期（年月日）的7:00和次日7:00时间戳（系统默认时区）
     *
     * @param year  年份（如2023）
     * @param month 月份（1-12，1表示1月）
     * @param day   日期（1-31）
     * @return long[2] 数组：0=指定日期7点时间戳, 1=次日7点时间戳
     */
    public static long[] get7amTimestamps(int year, int month, int day) {
        return get7amTimestamps(year, month, day, TimeZone.getDefault());
    }

    /**
     * 获取指定日期（年月日）的7:00和次日7:00时间戳（指定时区）
     *
     * @param year     年份（如2023）
     * @param month    月份（1-12，1表示1月）
     * @param day      日期（1-31）
     * @param timeZone 时区对象
     * @return long[2] 数组：0=指定日期7点时间戳, 1=次日7点时间戳
     */
    public static long[] get7amTimestamps(int year, int month, int day, TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        // 注意：Calendar月份从0开始，0=1月
        cal.set(year, month - 1, day, 7, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return get7amTimestamps(cal.getTimeInMillis(), timeZone);
    }

    // 保留之前的方法不变（为了完整性展示）

    /**
     * 获取指定日期的7:00和次日7:00时间戳（系统默认时区）
     *
     * @param dateMillis 输入日期的任意时间戳（毫秒）
     * @return long[2] 数组：0=当天7点时间戳, 1=次日7点时间戳
     */
    public static long[] get7amTimestamps(long dateMillis) {
        return get7amTimestamps(dateMillis, TimeZone.getDefault());
    }

    /**
     * 获取指定日期的7:00和次日7:00时间戳（指定时区）
     *
     * @param dateMillis 输入日期的任意时间戳（毫秒）
     * @param timeZone   时区对象
     * @return long[2] 数组：0=当天7点时间戳, 1=次日7点时间戳
     */
    public static long[] get7amTimestamps(long dateMillis, TimeZone timeZone) {
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(dateMillis);
        // 重置为当天07:00:00.000
        setCalendarTo7am(cal);
        long today7am = cal.getTimeInMillis();

        // 计算次日07:00
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long nextDay7am = cal.getTimeInMillis();

        return new long[]{today7am, nextDay7am};
    }

    /**
     * 辅助方法：将Calendar设置为当天的07:00:00.000
     */
    private static void setCalendarTo7am(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * 获取当前日期的7:00和次日7:00时间戳
     *
     * @return long[2] 数组：0=当天7点时间戳, 1=次日7点时间戳
     */
    public static long[] getCurrentDay7amTimestamps() {
        return get7amTimestamps(getAdjustedTimestamp(System.currentTimeMillis()));
    }

    /**
     * 判断如果当前时间在当天凌晨 00:00 到 07:00 之间，则返回前一天的时间戳（固定返回前一天中午 12:00 的时间戳），否则直接返回原时间戳：
     *
     * @param currentTimestamp
     * @return
     */
    public static long getAdjustedTimestamp(long currentTimestamp) {
        // 使用系统默认时区创建 Calendar 实例
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimestamp);

        // 获取当前小时和分钟
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 检查是否在 00:00 - 07:00 区间
        if (hour < 7 || (hour == 7 && minute == 0)) {
            // 回溯到前一天
            calendar.add(Calendar.DATE, -1);
            // 固定设置时间为前一天中午 12:00
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return calendar.getTimeInMillis();
    }

    /**
     * 根据时间戳获取上一天的日期（格式: yyyy-MM-dd）
     *
     * @param timestampMillis 当前时间戳（毫秒）
     * @return 格式化的日期字符串 "2025-07-24"
     */
    public static String getPreviousDayDate(long timestampMillis) {
        // 创建日历实例并设置时区（默认使用设备时区）
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampMillis);

        // 减去一天
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        // 创建日期格式化对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 格式化日期并返回
        return sdf.format(calendar.getTime());
    }
}
