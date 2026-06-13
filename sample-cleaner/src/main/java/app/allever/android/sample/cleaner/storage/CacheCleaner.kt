package app.allever.android.sample.cleaner.storage

import android.content.Context
import android.util.Log
import app.allever.android.lib.core.app.App
import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.core.CleanType
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import app.allever.android.sample.cleaner.scanner.JunkRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 缓存清理器
 *
 * 负责清理应用缓存文件，对应文档"缓存清理"章节。
 * 清理范围：
 * - 内部缓存目录：/data/data/<pkg>/cache
 * - 外部缓存目录：/storage/emulated/0/Android/data/<pkg>/cache
 *
 * 单一职责：只负责缓存类型文件的扫描和清理
 */
object CacheCleaner {

    private const val TAG = "CacheCleaner"

    /**
     * 扫描应用缓存文件
     *
     * 扫描范围：
     * 1. 本应用内部缓存目录
     * 2. 本应用外部缓存目录
     * 3. 外部存储根目录下的公共缓存（其他应用的缓存）
     *
     * @return 扫描到的缓存文件列表
     */
    suspend fun scan(): List<JunkFileItem> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val results = mutableListOf<JunkFileItem>()
        val context = App.context
        val rule = JunkRule.cacheRule()

        Log.i(TAG, "[scan] 开始扫描应用缓存")

        // 1. 扫描内部缓存
        scanCacheDir(context.cacheDir, results, rule)
        Log.d(TAG, "[scan] 内部缓存: ${context.cacheDir?.absolutePath}, 命中 ${results.size} 个")

        // 2. 扫描外部缓存
        try {
            val externalCacheDir = context.externalCacheDir
            if (externalCacheDir != null && externalCacheDir != context.cacheDir) {
                val beforeSize = results.size
                scanCacheDir(externalCacheDir, results, rule)
                Log.d(TAG, "[scan] 外部缓存: ${externalCacheDir.absolutePath}, 命中 ${results.size - beforeSize} 个")
            }
        } catch (_: SecurityException) {
            Log.w(TAG, "[scan] 外部缓存无权限访问")
        }

        // 3. 扫描外部存储下各应用的 cache 目录
        try {
            val androidDataDir = File("/storage/emulated/0/Android/data")
            if (androidDataDir.exists() && androidDataDir.canRead()) {
                val appDirs = androidDataDir.listFiles()?.filter { it.isDirectory }
                if (!appDirs.isNullOrEmpty()) {
                    Log.d(TAG, "[scan] Android/data 下有 ${appDirs.size} 个应用目录")
                    var crossAppCount = 0
                    appDirs.forEach { appDir ->
                        val cacheDir = File(appDir, "cache")
                        if (cacheDir.exists() && cacheDir.canRead()) {
                            val beforeSize = results.size
                            scanCacheDir(cacheDir, results, rule)
                            crossAppCount += (results.size - beforeSize)
                        }
                    }
                    Log.d(TAG, "[scan] 跨应用缓存命中 $crossAppCount 个文件")
                } else {
                    Log.w(TAG, "[scan] Android/data 目录为空或不可读")
                }
            } else {
                Log.d(TAG, "[scan] Android/data 不存在或无读取权限 (Scoped Storage)")
            }
        } catch (_: SecurityException) {
            Log.w(TAG, "[scan] Android/data 无权限访问")
        }

        val costMs = System.currentTimeMillis() - startTime
        val totalSize = results.sumOf { it.size }
        Log.i(
            TAG,
            "[scan] 扫描完成, 共 ${results.size} 个文件, " +
                "${JunkFileItem.formatFileSize(totalSize)}, 耗时 ${costMs}ms"
        )

        results
    }

    /**
     * 清理已选中的缓存文件
     *
     * @param items 待清理的缓存项（仅处理 selected=true 的）
     * @return 清理结果
     */
    suspend fun clean(items: List<JunkFileItem>): CleanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var cleanedSize = 0L
        var cleanedCount = 0
        val cleanedFiles = mutableListOf<File>()
        val selectedItems = items.filter { it.selected }

        Log.i(TAG, "[clean] 开始清理, 共 ${items.size} 项, 已选 ${selectedItems.size} 项")

        for (item in selectedItems) {
            if (app.allever.android.sample.cleaner.safety.SafetyChecker.safeDelete(item.file)) {
                cleanedSize += item.size
                cleanedCount++
                cleanedFiles.add(item.file)
                Log.v(TAG, "[clean] 已删除: ${item.fileName}")
            } else {
                Log.w(TAG, "[clean] 删除失败: ${item.absolutePath}")
            }
        }

        CleanResult(
            type = CleanType.CACHE,
            success = true,
            cleanedSize = cleanedSize,
            cleanedCount = cleanedCount,
            costTimeMs = System.currentTimeMillis() - startTime,
            cleanedFiles = cleanedFiles
        ).also {
            Log.i(
                TAG,
                "[clean] 完成, 删除 $cleanedCount 个文件, " +
                    "${JunkFileItem.formatFileSize(cleanedSize)}"
            )
        }
    }

    /**
     * 获取缓存目录的总大小
     */
    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        val context = App.context
        var size = 0L

        context.cacheDir?.let { dir ->
            size += calculateDirSize(dir)
        }

        try {
            context.externalCacheDir?.let { dir ->
                if (dir != context.cacheDir) {
                    size += calculateDirSize(dir)
                }
            }
        } catch (_: SecurityException) {}

        size
    }

    private fun scanCacheDir(
        cacheDir: File,
        results: MutableList<JunkFileItem>,
        rule: JunkRule
    ) {
        if (!cacheDir.exists() || !cacheDir.isDirectory) return

        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && rule.matches(file, cacheDir.absolutePath)) {
                results.add(JunkFileItem.from(file, CleanType.CACHE))
            } else if (file.isDirectory) {
                scanSubDirectory(file, results, rule, maxDepth = 5)
            }
        }
    }

    private fun scanSubDirectory(
        directory: File,
        results: MutableList<JunkFileItem>,
        rule: JunkRule,
        currentDepth: Int = 0,
        maxDepth: Int = 5
    ) {
        if (currentDepth > maxDepth || !directory.isDirectory) return

        directory.listFiles()?.forEach { file ->
            when {
                file.isFile && rule.matches(file, directory.absolutePath) -> {
                    results.add(JunkFileItem.from(file, CleanType.CACHE))
                }
                file.isDirectory -> {
                    scanSubDirectory(file, results, rule, currentDepth + 1, maxDepth)
                }
            }
        }
    }

    private fun calculateDirSize(dir: File): Long {
        if (!dir.exists()) return 0L
        var size = 0L
        val queue = ArrayDeque<File>()
        queue.add(dir)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            current.listFiles()?.forEach { file ->
                when {
                    file.isFile -> size += file.length()
                    file.isDirectory -> queue.add(file)
                }
            }
        }
        return size
    }
}
