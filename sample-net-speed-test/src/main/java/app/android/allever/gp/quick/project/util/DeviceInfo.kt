package app.android.allever.gp.quick.project.util

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.DeviceHelper
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.core.helper.TimeHelper


object DeviceInfo {

    //基本信息
    fun getScerrnWidth() = DisplayHelper.getScreenWidth()
    fun getScreenHeight() = DisplayHelper.getScreenHeight()

    fun getScreenResolution() = DisplayHelper.getScreenResolution(App.context)

    fun getFactory() = DeviceHelper.getDeviceBrand()

    fun getModel() = DeviceHelper.getDeviceModel()

    fun getAndroidVersion() = Build.VERSION.RELEASE

    fun getCpu(): String {
        return (Build.CPU_ABI)
    }

    fun getBootTime(): String {
        val time = SystemClock.elapsedRealtime()
        return TimeHelper.formatSecondsToDaysHoursMinutesSeconds(time / 1000)
    }

    fun getRomPublishDate(): String {
        // 尝试从系统属性中获取ROM发布日期
        val buildDate = Build.TIME
        return if (buildDate != 0L) {
            // 将时间戳转换为日期格式
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
            dateFormat.format(buildDate)
        } else {
            "未知"
        }
    }

    //网络
    fun getNetWorkType() = InternetUtil.getNetworkStateName(App.context)

    fun getInternalIp() = IPHelper.getInternalIp()

    //内存信息

    fun getMemoryUsageRate(): Int {
        val activityManager = App.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        // memoryInfo.totalMem 是系统总内存
        // memoryInfo.availMem 是系统当前可用内存
        // 内存使用率 = 1 - 系统当前可用内存 / 系统总内存
        return ((1F - memoryInfo.availMem / memoryInfo.totalMem.toFloat()) * 100).toInt()
    }

    /**
     * GB
     */
    fun getMemorySize(): Int {
        val activityManager = App.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return (memoryInfo.totalMem / 1024 / 1024 / 1024).toInt()
    }

    fun getAvailMemory(): Int {
        val activityManager = App.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return (memoryInfo.availMem / 1024 / 1024 / 1024).toInt()
    }


    //电池
    fun getBatteryLeft(): Int {
        val batteryManager = App.context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel: Int = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return batteryLevel
    }

    //存储
    // 获取已使用的存储空间大小（单位：GB）
    val usedSpace = (Environment.getDataDirectory().totalSpace - Environment.getDataDirectory().freeSpace) / 1024 / 1024 / 1024

    // 获取总的存储空间大小（单位：GB）
    val totalSpace = (Environment.getDataDirectory().totalSpace) / 1024 / 1024 / 1024

    // 获取剩余空间大小（单位：GB）
    val freeSpace = (Environment.getDataDirectory().freeSpace) / 1024 / 1024 / 1024

    val spaceLeft = ((freeSpace.toFloat() / totalSpace.toFloat()) * 100).toInt()


}