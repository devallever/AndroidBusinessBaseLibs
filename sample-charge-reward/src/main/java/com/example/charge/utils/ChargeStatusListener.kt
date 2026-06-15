package com.example.charge.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build

/**
 * 充电状态监听器工具类
 * 用于监听设备充电状态的变化
 */
class ChargeStatusListener(private val context: Context) {

    // 充电状态回调接口
    interface OnChargeStatusChangeListener {
        fun onChargeStatusChanged(
            isCharging: Boolean,
            chargeType: String,
            batteryLevel: Int,
            batteryTemperature: Float,
            batteryVoltage: Int
        )
    }

    private var chargeStatusReceiver: ChargeStatusReceiver? = null
    private var onChargeStatusChangeListener: OnChargeStatusChangeListener? = null

    /**
     * 开始监听充电状态
     */
    fun startListening(listener: OnChargeStatusChangeListener) {
        this.onChargeStatusChangeListener = listener

        // 创建广播接收器
        chargeStatusReceiver = ChargeStatusReceiver()

        // 注册广播接收器
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                chargeStatusReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(chargeStatusReceiver, intentFilter)
        }

        // 立即获取当前充电状态
        val batteryIntent =
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryIntent?.let {
            processBatteryIntent(it)
        }
    }

    /**
     * 停止监听充电状态
     */
    fun stopListening() {
        try {
            chargeStatusReceiver?.let {
                context.unregisterReceiver(it)
            }
        } catch (e: Exception) {
            log("ChargeStatusListener", "Error unregistering receiver: ${e.message}")
        } finally {
            chargeStatusReceiver = null
            onChargeStatusChangeListener = null
        }
    }

    /**
     * 广播接收器内部类
     */
    private inner class ChargeStatusReceiver : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                when (it.action) {
                    Intent.ACTION_BATTERY_CHANGED,
                    Intent.ACTION_POWER_CONNECTED,
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        processBatteryIntent(it)
                    }
                }
            }
        }
    }

    /**
     * 处理电池相关的Intent
     */
    private fun processBatteryIntent(intent: Intent) {
        // 获取充电状态
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // 获取充电类型
        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargeType = when {
            chargePlug == BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            chargePlug == BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not Charging"
        }

        // 获取电池电量
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = if (scale > 0) (level * 100 / scale) else 0

        // 获取电池温度（以摄氏度为单位）
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val batteryTemperature = temperature / 10f

        // 获取电池电压（以毫伏为单位）
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        // 回调监听事件
        onChargeStatusChangeListener?.onChargeStatusChanged(
            isCharging,
            chargeType,
            batteryLevel,
            batteryTemperature,
            voltage
        )
    }

    /**
     * 获取当前电池状态（无需监听即可获取）
     */
    fun getCurrentBatteryStatus(): BatteryStatus {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let {
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val chargePlug = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargeType = when {
                chargePlug == BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                chargePlug == BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Not Charging"
            }

            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryLevel = if (scale > 0) (level * 100 / scale) else 0

            val temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            val batteryTemperature = temperature / 10f

            val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

            return BatteryStatus(isCharging, chargeType, batteryLevel, batteryTemperature, voltage)
        }
        return BatteryStatus(false, "Unknown", 0, 0f, 0)
    }

    /**
     * 电池状态数据类
     */
    data class BatteryStatus(
        val isCharging: Boolean,
        val chargeType: String,
        val batteryLevel: Int,
        val batteryTemperature: Float,
        val batteryVoltage: Int
    )
}