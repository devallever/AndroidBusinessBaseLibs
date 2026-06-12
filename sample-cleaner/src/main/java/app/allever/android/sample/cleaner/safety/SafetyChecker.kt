package app.allever.android.sample.cleaner.safety

import app.allever.android.sample.cleaner.core.CleanConfig
import app.allever.android.sample.cleaner.scanner.JunkFileItem
import java.io.File

/**
 * 安全校验器
 *
 * 在执行清理操作前进行最终安全检查，
 * 确保不会误删重要文件。对应文档中的"安全清理机制"。
 *
 * 职责：
 * - 过滤白名单保护的文件
 * - 校验文件是否存在且可写
 * - 提供清理前的二次确认数据
 */
object SafetyChecker {

    /**
     * 过滤掉受保护的文件
     *
     * @param items 原始扫描结果
     * @param config 清理配置
     * @return 过滤后可安全清理的文件列表
     */
    fun filterSafeItems(
        items: List<JunkFileItem>,
        config: CleanConfig = CleanConfig()
    ): List<JunkFileItem> {
        return items.filter { item ->
            !WhiteList.isProtected(item.file, config) && isDeletable(item.file)
        }
    }

    /**
     * 检查单个文件是否可以安全删除
     *
     * @param file 目标文件
     * @return 是否可安全删除
     */
    fun canDelete(file: File, config: CleanConfig = CleanConfig()): Boolean {
        // 白名单检查
        if (WhiteList.isProtected(file, config)) return false

        // 文件存在性检查
        if (!file.exists()) return false

        // 可删除性检查
        return isDeletable(file)
    }

    /**
     * 校验文件是否可删除（存在、非空引用、有权限）
     */
    internal fun isDeletable(file: File): Boolean {
        if (!file.exists()) return false

        // 目录需要能列出内容或为空
        if (file.isDirectory) {
            // 非空目录也可以删除（递归删除），但需要确认父目录可写
            return file.parentFile?.canWrite() ?: false
        }

        // 普通文件：存在且父目录可写
        return file.parentFile?.canWrite() ?: false
    }

    /**
     * 统计待清理项的总大小和数量
     *
     * @param items 已选中的清理项
     * @return Pair(总大小, 总数量)
     */
    fun calculateSummary(items: List<JunkFileItem>): Pair<Long, Int> {
        val selected = items.filter { it.selected }
        val totalSize = selected.sumOf { it.size }
        return totalSize to selected.size
    }

    /**
     * 执行安全的文件删除操作
     *
     * @param file 要删除的文件或目录
     * @return 是否删除成功
     */
    fun safeDelete(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
