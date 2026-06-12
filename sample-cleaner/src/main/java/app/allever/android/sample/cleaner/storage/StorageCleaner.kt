package app.allever.android.sample.cleaner.storage

import app.allever.android.lib.store.core.StoreCore
import app.allever.android.sample.cleaner.core.CleanConfig
import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.core.CleanType
import app.allever.android.sample.cleaner.safety.SafetyChecker
import app.allever.android.sample.cleaner.scanner.FileScanner
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import app.allever.android.sample.cleaner.scanner.JunkRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 存储清理器
 *
 * 存储清理模块的总入口，组合 CacheCleaner 和 TempCleaner，
 * 同时集成 StoreCore 进行存储数据清理。
 *
 * 对应文档"存储清理"章节，遵循依赖倒置原则：
 * 通过接口/委托调用底层具体清理实现，自身只负责编排调度。
 */
object StorageCleaner {

    /**
     * 全面扫描所有可清理项目
     *
     * @param config 清理配置
     * @return 按类型分组的扫描结果
     */
    suspend fun fullScan(config: CleanConfig = CleanConfig()): Map<CleanType, List<JunkFileItem>> =
        coroutineScope {
            val cacheDeferred = async { CacheCleaner.scan() }
            val tempDeferred = async { TempCleaner.scan(maxAgeDays = config.maxFileAgeDays) }
            val adCacheDeferred = async { scanAdCache(config) }
            val apkDeferred = async { ApkCleaner.scan() }

            val results = mutableMapOf<CleanType, List<JunkFileItem>>()

            val cacheItems = cacheDeferred.await()
            if (cacheItems.isNotEmpty()) results[CleanType.CACHE] = cacheItems

            val tempItems = tempDeferred.await()
            if (tempItems.isNotEmpty()) results[CleanType.TEMP] = tempItems

            val adCacheItems = adCacheDeferred.await()
            if (adCacheItems.isNotEmpty()) results[CleanType.AD_CACHE] = adCacheItems

            val apkItems = apkDeferred.await()
            if (apkItems.isNotEmpty()) results[CleanType.APK] = apkItems

            results
        }

    /**
     * 按指定类型扫描
     *
     * @param type 清理类型
     * @param config 配置
     * @return 扫描结果
     */
    suspend fun scanByType(
        type: CleanType,
        config: CleanConfig = CleanConfig()
    ): List<JunkFileItem> = withContext(Dispatchers.IO) {
        when (type) {
            CleanType.CACHE -> CacheCleaner.scan()
            CleanType.LOG, CleanType.TEMP -> TempCleaner.scan(maxAgeDays = config.maxFileAgeDays)
            CleanType.AD_CACHE -> scanAdCache(config)
            CleanType.RESIDUAL -> scanResidual(config)
            CleanType.APK -> ApkCleaner.scan()
            CleanType.ALL -> fullScan(config).values.flatten()
            else -> emptyList()
        }
    }

    /**
     * 执行清理操作
     *
     * @param items 待清理项列表（通过 selected 字段控制是否实际清理）
     * @param clearStoreData 是否同时清理 StoreCore 存储数据
     * @return 各类型的清理结果
     */
    suspend fun clean(
        items: List<JunkFileItem>,
        clearStoreData: Boolean = false
    ): List<CleanResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<CleanResult>()

        // 安全校验过滤
        val safeItems = SafetyChecker.filterSafeItems(items)

        // 按 CleanType 分组
        val grouped = safeItems.groupBy { it.type }

        for ((type, typeItems) in grouped) {
            val result = when (type) {
                CleanType.CACHE -> CacheCleaner.clean(typeItems)
                CleanType.TEMP, CleanType.LOG -> TempCleaner.clean(typeItems)
                CleanType.APK -> ApkCleaner.clean(typeItems)
                CleanType.AD_CACHE -> cleanGeneric(typeItems, type)
                CleanType.RESIDUAL -> cleanGeneric(typeItems, type)
                else -> cleanGeneric(typeItems, type)
            }
            results.add(result)
        }

        // 可选：清理 StoreCore 存储数据
        if (clearStoreData) {
            try {
                StoreCore.clear()
                results.add(CleanResult(CleanType.CACHE, true))
            } catch (_: Exception) {
                results.add(CleanResult.failed(CleanType.CACHE))
            }
        }

        results
    }

    /**
     * 计算总的可清理大小
     */
    fun calculateTotalSize(items: List<JunkFileItem>): Pair<Long, Int> {
        return SafetyChecker.calculateSummary(items)
    }

    /**
     * 扫描广告缓存
     */
    private suspend fun scanAdCache(config: CleanConfig): List<JunkFileItem> {
        return FileScanner.scan(
            rootDir = File("/storage/emulated/0/Android"),
            rules = listOf(JunkRule.adCacheRule()),
            config = config,
            strategy = FileScanner.Strategy.BFS
        )
    }

    /**
     * 扫描残留文件
     */
    private suspend fun scanResidual(config: CleanConfig): List<JunkFileItem> {
        return FileScanner.scan(
            rootDir = File("/storage/emulated/0/Android"),
            rules = listOf(JunkRule.residualRule()),
            config = config,
            strategy = FileScanner.Strategy.BFS
        )
    }

    /**
     * 通用清理方法（用于 AD_CACHE、RESIDUAL 等非专用 Cleaner 处理的类型）
     */
    private suspend fun cleanGeneric(
        items: List<JunkFileItem>,
        type: CleanType
    ): CleanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var cleanedSize = 0L
        var cleanedCount = 0
        val cleanedFiles = mutableListOf<File>()

        for (item in items) {
            if (!item.selected) continue
            if (SafetyChecker.safeDelete(item.file)) {
                cleanedSize += item.size
                cleanedCount++
                cleanedFiles.add(item.file)
            }
        }

        CleanResult(
            type = type,
            success = true,
            cleanedSize = cleanedSize,
            cleanedCount = cleanedCount,
            costTimeMs = System.currentTimeMillis() - startTime,
            cleanedFiles = cleanedFiles
        )
    }
}
