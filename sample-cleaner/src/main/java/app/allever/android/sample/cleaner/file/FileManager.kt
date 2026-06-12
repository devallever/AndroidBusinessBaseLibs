package app.allever.android.sample.cleaner.file

import android.os.Environment
import app.allever.android.sample.cleaner.core.CleanConfig
import app.allever.android.sample.cleaner.scanner.FileScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * 文件管理器
 *
 * 对应文档"文件管理"章节，提供：
 * - 大文件扫描（>= 阈值）
 * - 重复文件检测（大小→MD5 两阶段筛选）
 * - 文件分类管理
 *
 * 单一职责：只负责文件的扫描、检测和分类，不负责删除操作
 */
object FileManager {

    /** 默认大文件阈值：10MB */
    private const val DEFAULT_LARGE_FILE_THRESHOLD = 10L * 1024 * 1024

    /**
     * 大文件扫描结果
     */
    data class LargeFileResult(
        val files: List<FileInfo>,
        val totalSize: Long,
        val scanTimeMs: Long
    )

    /**
     * 重复文件检测结果
     */
    data class DuplicateResult(
        /** 分组 key → 同一组重复的文件列表 */
        val groups: Map<String, List<FileInfo>>,
        val totalWastedSize: Long,
        val scanTimeMs: Long
    )

    // ========== 大文件扫描 ==========

    /**
     * 扫描大文件
     *
     * 对应文档"大文件扫描"章节：
     * - 默认阈值 10MB
     * - 并行扫描多个目录
     * - 智能跳过系统目录
     *
     * @param thresholdBytes 大小阈值（字节），默认 10MB
     * @return 大文件列表及统计信息
     */
    suspend fun scanLargeFiles(
        thresholdBytes: Long = DEFAULT_LARGE_FILE_THRESHOLD,
        config: CleanConfig = CleanConfig()
    ): LargeFileResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val largeFiles = mutableListOf<FileInfo>()

        val rootDirs = getScanRootDirs()

        coroutineScope {
            rootDirs
                .filter { it.exists() && it.canRead() }
                .map { dir ->
                    async {
                        scanLargeFilesRecursive(dir, thresholdBytes, config)
                    }
                }
                .awaitAll()
                .flatten()
                .let { results ->
                    largeFiles.addAll(results)
                }
        }

        val sorted = largeFiles.sortedDescending()
        LargeFileResult(
            files = sorted,
            totalSize = sorted.sumOf { it.size },
            scanTimeMs = System.currentTimeMillis() - startTime
        )
    }

    // ========== 重复文件检测 ==========

    /**
     * 检测重复文件
     *
     * 对应文档"重复文件检测"章节的多阶段筛选策略：
     * 阶段1：按大小分组（快速排除大小不同的文件）
     * 阶段2：对同大小的文件计算 MD5 哈希确认内容相同
     *
     * @param minFileSize 最小文件大小过滤（字节），默认 1KB
     * @return 重复文件分组结果
     */
    suspend fun detectDuplicates(
        minFileSize: Long = 1024L,
        config: CleanConfig = CleanConfig()
    ): DuplicateResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val rootDirs = getScanRootDirs()

        // 收集所有候选文件
        val allFiles = mutableListOf<File>()
        for (dir in rootDirs) {
            if (dir.exists() && dir.canRead()) {
                collectFiles(dir, allFiles, maxDepth = 15, config = config)
            }
        }

        // 过滤掉太小的文件
        val candidates = allFiles.filter { it.isFile && it.length() >= minFileSize }

        // 阶段1：按大小分组
        val sizeGroups = candidates.groupBy { it.length() }.filter { it.value.size > 1 }

        // 阶段2：对同大小组计算 MD5
        val duplicateGroups = mutableMapOf<String, MutableList<FileInfo>>()

        for ((_size, filesInGroup) in sizeGroups) {
            val hashToFiles = mutableMapOf<String, File>()

            for (file in filesInGroup) {
                val md5 = calculateMd5(file)
                if (md5 != null) {
                    val existing = hashToFiles[md5]
                    if (existing != null) {
                        // 发现重复
                        val groupKey = "$md5 (${file.length()} bytes)"
                        if (!duplicateGroups.containsKey(groupKey)) {
                            duplicateGroups[groupKey] = mutableListOf(
                                FileInfo(existing, FileCategory.fromExtension(existing.extension))
                            )
                        }
                        duplicateGroups[groupKey]?.add(
                            FileInfo(file, FileCategory.fromExtension(file.extension))
                        )
                    } else {
                        hashToFiles[md5] = file
                    }
                }
            }
        }

        // 计算浪费空间（每组只保留一个，其余为浪费）
        var totalWasted = 0L
        for ((_, files) in duplicateGroups) {
            if (files.size > 1) {
                // 第一个保留，其余为浪费
                totalWasted += files.drop(1).sumOf { it.size }
            }
        }

        DuplicateResult(
            groups = duplicateGroups.mapValues { it.value },
            totalWastedSize = totalWasted,
            scanTimeMs = System.currentTimeMillis() - startTime
        )
    }

    // ========== 文件分类 ==========

    /**
     * 获取指定目录下按分类分组的文件
     *
     * @param category 目标分类
     * @param limit 最大返回数量
     * @return 分类后的文件列表
     */
    suspend fun getFilesByCategory(
        category: FileCategory,
        limit: Int = Int.MAX_VALUE
    ): List<FileInfo> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileInfo>()
        val extensions = category.extensions()

        for (dir in getScanRootDirs()) {
            if (!dir.exists() || !dir.canRead()) continue
            collectByCategory(dir, category, extensions, result, depth = 0, maxDepth = 10)
            if (result.size >= limit) break
        }

        result.take(limit).sortedDescending()
    }

    // ========== 内部方法 ==========

    /**
     * 获取扫描根目录
     */
    private fun getScanRootDirs(): List<File> {
        val dirs = mutableListOf<File>()

        // 内部存储根目录
        Environment.getExternalStorageDirectory()?.let { dirs.add(it) }

        // Download 目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let {
            dirs.add(it)
        }

        // DCIM 目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.let {
            dirs.add(it)
        }

        return dirs
    }

    /**
     * 递归扫描大文件
     */
    private fun scanLargeFilesRecursive(
        directory: File,
        thresholdBytes: Long,
        config: CleanConfig,
        depth: Int = 0,
        maxDepth: Int = 20
    ): List<FileInfo> {
        if (depth > maxDepth || !directory.isDirectory) return emptyList()
        if (FileScanner.shouldSkip(directory)) return emptyList()

        val results = mutableListOf<FileInfo>()
        val files = directory.listFiles() ?: return emptyList()

        for (file in files) {
            if (app.allever.android.sample.cleaner.safety.WhiteList.isProtected(file, config)) {
                continue
            }

            when {
                file.isFile && file.length() >= thresholdBytes -> {
                    results.add(
                        FileInfo(
                            file = file,
                            category = FileCategory.fromExtension(file.extension)
                        )
                    )
                }

                file.isDirectory && !FileScanner.shouldSkip(file) -> {
                    results.addAll(scanLargeFilesRecursive(file, thresholdBytes, config, depth + 1, maxDepth))
                }
            }
        }

        return results
    }

    /**
     * 收集文件用于重复检测
     */
    private fun collectFiles(
        dir: File,
        output: MutableList<File>,
        depth: Int = 0,
        maxDepth: Int = 15,
        config: CleanConfig = CleanConfig()
    ) {
        if (depth > maxDepth || !dir.isDirectory) return
        if (FileScanner.shouldSkip(dir)) return

        dir.listFiles()?.forEach { file ->
            if (!app.allever.android.sample.cleaner.safety.WhiteList.isProtected(file, config)) {
                if (file.isFile) {
                    output.add(file)
                } else if (file.isDirectory && !FileScanner.shouldSkip(file)) {
                    collectFiles(file, output, depth + 1, maxDepth, config)
                }
            }
        }
    }

    /**
     * 按分类收集文件
     */
    private fun collectByCategory(
        dir: File,
        targetCategory: FileCategory,
        extensions: Set<String>,
        output: MutableList<FileInfo>,
        depth: Int,
        maxDepth: Int
    ) {
        if (depth > maxDepth || !dir.isDirectory) return

        dir.listFiles()?.forEach { file ->
            when {
                file.isFile && file.extension.lowercase() in extensions -> {
                    output.add(FileInfo(file, targetCategory))
                }
                file.isDirectory && !FileScanner.shouldSkip(file) -> {
                    collectByCategory(file, targetCategory, extensions, output, depth + 1, maxDepth)
                }
            }
        }
    }

    /**
     * 计算 MD5 哈希值
     */
    internal fun calculateMd5(file: File): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    md.update(buffer, 0, read)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }
}
