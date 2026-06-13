package app.allever.android.sample.cleaner.core

import android.util.Log
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 清理引擎 - 总调度器
 *
 * 对外统一暴露 scan → preview → clean 三阶段流程，
 * 内部协调 StorageCleaner / MemoryCleaner / FileScanner 等各子模块。
 *
 * 遵循原则：
 * - 单一职责：只负责调度编排，不直接实现具体扫描/清理逻辑
 * - 依赖倒置：通过接口依赖各 Cleaner，不耦合具体实现
 * - 开放封闭：新增清理类型只需扩展，无需修改 Engine 核心逻辑
 */
object CleanEngine {

    private const val TAG = "CleanEngine"

    // ========== 状态管理（响应式） ==========

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    /** 扫描状态流，供 UI 观察 */
    val scanState: Flow<ScanState> = _scanState.asStateFlow()

    private val _cleanState = MutableStateFlow<CleanState>(CleanState.Idle)
    /** 清理状态流，供 UI 观察 */
    val cleanState: Flow<CleanState> = _cleanState.asStateFlow()

    // ========== 扫描流程 ==========

    /**
     * 执行全面扫描
     *
     * 流程：Idle → Scanning → Scanned(结果)
     */
    suspend fun fullScan(config: CleanConfig = CleanConfig()): Map<CleanType, List<JunkFileItem>> {
        Log.i(TAG, "[fullScan] 状态转换: ${_scanState.value} → Scanning")
        _scanState.value = ScanState.Scanning

        return try {
            val results =
                app.allever.android.sample.cleaner.storage.StorageCleaner.fullScan(config)
            _scanState.value = ScanState.Scanned(results)
            Log.i(
                TAG,
                "[fullScan] 状态转换: Scanning → Scanned(${results.keys.size} 种类型)"
            )
            results
        } catch (e: Exception) {
            _scanState.value = ScanState.Error(e.message ?: "扫描失败")
            Log.e(TAG, "[fullScan] 状态转换: Scanning → Error: ${e.message}", e)
            emptyMap()
        }
    }

    /**
     * 按类型扫描
     */
    suspend fun scanByType(
        type: CleanType,
        config: CleanConfig = CleanConfig()
    ): List<JunkFileItem> {
        Log.i(TAG, "[scanByType] 状态转换: ${_scanState.value} → Scanning, 类型=$type")
        _scanState.value = ScanState.Scanning

        return try {
            val results =
                app.allever.android.sample.cleaner.storage.StorageCleaner.scanByType(type, config)
            _scanState.value = ScanState.Scanned(mapOf(type to results))
            Log.i(TAG, "[scanByType] 状态转换: Scanning → Scanned(${results.size} 个)")
            results
        } catch (e: Exception) {
            _scanState.value = ScanState.Error(e.message ?: "扫描失败")
            Log.e(TAG, "[scanByType] 扫描失败 [$type]: ${e.message}", e)
            emptyList()
        }
    }

    // ========== 清理流程 ==========

    /**
     * 执行清理操作
     *
     * @param items 待清理项（通过 selected 字段控制）
     * @param clearStoreData 是否同时清理存储数据
     * @return 清理结果列表
     */
    suspend fun clean(
        items: List<JunkFileItem>,
        clearStoreData: Boolean = false
    ): List<CleanResult> {
        val selectedCount = items.count { it.selected }
        Log.i(
            TAG,
            "[clean] 状态转换: ${_cleanState.value} → Cleaning, " +
                "共${items.size}项, 已选${selectedCount}项, clearStoreData=$clearStoreData"
        )
        _cleanState.value = CleanState.Cleaning(0, selectedCount)

        return try {
            val results =
                app.allever.android.sample.cleaner.storage.StorageCleaner.clean(items, clearStoreData)

            val totalSize = results.sumOf { it.cleanedSize }
            val totalCount = results.sumOf { it.cleanedCount }

            _cleanState.value = CleanState.Completed(results, totalSize, totalCount)
            Log.i(
                TAG,
                "[clean] 状态转换: Cleaning → Completed(" +
                    "释放${formatSize(totalSize)}, ${totalCount}个文件)"
            )
            results
        } catch (e: Exception) {
            _cleanState.value = CleanState.Error(e.message ?: "清理失败")
            Log.e(TAG, "[clean] 清理失败: ${e.message}", e)
            emptyList()
        }
    }

    // ========== 工具方法 ==========

    /**
     * 重置所有状态
     */
    fun reset() {
        Log.d(TAG, "[reset] 重置所有状态")
        _scanState.value = ScanState.Idle
        _cleanState.value = CleanState.Idle
    }

    /**
     * 格式化文件大小为可读字符串
     */
    fun formatSize(size: Long): String = JunkFileItem.formatFileSize(size)

    // ========== 状态定义 ==========

    /**
     * 扫描状态密封类
     */
    sealed class ScanState {
        data object Idle : ScanState()
        data object Scanning : ScanState()

        data class Scanned(val results: Map<CleanType, List<JunkFileItem>>) : ScanState()

        data class Error(val message: String) : ScanState()
    }

    /**
     * 清理状态密封类
     */
    sealed class CleanState {
        data object Idle : CleanState()

        data class Cleaning(
            val currentProgress: Int,
            val totalProgress: Int
        ) : CleanState()

        data class Completed(
            val results: List<CleanResult>,
            val totalSize: Long,
            val totalCount: Int
        ) : CleanState()

        data class Error(val message: String) : CleanState()
    }
}
