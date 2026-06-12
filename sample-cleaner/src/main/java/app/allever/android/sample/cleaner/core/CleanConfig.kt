package app.allever.android.sample.cleaner.core

/**
 * 清理配置
 *
 * 管理清理行为的安全阈值和白名单规则，遵循开放封闭原则：
 * 通过配置对象控制清理范围，无需修改核心逻辑代码
 *
 * @param largeFileThresholdBytes 大文件识别阈值（字节），默认 10MB
 * @param maxFileAgeDays 最大文件保留天数，超过此天数的临时文件将被标记为可清理
 * @param minFileSizeBytes 最小文件大小过滤（字节），小于此值的空文件可被清理
 * @param enableParallelScan 是否启用并行扫描
 * @param parallelism 并行扫描线程数
 * @param protectedPaths 受保护的路径白名单
 * @param protectedExtensions 受保护的文件扩展名白名单
 */
data class CleanConfig(
    val largeFileThresholdBytes: Long = DEFAULT_LARGE_FILE_THRESHOLD,
    val maxFileAgeDays: Int = DEFAULT_MAX_FILE_AGE_DAYS,
    val minFileSizeBytes: Long = DEFAULT_MIN_FILE_SIZE,
    val enableParallelScan: Boolean = true,
    val parallelism: Int = DEFAULT_PARALLELISM,
    val protectedPaths: Set<String> = DEFAULT_PROTECTED_PATHS,
    val protectedExtensions: Set<String> = DEFAULT_PROTECTED_EXTENSIONS
) {
    companion object {
        const val DEFAULT_LARGE_FILE_THRESHOLD = 10L * 1024 * 1024 // 10MB
        const val DEFAULT_MAX_FILE_AGE_DAYS = 30
        const val DEFAULT_MIN_FILE_SIZE = 0L // 不限制最小大小
        const val DEFAULT_PARALLELISM = 4

        /** 默认受保护路径 */
        val DEFAULT_PROTECTED_PATHS: Set<String> = setOf(
            "/system",
            "/vendor",
            "/product",
            "/data/app",
            "/proc",
            "/sys"
        )

        /** 默认受保护扩展名 */
        val DEFAULT_PROTECTED_EXTENSIONS: Set<String> = setOf(
            "db", "db-journal", "sqlite", "sqlite3", "wal"
        )
    }

    /**
     * 大文件阈值是否匹配
     */
    fun isLargeFile(fileSize: Long): Boolean = fileSize >= largeFileThresholdBytes

    /**
     * 文件是否超龄
     */
    fun isExpired(lastModified: Long): Boolean {
        if (maxFileAgeDays <= 0) return false
        val ageMs = System.currentTimeMillis() - lastModified
        val ageDays = ageMs / (1000L * 60 * 60 * 24)
        return ageDays > maxFileAgeDays
    }
}
