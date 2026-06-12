package app.allever.android.sample.cleaner.storage

import android.os.Environment
import app.allever.android.lib.core.app.App
import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.core.CleanType
import app.allever.android.sample.cleaner.scanner.JunkRule
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 临时文件清理器
 *
 * 负责清理临时文件和日志文件，对应文档中"垃圾文件识别"的 TEMP 和 LOG 类型。
 * 包括：
 * - .tmp / .temp / .bak / .swp 等临时文件
 * - *.log 日志文件
 * - 超过指定天数的旧临时文件
 *
 * 单一职责：只负责临时/日志类文件的扫描和清理
 */
object TempCleaner {

    /** 默认最大扫描深度 */
    private const val MAX_SCAN_DEPTH = 10

    /**
     * 扫描临时文件
     *
     * @param rootDirs 扫描的根目录列表
     * @param maxAgeDays 最大保留天数（超过此天数才标记为可清理），0 表示不限
     * @return 扫描到的临时文件列表
     */
    suspend fun scan(
        rootDirs: List<File> = getDefaultScanDirs(),
        maxAgeDays: Int = 0
    ): List<JunkFileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<JunkFileItem>()

        val tempRule = if (maxAgeDays > 0) {
            JunkRule.tempRule().copy(maxAgeDays = maxAgeDays)
        } else {
            JunkRule.tempRule()
        }

        val logRule = if (maxAgeDays > 0) {
            JunkRule.logRule().copy(maxAgeDays = maxAgeDays)
        } else {
            JunkRule.logRule()
        }

        val rules = listOf(tempRule, logRule)

        for (dir in rootDirs) {
            if (!dir.exists() || !dir.canRead()) continue
            scanRecursive(dir, rules, results, depth = 0)
        }

        results
    }

    /**
     * 清理已选中的临时文件
     *
     * @param items 待清理项
     * @return 清理结果
     */
    suspend fun clean(items: List<JunkFileItem>): CleanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var cleanedSize = 0L
        var cleanedCount = 0
        val cleanedFiles = mutableListOf<File>()

        for (item in items) {
            if (!item.selected) continue

            if (app.allever.android.sample.cleaner.safety.SafetyChecker.safeDelete(item.file)) {
                cleanedSize += item.size
                cleanedCount++
                cleanedFiles.add(item.file)
            }
        }

        CleanResult(
            type = CleanType.TEMP,
            success = true,
            cleanedSize = cleanedSize,
            cleanedCount = cleanedCount,
            costTimeMs = System.currentTimeMillis() - startTime,
            cleanedFiles = cleanedFiles
        )
    }

    /**
     * 获取默认扫描目录
     *
     * 包含：
     * 1. 本应用内部/外部缓存
     * 2. 本应用 files 目录下的 temp/tmp 子目录
     * 3. 外部存储根目录（Download、顶层临时文件等）
     */
    fun getDefaultScanDirs(): List<File> {
        val dirs = mutableListOf<File>()
        val context = App.context

        // 1. 应用内部缓存
        context.cacheDir?.let { dirs.add(it) }

        // 2. 应用外部缓存
        try {
            context.externalCacheDir?.let {
                if (it != context.cacheDir) dirs.add(it)
            }
        } catch (_: SecurityException) {}

        // 3. 应用 files 目录下的 temp 子目录
        context.filesDir?.let { filesDir ->
            val tempDir = File(filesDir, "temp")
            if (tempDir.exists()) dirs.add(tempDir)

            val tmpDir = File(filesDir, "tmp")
            if (tmpDir.exists()) dirs.add(tmpDir)
        }

        // 4. 外部存储公共目录（Download、顶层 .tmp/.temp 文件等）
        try {
            val externalRoot = Environment.getExternalStorageDirectory()
            if (externalRoot.exists() && externalRoot.canRead()) {
                dirs.add(externalRoot)

                // Download 目录
                val downloadDir = File(externalRoot, "Download")
                if (downloadDir.exists()) dirs.add(downloadDir)
            }
        } catch (_: Exception) {}

        return dirs
    }

    /**
     * 递归扫描目录
     */
    private fun scanRecursive(
        directory: File,
        rules: List<JunkRule>,
        results: MutableList<JunkFileItem>,
        depth: Int
    ) {
        if (depth > MAX_SCAN_DEPTH) return
        if (!directory.exists() || !directory.isDirectory) return

        val files = directory.listFiles() ?: return

        for (file in files) {
            if (app.allever.android.sample.cleaner.safety.WhiteList.isProtected(file)) continue

            when {
                file.isFile -> {
                    for (rule in rules) {
                        if (rule.matches(file, directory.absolutePath)) {
                            results.add(JunkFileItem.from(file, rule.type))
                            break
                        }
                    }
                }
                file.isDirectory -> {
                    scanRecursive(file, rules, results, depth + 1)
                }
            }
        }
    }
}
