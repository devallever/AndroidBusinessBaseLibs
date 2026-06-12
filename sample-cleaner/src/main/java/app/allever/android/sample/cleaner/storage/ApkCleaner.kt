package app.allever.android.sample.cleaner.storage

import android.os.Environment
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

    /**
     * 扫描 APK 文件
     *
     * @return 扫描到的 APK 文件列表
     */
    suspend fun scan(): List<JunkFileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<JunkFileItem>()
        val rule = JunkRule.apkRule()
        val config = CleanConfig()

        // 1. 扫描 Download 目录
        try {
            val downloadDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir.exists() && downloadDir.canRead()) {
                downloadDir.listFiles()
                    ?.filter { it.isFile && it.extension.lowercase() == "apk" }
                    ?.filter { it.length() >= 1024 * 100 }
                    ?.forEach { file ->
                        if (!app.allever.android.sample.cleaner.safety.WhiteList.isProtected(file, config)) {
                            results.add(JunkFileItem(file = file, type = CleanType.APK))
                        }
                    }
            }
        } catch (_: SecurityException) {}

        // 2. 扫描外部存储根目录（顶层 APK 文件）
        try {
            val externalRoot = Environment.getExternalStorageDirectory()
            if (externalRoot.exists() && externalRoot.canRead()) {
                externalRoot.listFiles()
                    ?.filter { it.isFile && it.extension.lowercase() == "apk" }
                    ?.forEach { file ->
                        if (!app.allever.android.sample.cleaner.safety.WhiteList.isProtected(file, config)
                            && file.length() >= 1024 * 100) {  // >= 100KB
                            results.add(JunkFileItem(file = file, type = CleanType.APK))
                        }
                    }
            }
        } catch (_: SecurityException) {}

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

        for (item in items) {
            if (item.file.delete()) {
                cleanedSize += item.size
                cleanedCount++
                cleanedFiles.add(item.file)
            }
        }

        CleanResult(
            type = CleanType.APK,
            success = true,
            cleanedSize = cleanedSize,
            cleanedCount = cleanedCount,
            costTimeMs = System.currentTimeMillis() - startTime,
            cleanedFiles = cleanedFiles
        )
    }
}
