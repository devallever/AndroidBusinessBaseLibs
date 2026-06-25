package app.allever.android.lib.player.core.cache

/**
 * 视频缓存配置
 *
 * 用于在播放前配置缓存行为
 *
 * @param enabled 是否启用缓存（默认 true）
 * @param allowSaveToExternalStorage 是否允许缓存到外部存储（默认 false，节省内部存储空间）
 * @param maxSingleFileSize 单个视频最大缓存大小（字节，0 表示不限制）
 * @param maxTotalCacheSize 最大总缓存大小（字节，0 表示不限制，默认 512MB）
 */
data class CacheConfig(
    val enabled: Boolean = true,
    val allowSaveToExternalStorage: Boolean = false,
    val maxSingleFileSize: Long = 0L,
    val maxTotalCacheSize: Long = 512 * 1024 * 1024L  // 默认 512MB
)

/**
 * 缓存文件名生成接口
 *
 * 用于自定义缓存文件的命名规则
 */
interface FileNameGenerator {
    /**
     * 根据原始 URL 生成缓存文件名
     *
     * @param url 原始视频 URL
     * @return 缓存文件名（不含扩展名）
     */
    fun generate(url: String): String
}
