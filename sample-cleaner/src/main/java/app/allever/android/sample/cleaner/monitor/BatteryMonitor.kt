package app.allever.android.sample.cleaner.monitor

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import app.allever.android.lib.core.app.App

/**
 * 电池监控
 *
 * 对应文档"电池监控"章节。
 * 数据来源：BatteryManager API + ACTION_BATTERY_CHANGED
 *
 * 监控指标：电量百分比、充电状态、电池温度、健康状态
 */
object BatteryMonitor {

    data class BatteryInfo(
        val level: Int,                 // 电量百分比 0-100
        val scale: Int,                 // 总刻度（通常 100）
        val isCharging: Boolean,        // 是否在充电
        val chargeStatus: String,       // 充电状态描述
        val temperature: Float,         // 温度（摄氏度）
        val voltage: Int,               // 电压(mV)
        val health: String              // 健康状态描述
    ) {
        val formattedLevel: String get() = "$level%"
        val formattedTemp: String get() = "%.1f°C".format(temperature)
    }

    /**
     * 获取当前电池信息
     */
    fun getBatteryInfo(): BatteryInfo {
        val context = App.context
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager? ?: return createDefaultInfo()

        val level: Int
        val scale: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            scale = 100
        } else {
            // 兼容旧版本
            val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 0) ?: 0
            scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
        }

        val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            batteryManager.isCharging
        } else {
            false
        }

        val intent = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra("status", 0) ?: 0
        val chargeStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
            BatteryManager.BATTERY_STATUS_FULL -> "已充满"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
            else -> "未知"
        }

        val temp = intent?.getIntExtra("temperature", 0) ?: 0
        val temperature = temp / 10f // 原始单位是 0.1°C

        val voltage = intent?.getIntExtra("voltage", 0) ?: 0

        val healthCode = intent?.getIntExtra("health", 0) ?: 0
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
            BatteryManager.BATTERY_HEALTH_DEAD -> "损坏"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "过压"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "故障"
            else -> "正常"
        }

        return BatteryInfo(
            level = level.coerceIn(0, scale),
            scale = scale,
            isCharging = isCharging,
            chargeStatus = chargeStatus,
            temperature = temperature,
            voltage = voltage,
            health = health
        )
    }

    private fun createDefaultInfo(): BatteryInfo = BatteryInfo(
        level = 0, scale = 100, isCharging = false,
        chargeStatus = "未知", temperature = 0f, voltage = 0, health = "未知"
    )
}
