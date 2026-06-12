package app.allever.android.sample.cleaner.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import app.allever.android.lib.core.app.App

/**
 * 内存监控
 *
 * 对应文档"内存状态监控"章节。
 * 获取设备内存信息：总内存、可用内存、已用内存、使用率、低内存阈值。
 *
 * 数据来源：ActivityManager.MemoryInfo
 */
object MemoryMonitor {

    /**
     * 内存信息数据类
     *
     * @param totalMem 总内存（字节）
     * @param availMem 可用内存（字节）
     * @param usedMem 已用内存（字节）
     * @param usagePercent 使用率 (0-100)
     * @param lowMemory 是否处于低内存状态
     * @param threshold 低内存阈值（字节）
     */
    data class MemoryInfo(
        val totalMem: Long,
        val availMem: Long,
        val usedMem: Long,
        val usagePercent: Float,
        val lowMemory: Boolean,
        val threshold: Long
    ) {
        /** 格式化总内存 */
        val formattedTotal: String get() = formatSize(totalMem)

        /** 格式化可用内存 */
        val formattedAvail: String get() = formatSize(availMem)

        /** 格式化已用内存 */
        val formattedUsed: String get() = formatSize(usedMem)

        companion object {
            fun formatSize(size: Long): String {
                if (size <= 0) return "0 B"
                val units = arrayOf("B", "KB", "MB", "GB")
                var unitIndex = 0
                var value = size.toDouble()
                while (value >= 1024 && unitIndex < units.lastIndex) {
                    value /= 1024
                    unitIndex++
                }
                return if (unitIndex == 0) "$size ${units[unitIndex]}"
                else String.format("%.1f %s", value, units[unitIndex])
            }
        }
    }

    /**
     * 获取当前内存信息
     */
    fun getMemoryInfo(): MemoryInfo {
        val context = App.context
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()

        try {
            activityManager.getMemoryInfo(memoryInfo)
        } catch (_: Exception) {}

        val totalMem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            memoryInfo.totalMem
        } else {
            // API < 16 无法获取总内存，使用可用内存估算
            memoryInfo.availMem * 2L
        }

        val availMem = memoryInfo.availMem
        val usedMem = totalMem - availMem
        val usagePercent = if (totalMem > 0) (usedMem.toFloat() / totalMem * 100f) else 0f

        return MemoryInfo(
            totalMem = totalMem,
            availMem = availMem,
            usedMem = usedMem,
            usagePercent = usagePercent.coerceIn(0f, 100f),
            lowMemory = memoryInfo.lowMemory,
            threshold = memoryInfo.threshold
        )
    }

    /**
     * 获取 Java 堆内存信息（Runtime）
     *
     * @return Pair(已用堆内存, 最大堆内存)
     */
    fun getHeapMemoryInfo(): Pair<Long, Long> {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        val max = runtime.maxMemory()
        return used to max
    }
}
