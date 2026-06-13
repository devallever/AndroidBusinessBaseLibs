package app.allever.android.sample.cleaner.storage

import android.os.Environment
import android.util.Log
import app.allever.android.lib.core.app.App
import app.allever.android.sample.cleaner.core.CleanConfig
import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.core.CleanType
import app.allever.android.sample.cleaner.scanner.FileScanner
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import app.allever.android.sample.cleaner.scanner.JunkRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * APK 安装包清理器
 *
 * 扫描并清理已下载但可能不再需要的 APK 安装包文件。
 * 扫描范围：
 * - Download 目录下的 .apk 文件
 * - 外部存储根目录下的 .apk 文件
 */
object ApkCleaner {

    private const val TAG = "ApkCleaner"

    /** 最小文件大小阈值（100KB），过滤无效小文件 */
    private const val MIN_APK_SIZE_BYTES = 1024L * 100

    /**
     * 扫描 APK 文件
     *
     * @return 扫描到的 APK 文件列表
     */
    suspend fun scan(): List<JunkFileItem> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val results = mutableListOf<JunkFileItem>()
        val config = CleanConfig()

        Log.i(TAG, "[scan] 开始扫描 APK 文件 (最小大小=${MIN_APK_SIZE_BYTES / 1024}KB)")

        // 1. 扫描 Download 目录
        try {
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir.exists() && downloadDir.canRead()) {
                val apkFiles = downloadDir.listFiles()
                    ?.filter { it.isFile && it.extension.lowercase() == "apk" }
                    ?.filter { it.length() >= MIN_APK_SIZE_BYTES }
                    ?: emptyList()

                Log.d(TAG, "[scan] Download 目录发现 ${apkFiles.size} 个 APK 文件")

                apkFiles.forEach { file ->
                    if (!app.allever.android.sample.cleaner.safety.WhiteList.isProtected(file, config)) {
                        results.add(JunkFileItem(file = file, type = CleanType.APK))
                    }
                }
            } else {
                Log.d(TAG, "[scan] Download 目录不存在或不可读: ${downloadDir.absolutePath}")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "[scan] Download 目录无权限访问: ${e.message}")
        }

        // 2. 扫描外部存储根目录（顶层 APK 文件）
        try {
            val externalRoot = Environment.getExternalStorageDirectory()
            if (externalRoot.exists() && externalRoot.canRead()) {
                val topApkFiles = externalRoot.listFiles()
                    ?.filter { it.isFile && it.extension.lowercase() == "apk" }
                    ?.filter { it.length() >= MIN_APK_SIZE_BYTES }
                    ?: emptyList()

                Log.d(TAG, "[scan] 根目录顶层发现 ${topApkFiles.size} 个 APK 文件")

                topApkFiles.forEach { file ->
                    if (!app.allever.android.sample.cleaner.safety.WhiteList.isProtected(file, config)) {
                        results.add(JunkFileItem(file = file, type = CleanType.APK))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "[scan] 根目录无权限访问: ${e.message}")
        }

        val costMs = System.currentTimeMillis() - startTime
        val totalSize = results.sumOf { it.size }
        Log.i(
            TAG,
            "[scan] 完成, 共 ${results.size} 个 APK, " +
                "${JunkFileItem.formatFileSize(totalSize)}, 耗时 ${costMs}ms"
        )

        results
    }

    /**
     * 清理选中的 APK 文件
     *
     * @param items 待清理的文件列表
     * @return 清理结果
     */
    suspend fun clean(items: List<JunkFileItem>): CleanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var cleanedSize = 0L
        var cleanedCount = 0
        val cleanedFiles = mutableListOf<java.io.File>()
        val selectedItems = items.filter { it.selected }

        Log.i(TAG, "[clean] 开始清理, 共 ${items.size} 项, 已选 ${selectedItems.size} 项")

        for (item in selectedItems) {
            if (item.file.delete()) {
                cleanedSize += item.size
                cleanedCount++
                cleanedFiles.add(item.file)
                Log.v(TAG, "[clean] 已删除: ${item.fileName}")
            } else {
                Log.w(TAG, "[clean] 删除失败: ${item.absolutePath}")
            }
        }

        CleanResult(
            type = CleanType.APK,
            success = true,
            cleanedSize = cleanedSize,
            cleanedCount = cleanedCount,
            costTimeMs = System.currentTimeMillis() - startTime,
            cleanedFiles = cleanedFiles
        ).also {
            Log.i(
                TAG,
                "[clean] 完成, 删除 $cleanedCount 个 APK, " +
                    "${JunkFileItem.formatFileSize(cleanedSize)}"
            )
        }
    }
}
