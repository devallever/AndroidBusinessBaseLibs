package app.allever.android.sample.cleaner.memory

import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.core.CleanType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 内存释放器
 *
 * 对应文档"内存释放策略"章节，按优先级顺序执行：
 * 1. 清理应用缓存
 * 2. 关闭后台进程（按优先级从低到高）
 * 3. 触发 GC
 *
 * 单一职责：只负责内存释放策略的编排执行
 */
object MemoryCleaner {

    /**
     * 执行一键加速（内存释放）
     *
     * 按文档建议的策略顺序依次执行：
     * 清理缓存 → 杀后台进程 → 触发GC
     *
     * @return 释放结果
     */
    suspend fun releaseMemory(): CleanResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        var releasedMemory = 0L
        var killedCount = 0

        // 步骤1：获取当前内存状态作为基准
        val beforeInfo = MemoryMonitor.getMemoryInfo()

        // 步骤2：关闭可清理的后台进程
        val processes = ProcessManager.getRunningProcesses()
        val killableProcesses = processes.filter { ProcessManager.isKillable(it) }

        for (process in killableProcesses) {
            val success = ProcessManager.killBackgroundProcesses(process.processName)
            if (success) {
                killedCount++
            }
        }

        // 步骤3：触发 GC
        triggerGc()

        // 稍等片刻让系统回收
        kotlinx.coroutines.delay(300)

        // 步骤4：计算释放效果
        val afterInfo = MemoryMonitor.getMemoryInfo()
        releasedMemory = beforeInfo.availMem - afterInfo.availMem
        if (releasedMemory < 0) releasedMemory = 0

        CleanResult(
            type = CleanType.CACHE,
            success = true,
            cleanedSize = releasedMemory,
            cleanedCount = killedCount,
            costTimeMs = System.currentTimeMillis() - startTime
        )
    }

    /**
     * 手动触发垃圾回收
     *
     * 注意：System.gc() 只是建议 JVM 进行 GC，
     * 实际是否执行由虚拟机决定。
     */
    internal fun triggerGc() {
        try {
            Runtime.getRuntime().gc()
            System.runFinalization()
        } catch (_: Exception) {}
    }
}
