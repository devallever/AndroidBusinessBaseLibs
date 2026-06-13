package app.allever.android.sample.cleaner.storage

import android.util.Log
import app.allever.android.lib.core.store.StoreCore
import app.allever.android.sample.cleaner.core.CleanConfig
import app.allever.android.sample.cleaner.core.CleanResult
import app.allever.android.sample.cleaner.core.CleanType
import app.allever.android.sample.cleaner.safety.SafetyChecker
import app.allever.android.sample.cleaner.scanner.FileScanner
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import app.allever.android.sample.cleaner.scanner.JunkRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 存储清理器
 *
 * 存储清理模块的总入口，组合 CacheCleaner、TempCleaner、ApkCleaner 等独立 Cleaner，
 * 同时集成 StoreCore 进行存储数据清理。
 *
 * 各 Cleaner 职责单一，分别负责不同类型垃圾文件的扫描和清理：
 * - CacheCleaner: 应用缓存文件
 * - TempCleaner: 临时文件和日志文件
 * - ApkCleaner: APK 安装包
 * - 广告缓存/残留: 通过 FileScanner + JunkRule 扫描
 */
object StorageCleaner {

    private const val TAG = "StorageCleaner"

    // ========== 公共接口 ==========

    /**
     * 全面扫描所有可清理项目
     *
     * 各 Cleaner 并行执行，互不干扰。
     *
     * @param config 清理配置
     * @param onJobCreated 协程 Job 创建回调（用于外部取消控制）
     * @return 按类型分组的扫描结果
     */
    suspend fun fullScan(
        config: CleanConfig = CleanConfig(),
        onJobCreated: ((Job) -> Unit)? = null
    ): Map<CleanType, List<JunkFileItem>> =
        coroutineScope {
            val startTime = System.currentTimeMillis()

            Log.i(TAG, "[fullScan] 开始全面扫描")
            Log.d(TAG, "[fullScan] 并行度=${config.parallelism}")

            // 创建顶层 Job 供外部取消
            val scanJob = currentCoroutineContext()[Job]
            if (scanJob != null) {
                onJobCreated?.invoke(scanJob)
                Log.d(TAG, "[fullScan] scanJob 已注册到 Engine")
            }

            val cacheDeferred = async { CacheCleaner.scan() }
            val tempDeferred = async { TempCleaner.scan(maxAgeDays = config.maxFileAgeDays) }
            val adCacheDeferred = async { scanAdCache(config) }
            val apkDeferred = async { ApkCleaner.scan() }

            // 检查取消状态
            ensureActive()

            val results = mutableMapOf<CleanType, List<JunkFileItem>>()

            // 缓存
            cacheDeferred.await().also { items ->
                ensureActive()
                if (items.isNotEmpty()) results[CleanType.CACHE] = items
                Log.d(TAG, "[fullScan] [CACHE] ${items.size} 个文件")
            }

            // 临时/日志
            tempDeferred.await().also { items ->
                ensureActive()
                if (items.isNotEmpty()) results[CleanType.TEMP] = items
                Log.d(TAG, "[fullScan] [TEMP] ${items.size} 个文件")
            }

            // 广告缓存
            adCacheDeferred.await().also { items ->
                ensureActive()
                if (items.isNotEmpty()) results[CleanType.AD_CACHE] = items
                Log.d(TAG, "[fullScan] [AD_CACHE] ${items.size} 个文件")
            }

            // APK
            apkDeferred.await().also { items ->
                ensureActive()
                if (items.isNotEmpty()) results[CleanType.APK] = items
                Log.d(TAG, "[fullScan] [APK] ${items.size} 个文件")
            }

            val totalCostMs = System.currentTimeMillis() - startTime
            val totalCount = results.values.sumOf { it.size }
            val totalSize = results.values.flatten().sumOf { it.size }

            Log.i(
                TAG,
                "[fullScan] 全量扫描完成, $totalCount 个文件, " +
                    "${JunkFileItem.formatFileSize(totalSize)}, 耗时 ${totalCostMs}ms"
            )

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
        Log.i(TAG, "[scanByType] 扫描类型: $type")

        ensureActive()

        val result = when (type) {
            CleanType.CACHE -> CacheCleaner.scan()
            CleanType.LOG, CleanType.TEMP -> TempCleaner.scan(maxAgeDays = config.maxFileAgeDays)
            CleanType.AD_CACHE -> scanAdCache(config)
            CleanType.RESIDUAL -> scanResidual(config)
            CleanType.APK -> ApkCleaner.scan()
            CleanType.ALL -> fullScan(config).values.flatten()
            else -> emptyList()
        }.also {
            ensureActive()
            Log.i(
                TAG,
                "[scanByType] [$type] 完成, ${it.size} 个文件"
            )
        }

        result
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
        val startTime = System.currentTimeMillis()
        val selectedCount = items.count { it.selected }

        Log.i(TAG, "[clean] 开始清理, 共 ${items.size} 项, 已选 $selectedCount 项")

        ensureActive()

        val results = mutableListOf<CleanResult>()

        // 安全校验过滤
        val safeItems = SafetyChecker.filterSafeItems(items)
        if (safeItems.size != items.size) {
            Log.d(TAG, "[clean] 安全校验过滤: ${items.size} → ${safeItems.size}")
        }

        // 按 CleanType 分组
        val grouped = safeItems.groupBy { it.type }
        Log.d(TAG, "[clean] 分组后类型数: ${grouped.keys}")

        for ((type, typeItems) in grouped) {
            ensureActive()

            val result = when (type) {
                CleanType.CACHE -> CacheCleaner.clean(typeItems)
                CleanType.TEMP, CleanType.LOG -> TempCleaner.clean(typeItems)
                CleanType.APK -> ApkCleaner.clean(typeItems)
                CleanType.AD_CACHE -> cleanGeneric(typeItems, type)
                CleanType.RESIDUAL -> cleanGeneric(typeItems, type)
                else -> cleanGeneric(typeItems, type)
            }
            Log.i(
                TAG,
                "[clean] [$type] 清理完成: ${result.cleanedCount} 个文件, " +
                    "${JunkFileItem.formatFileSize(result.cleanedSize)}, " +
                    "耗时 ${result.costTimeMs}ms"
            )
            results.add(result)
        }

        if (clearStoreData) {
            try {
                StoreCore.clear()
                Log.d(TAG, "[clean] StoreCore 数据已清除")
                results.add(CleanResult(CleanType.CACHE, true))
            } catch (e: Exception) {
                Log.e(TAG, "[clean] StoreCore 清除失败: ${e.message}", e)
                results.add(CleanResult.failed(CleanType.CACHE))
            }
        }

        val totalCostMs = System.currentTimeMillis() - startTime
        val totalCleanedSize = results.sumOf { it.cleanedSize }
        val totalCleanedCount = results.sumOf { it.cleanedCount }

        Log.i(
            TAG,
            "[clean] 全部清理完成, 共删除 $totalCleanedCount 个文件, " +
                "释放 ${JunkFileItem.formatFileSize(totalCleanedSize)}, " +
                "总耗时 ${totalCostMs}ms"
        )

        results
    }

    // ========== 内部方法 ==========

    /**
     * 扫描广告缓存
     */
    private suspend fun scanAdCache(config: CleanConfig): List<JunkFileItem> {
        return FileScanner.scan(
            rootDir = File("/storage/emulated/0/Android"),
            rules = listOf(JunkRule.adCacheRule()),
            config = config,
            strategy = FileScanner.Strategy.BFS
        ).also {
            Log.v(TAG, "[scanAdCache] 命中 ${it.size} 个广告缓存文件")
        }
    }

    /**
     * 扫描残留文件（已卸载应用的遗留目录）
     */
    private suspend fun scanResidual(config: CleanConfig): List<JunkFileItem> {
        return FileScanner.scan(
            rootDir = File("/storage/emulated/0/Android"),
            rules = listOf(JunkRule.residualRule()),
            config = config,
            strategy = FileScanner.Strategy.BFS
        ).also {
            Log.v(TAG, "[scanResidual] 命中 ${it.size} 个残留目录")
        }
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
            ensureActive()
            if (!item.selected) continue
            if (SafetyChecker.safeDelete(item.file)) {
                cleanedSize += item.size
                cleanedCount++
                cleanedFiles.add(item.file)
                Log.v(TAG, "[doClean] 已删除: ${item.file.name} (${item.size}B)")
            } else {
                Log.w(TAG, "[doClean] 删除失败: ${item.file.absolutePath}")
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
