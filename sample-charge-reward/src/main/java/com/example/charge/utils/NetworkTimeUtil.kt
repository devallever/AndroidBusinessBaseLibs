package com.example.charge.utils

import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

object NetworkTimeUtil {

    private val ntpServerHost = arrayOf("ntp1.aliyun.com", "time.google.com", "time1.apple.com")
    private val NTP_TIME_OUT_MILLISECOND = 3000

    /**
     * 开始校准时间
     */
    suspend fun startCalibrateTime(): Long = withContext(Dispatchers.IO) {
        for (i in ntpServerHost.indices) {
            val time: Long = getTimeFromNtpServer(ntpServerHost[i])
            val datetime = getTimeStrByTimeMillis(time)
            if (time != -1L && datetime != "1970-01-01") {
                return@withContext time
            }
        }
        return@withContext -1L
    }

    /**
     * 从ntp服务器中获取时间
     *
     * @param ntpHost ntp服务器域名地址
     * @return 如果失败返回-1，否则返回当前的毫秒数
     */
    private fun getTimeFromNtpServer(ntpHost: String): Long {
        val client = NtpClient()
        val isSuccessful: Boolean = client.requestTime(ntpHost, NTP_TIME_OUT_MILLISECOND)
        return if (isSuccessful) {
            client.ntpTime
        } else -1
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeStrByTimeMillis(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return dateFormat.format(timeMillis)
    }
}