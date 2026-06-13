package app.allever.android.lib.imageloader.core.internal.engine

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * 图片加载线程池管理器
 *
 * 使用固定大小的线程池控制并发下载数量，避免 OOM。
 * 默认线程数 = CPU 核心数，上限为 4。
 *
 * 用法：
 * ```
 * ImageExecutor.execute { /* 加载任务 */ }
 * ```
 */
object ImageExecutor {

    /** 核心线程数（可配置） */
    var corePoolSize: Int =
        Runtime.getRuntime().availableProcessors().coerceIn(1, 4)

    private val threadCounter = AtomicInteger(0)

    val executor: ExecutorService by lazy {
        Executors.newFixedThreadPool(corePoolSize) { task ->
            Thread(task, "ImageLoader-${threadCounter.incrementAndGet()}").apply {
                priority = Thread.NORM_PRIORITY - 1
                isDaemon = true // 守护线程，不阻止 JVM 退出
            }
        }
    }

    /** 在后台线程执行任务 */
    fun execute(runnable: Runnable) { executor.execute(runnable) }

    fun <T> submit(callable: () -> T) = executor.submit(callable)

    /** 关闭线程池（应用退出时调用） */
    fun shutdown() { executor.shutdown() }

    /** 是否已关闭 */
    fun isShutdown(): Boolean = executor.isShutdown
}
