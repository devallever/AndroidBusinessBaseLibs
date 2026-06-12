package app.allever.android.sample.cleaner.monitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 温度监控
 *
 * 对应文档"温度监控"章节。
 * 数据来源：读取 /sys/class/thermal/ 和 /sys/devices/virtual/thermal/
 *
 * 监控指标：CPU温度、电池温度、设备表面温度
 */
object ThermalMonitor {

    data class ThermalInfo(
        val cpuTemp: Float,          // CPU 温度（°C）
        val batteryTemp: Float,      // 电池温度（°C）
        val skinTemp: Float,         // 表面温度（°C）
        val thermalZoneCount: Int,   // 热力区域数量
        val isOverheated: Boolean    // 是否过热
    ) {
        val formattedCpuTemp: String get() = if (cpuTemp <= 0f) "--" else "%.1f°C".format(cpuTemp)
        val formattedBatteryTemp: String get() = if (batteryTemp <= 0f) "--" else "%.1f°C".format(batteryTemp)

        companion object {
            const val OVERHEAT_THRESHOLD = 45f // 过热阈值 °C
        }
    }

    /**
     * 获取设备温度信息
     */
    suspend fun getThermalInfo(): ThermalInfo = withContext(Dispatchers.IO) {
        val cpuTemp = readCpuThermalZone()
        val batteryTemp = readBatteryTemperature()
        val zoneCount = countThermalZones()

        ThermalInfo(
            cpuTemp = cpuTemp,
            batteryTemp = batteryTemp,
            skinTemp = 0f, // Android 通常无法直接获取皮肤温度
            thermalZoneCount = zoneCount,
            isOverheated = cpuTemp > ThermalInfo.OVERHEAT_THRESHOLD ||
                    batteryTemp > ThermalInfo.OVERHEAT_THRESHOLD
        )
    }

    // ========== 内部实现 ==========

    /**
     * 从 thermal_zone 读取 CPU 温度
     */
    private fun readCpuThermalZone(): Float {
        // 尝试常见的 thermal zone 路径
        val zonePaths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone1/temp"
        )

        for (path in zonePaths) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val rawValue = file.readText().trim().toFloatOrNull() ?: continue
                    // 有些设备单位是摄氏度，有些是毫摄氏度
                    return if (rawValue > 200) rawValue / 1000f else rawValue
                }
            } catch (_: Exception) {}
        }

        return 0f
    }

    /**
     * 读取电池温度（从 BatteryMonitor 获取更准确）
     */
    private fun readBatteryTemperature(): Float {
        return try {
            val info = BatteryMonitor.getBatteryInfo()
            info.temperature
        } catch (_: Exception) {
            0f
        }
    }

    /**
     * 统计热力区域数量
     */
    private fun countThermalZones(): Int {
        return try {
            val thermalDir = File("/sys/class/thermal/")
            if (thermalDir.exists()) {
                thermalDir.listFiles()
                    ?.count { it.name.startsWith("thermal_zone") }
                    ?: 0
            } else {
                0
            }
        } catch (_: Exception) {
            0
        }
    }
}
