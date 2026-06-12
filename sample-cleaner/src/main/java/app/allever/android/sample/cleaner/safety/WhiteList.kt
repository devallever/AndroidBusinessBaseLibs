package app.allever.android.sample.cleaner.safety

import app.allever.android.sample.cleaner.core.CleanConfig
import java.io.File

/**
 * 白名单管理
 *
 * 负责保护重要文件和目录不被清理，对应文档中的"白名单机制"。
 * 遵循单一职责原则：只负责判断一个文件是否受保护。
 *
 * 保护策略：
 * 1. 系统关键路径（/system, /vendor, /proc 等）
 * 2. 数据库文件（.db, .sqlite 等）
 * 3. 应用自身数据目录
 * 4. 用户自定义保护路径
 */
object WhiteList {

    /** 用户自定义的额外保护路径 */
    private val customProtectedPaths = mutableSetOf<String>()

    /** 用户自定义的保护扩展名 */
    private val customProtectedExtensions = mutableSetOf<String>()

    /**
     * 添加自定义保护路径
     *
     * @param path 要保护的绝对路径或路径前缀
     */
    fun addProtectedPath(path: String) {
        synchronized(customProtectedPaths) {
            customProtectedPaths.add(path)
        }
    }

    /**
     * 移除自定义保护路径
     */
    fun removeProtectedPath(path: String) {
        synchronized(customProtectedPaths) {
            customProtectedPaths.remove(path)
        }
    }

    /**
     * 添加自定义保护扩展名
     */
    fun addProtectedExtension(ext: String) {
        synchronized(customProtectedExtensions) {
            customProtectedExtensions.add(ext.lowercase())
        }
    }

    /**
     * 判断文件是否受白名单保护
     *
     * @param file 待检测文件
     * @param config 清理配置
     * @return true 表示受保护，不应清理
     */
    fun isProtected(file: File, config: CleanConfig = CleanConfig()): Boolean {
        // 检查配置中的受保护路径
        if (isPathProtected(file.absolutePath, config)) return true

        // 检查配置中的受保护扩展名
        if (isExtensionProtected(file.extension.lowercase(), config)) return true

        // 检查用户自定义保护
        if (isCustomProtected(file)) return true

        return false
    }

    /**
     * 判断路径是否受保护
     */
    private fun isPathProtected(absolutePath: String, config: CleanConfig): Boolean {
        // 配置中的默认保护路径
        for (protectedPath in config.protectedPaths) {
            if (absolutePath == protectedPath || absolutePath.startsWith("$protectedPath/")) {
                return true
            }
        }

        // 自定义保护路径
        synchronized(customProtectedPaths) {
            for (customPath in customProtectedPaths) {
                if (absolutePath == customPath || absolutePath.startsWith("$customPath/")) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * 判断扩展名是否受保护
     */
    private fun isExtensionProtected(extension: String, config: CleanConfig): Boolean {
        if (extension.isEmpty()) return false
        if (extension in config.protectedExtensions) return true

        synchronized(customProtectedExtensions) {
            if (extension in customProtectedExtensions) return true
        }

        return false
    }

    /**
     * 判断是否被用户自定义规则保护
     */
    private fun isCustomProtected(file: File): Boolean {
        // 可在此处扩展更多自定义保护逻辑
        return false
    }

    /**
     * 重置所有自定义保护规则
     */
    fun resetCustomRules() {
        synchronized(customProtectedPaths) { customProtectedPaths.clear() }
        synchronized(customProtectedExtensions) { customProtectedExtensions.clear() }
    }
}
