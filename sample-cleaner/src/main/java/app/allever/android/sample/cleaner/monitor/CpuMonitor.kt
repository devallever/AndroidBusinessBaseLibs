package app.allever.android.sample.cleaner.monitor

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import app.allever.android.lib.core.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * CPU 监控
 *
 * 对应文档"CPU监控"章节。
 * 数据来源：
 * - CPU 使用率：读取 /proc/stat（两次采样差值计算）
 * - 核心数：读取 /sys/devices/system/cpu/
 * - CPU 频率：读取 /sys/devices/system/cpu/cpu/cpufreq/
 */
object CpuMonitor {

    data class CpuInfo(
        val usagePercent: Float,       // 使用率 0-100
        val coreCount: Int,             // 核心数
        val currentFreqKhz: Long,       // 当前频率(KHz)
        val maxFreqKhz: Long            // 最大频率(KHz)
    ) {
        val formattedUsage: String get() = "%.1f%%".format(usagePercent)
        val formattedFreq: String get() = "${currentFreqKhz / 1000} MHz"
    }

    /**
     * 上一次采样的 CPU 时间数据
     */
    private var lastCpuTimes: CpuTimes? = null

    /**
     * 获取 CPU 信息
     *
     * CPU 使用率通过两次采样差值计算：
     * - 如果有上一次采样数据，直接用差值计算
     * - 如果是首次调用，先采样一次，等待 300ms 后再采样，确保能算出差值
     */
    suspend fun getCpuInfo(): CpuInfo = withContext(Dispatchers.IO) {
        val coreCount = getCpuCoreCount()
        val currentFreq = getCurrentCpuFreq()
        val maxFreq = getMaxCpuFreq()

        // 计算实时使用率（基于两次采样差值）
        val usagePercent = calculateRealtimeCpuUsage()

        CpuInfo(
            usagePercent = usagePercent.coerceIn(0f, 100f),
            coreCount = coreCount,
            currentFreqKhz = currentFreq,
            maxFreqKhz = maxFreq
        )
    }

    // ========== CPU 使用率（差值法） ==========

    /**
     * CPU 时间快照数据
     */
    private data class CpuTimes(
        val user: Long,
        val nice: Long,
        val system: Long,
        val idle: Long,
        val iowait: Long,
        val total: Long,
        val timestampMs: Long
    )

    /**
     * 计算实时 CPU 使用率
     *
     * 使用两次采样差值法：
     * usage = 1 - (idle2 - idle1) / (total2 - total1)
     *
     * @return 实时使用率百分比 (0-100)
     */
    private suspend fun calculateRealtimeCpuUsage(): Float {
        // 首次调用：先做一次基准采样
        if (lastCpuTimes == null) {
            val initial = readCpuTimes() ?: return 0f
            lastCpuTimes = initial

            // 等待 300ms 后再做第二次采样，确保有足够的时间差计算差值
            kotlinx.coroutines.delay(300L)
        }

        val current = readCpuTimes() ?: return 0f
        val last = lastCpuTimes!!

        // 更新上次采样
        lastCpuTimes = current

        // 计算差值
        val totalDelta = current.total - last.total
        val idleDelta = current.idle - last.idle

        if (totalDelta <= 0) return 0f

        // 使用率 = 非空闲时间增量 / 总时间增量
        val usage = 1f - idleDelta.toFloat() / totalDelta.toFloat()
        return usage.coerceIn(0f, 100f)
    }

    /**
     * 读取当前 CPU 时间快照
     */
    private fun readCpuTimes(): CpuTimes? {
        return try {
            BufferedReader(FileReader("/proc/stat")).use { reader ->
                val line = reader.readLine() ?: return@use null

                // 第一行格式: cpu user nice system idle iowait irq softirq steal guest guest_nice
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size < 5) return@use null

                val user = parts[1].toLongOrNull() ?: return@use null
                val nice = parts[2].toLongOrNull() ?: return@use null
                val system = parts[3].toLongOrNull() ?: return@use null
                val idle = parts[4].toLongOrNull() ?: return@use null
                val iowait = if (parts.size > 5) parts[5].toLongOrNull() ?: 0 else 0

                val total = user + nice + system + idle + iowait

                CpuTimes(
                    user = user,
                    nice = nice,
                    system = system,
                    idle = idle,
                    iowait = iowait,
                    total = total,
                    timestampMs = System.currentTimeMillis()
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    // ========== 内部实现 ==========

    private fun getCpuCoreCount(): Int {
        return try {
            File("/sys/devices/system/cpu/").listFiles()
                ?.count { it.name.startsWith("cpu") && it.name.drop(4).toIntOrNull() != null }
                ?: Runtime.getRuntime().availableProcessors()
        } catch (_: Exception) {
            Runtime.getRuntime().availableProcessors()
        }
    }

    private fun getCurrentCpuFreq(): Long {
        return try {
            // 尝试读取 cpu0 的当前频率
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (freqFile.exists()) {
                freqFile.readText().trim().toLongOrNull() ?: 0L
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    private fun getMaxCpuFreq(): Long {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq")
            if (freqFile.exists()) {
                freqFile.readText().trim().toLongOrNull() ?: 0L
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }
}
