package app.allever.android.sample.cleaner.scanner

import app.allever.android.sample.cleaner.core.CleanConfig
import app.allever.android.sample.cleaner.safety.WhiteList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.util.LinkedList

/**
 * 文件扫描器
 *
 * 对应文档中的"文件扫描算法"章节，支持三种扫描策略：
 * - 递归扫描：深度优先，实现简单，适合小规模目录
 * - 广度优先(BFS)：层级遍历，可控深度，适合大规模文件
 * - 并行扫描：多协程并发，速度最快，适合多目录场景
 *
 * 单一职责原则：只负责文件扫描和匹配，不负责删除操作
 */
object FileScanner {

    /**
     * 扫描策略枚举
     */
    enum class Strategy {
        /** 递归深度优先 */
        RECURSIVE,
        /** 广度优先（BFS） */
        BFS,
        /** 协程并行扫描 */
        PARALLEL
    }

    // ========== 公共扫描接口 ==========

    /**
     * 使用默认配置并行扫描多个根目录
     *
     * @param rootDirs 根目录列表
     * @param rules 垃圾文件规则列表
     * @param config 清理配置
     * @return 匹配到的垃圾文件列表
     */
    suspend fun parallelScan(
        rootDirs: List<File>,
        rules: List<JunkRule>,
        config: CleanConfig = CleanConfig()
    ): List<JunkFileItem> = withContext(Dispatchers.IO) {
        if (rootDirs.isEmpty() || rules.isEmpty()) return@withContext emptyList()

        coroutineScope {
            rootDirs
                .filter { it.exists() && it.canRead() }
                .map { dir ->
                    async {
                        scanDirectory(dir, rules, config, Strategy.RECURSIVE)
                    }
                }
                .awaitAll()
                .flatten()
                .sortedDescending()
        }
    }

    /**
     * 扫描单个目录
     *
     * @param rootDir 根目录
     * @param rules 规则列表
     * @param config 配置
     * @param strategy 扫描策略，默认 BFS
     * @return 匹配的垃圾文件列表
     */
    suspend fun scan(
        rootDir: File,
        rules: List<JunkRule>,
        config: CleanConfig = CleanConfig(),
        strategy: Strategy = Strategy.BFS
    ): List<JunkFileItem> = withContext(Dispatchers.IO) {
        scanDirectory(rootDir, rules, config, strategy)
    }

    // ========== 内部扫描实现 ==========

    /**
     * 核心扫描逻辑，根据策略分发
     */
    private fun scanDirectory(
        rootDir: File,
        rules: List<JunkRule>,
        config: CleanConfig,
        strategy: Strategy
    ): List<JunkFileItem> {
        return when (strategy) {
            Strategy.RECURSIVE -> recursiveScan(rootDir, rootDir.absolutePath, rules, config)
            Strategy.BFS -> bfsScan(rootDir, rootDir.absolutePath, rules, config)
            Strategy.PARALLEL -> recursiveScan(rootDir, rootDir.absolutePath, rules, config)
        }
    }

    /**
     * 递归深度优先扫描
     *
     * 实现简单，适合小规模目录。
     * 智能跳过系统目录、隐藏目录。
     */
    private fun recursiveScan(
        directory: File,
        rootPath: String,
        rules: List<JunkRule>,
        config: CleanConfig
    ): List<JunkFileItem> {
        val results = mutableListOf<JunkFileItem>()

        if (!directory.exists() || !directory.isDirectory) return results

        // 智能跳过：受保护路径、隐藏目录
        if (shouldSkip(directory)) return results

        val files = directory.listFiles() ?: return results

        for (file in files) {
            // 跳过受保护文件
            if (WhiteList.isProtected(file, config)) continue

            when {
                file.isFile -> {
                    // 尝试匹配所有规则
                    for (rule in rules) {
                        if (rule.matches(file, rootPath)) {
                            results.add(JunkFileItem.from(file, rule.type))
                            break // 一个文件只匹配一个类型
                        }
                    }
                }

                file.isDirectory && !shouldSkip(file) -> {
                    // 递归子目录
                    results.addAll(recursiveScan(file, rootPath, rules, config))
                }
            }
        }

        return results
    }

    /**
     * 广度优先扫描（BFS）
     *
     * 使用队列逐层遍历，可控制最大深度，
     * 适合大规模文件扫描，避免栈溢出风险。
     */
    private fun bfsScan(
        rootDir: File,
        rootPath: String,
        rules: List<JunkRule>,
        config: CleanConfig,
        maxDepth: Int = 20
    ): List<JunkFileItem> {
        val results = mutableListOf<JunkFileItem>()
        val queue = LinkedList<Pair<File, Int>>()

        if (!rootDir.exists() || !rootDir.isDirectory || shouldSkip(rootDir)) {
            return results
        }

        queue.offer(rootDir to 0)

        while (queue.isNotEmpty()) {
            val (dir, depth) = queue.poll()!!

            // 深度控制
            if (depth > maxDepth) continue

            val files = dir.listFiles() ?: continue

            for (file in files) {
                if (WhiteList.isProtected(file, config)) continue

                when {
                    file.isFile -> {
                        for (rule in rules) {
                            if (rule.matches(file, rootPath)) {
                                results.add(JunkFileItem.from(file, rule.type))
                                break
                            }
                        }
                    }

                    file.isDirectory && !shouldSkip(file) && depth < maxDepth -> {
                        queue.offer(file to depth + 1)
                    }
                }
            }
        }

        return results
    }

    // ========== 智能跳过策略 ==========

    /**
     * 判断是否应跳过该目录/文件
     *
     * 只跳过系统关键目录（/system, /proc, /sys 等），
     * 不跳过用户数据目录，确保垃圾文件能被扫描到。
     */
    internal fun shouldSkip(file: File): Boolean {
        if (!file.isDirectory) return false

        val name = file.name
        val path = file.absolutePath.lowercase()

        // 隐藏目录（但保留 .cache 等常见缓存目录）
        if (name.startsWith(".") && name !in listOf(".cache", ".tmp", ".temp")) return true

        // 仅跳过真正的系统关键目录
        val skipPatterns = listOf(
            "/system/",
            "/vendor/",
            "/proc/",
            "/sys/",
            "/dev/"
        )

        for (pattern in skipPatterns) {
            if (path.contains(pattern)) return true
        }

        return false
    }

    /**
     * 计算目录的总大小（用于缓存大小统计）
     *
     * @param directory 目标目录
     * @return 目录总占用字节数
     */
    suspend fun calculateDirectorySize(directory: File): Long =
        withContext(Dispatchers.IO) {
            if (!directory.exists() || !directory.isDirectory) return@withContext 0L

            var totalSize = 0L
            val queue = LinkedList<File>()
            queue.offer(directory)

            while (queue.isNotEmpty()) {
                val dir = queue.poll() ?: continue
                val files = dir.listFiles() ?: continue

                for (file in files) {
                    when {
                        file.isFile -> totalSize += file.length()
                        file.isDirectory && !shouldSkip(file) -> queue.offer(file)
                    }
                }
            }

            totalSize
        }
}
