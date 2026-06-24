package app.allever.android.sample.audiovideo.core.player.cache

import android.content.Context
import android.os.Environment
import android.util.Log
import app.allever.android.lib.core.app.App
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.FileNameGenerator as CacheFileNameGenerator
import java.io.File
import java.security.MessageDigest
import java.util.Locale

/**
 * 视频缓存管理器（单例）
 *
 * 职责：
 * - 管理 HttpProxyCacheServer 生命周期
 * - 提供统一的缓存URL转换接口
 * - 配置缓存策略（大小、路径、是否启用）
 * - 支持内部存储和外部存储
 *
 * 使用方式：
 * ```kotlin
 * // 初始化（在 Application 中调用一次）
 * VideoCacheManager.init(applicationContext)
 *
 * // 获取代理URL（自动处理缓存）
 * val proxyUrl = VideoCacheManager.getProxyUrl("https://example.com/video.mp4")
 * videoPlayer.setSource(proxyUrl)
 *
 * // 检查是否已缓存
 * val isCached = VideoCacheManager.isCached("https://example.com/video.mp4")
 * ```
 */
object VideoCacheManager {

    private const val TAG = "VideoCacheManager"

    /** 缓存目录名称（内部存储）*/
    private const val INTERNAL_CACHE_DIR_NAME = "video_cache"

    /** 缓存目录名称（外部存储）*/
    private const val EXTERNAL_CACHE_DIR_NAME = "AndroidVideoCache"

    /** 默认最大缓存大小：512MB */
    private const val DEFAULT_MAX_CACHE_SIZE = 512L * 1024 * 1024L

    // ==================== 配置项 ====================

    /** 是否启用缓存（默认 true）*/
    var isCacheEnabled: Boolean = true

    /** 是否允许保存到外部存储（默认 false）*/
    var allowExternalStorage: Boolean = false

    /** 最大缓存大小（字节）*/
    var maxCacheSizeBytes: Long = DEFAULT_MAX_CACHE_SIZE

    /** Application Context 引用 */
    private var appContext: Context? = App.context

    // ==================== 核心方法 ====================

    /**
     * 获取代理URL（带缓存功能）
     *
     * 工作原理：
     * - 如果缓存已启用且视频已完全缓存 → 返回本地文件代理URL
     * - 如果缓存已启用但未缓存 → 边下载边播放，同时缓存到本地
     * - 如果缓存已禁用 → 直接返回原始URL
     *
     * @param originalUrl 原始网络URL
     * @return 代理URL或原始URL
     */
    fun getProxyUrl(originalUrl: String): String {
        // 检查是否初始化
        if (appContext == null) {
            Log.w(TAG, "VideoCacheManager 未初始化，直接返回原始URL")
            return originalUrl
        }

        // 检查是否启用缓存
        if (!isCacheEnabled) {
            Log.d(TAG, "缓存已禁用，使用原始URL: $originalUrl")
            return originalUrl
        }

        // 检查是否为网络URL
        if (!isNetworkUrl(originalUrl)) {
            Log.d(TAG, "非网络URL，不进行缓存: $originalUrl")
            return originalUrl
        }

        try {
            val proxyUrl = proxyCacheServer.getProxyUrl(originalUrl)
            val isCached = isCached(originalUrl)

            Log.d(TAG, "原始URL: $originalUrl")
            Log.d(TAG, "代理URL: $proxyUrl")
            Log.d(TAG, "是否已完全缓存: $isCached")

            return proxyUrl
        } catch (e: Exception) {
            Log.e(TAG, "获取代理URL失败，使用原始URL", e)
            return originalUrl
        }
    }

    /**
     * 检查视频是否已完全缓存
     *
     * @param url 原始URL
     * @return true 已完全缓存，false 未缓存或部分缓存
     */
    fun isCached(url: String): Boolean {
        return try {
            if (appContext == null || !isCacheEnabled || !isNetworkUrl(url)) {
                false
            } else {
                proxyCacheServer.isCached(url)
            }
        } catch (e: Exception) {
            Log.e(TAG, "检查缓存状态失败", e)
            false
        }
    }

    /**
     * 获取缓存文件的完整路径
     *
     * 注意：此方法通过缓存目录和文件名生成器来推断缓存文件路径，
     * 因为 AndroidVideoCache 库的 getCacheFile() 方法是私有的。
     *
     * @param url 原始URL
     * @return 缓存文件对象，如果未缓存则返回null
     */
    fun getCacheFile(url: String): File? {
        return try {
            if (appContext == null || !isCacheEnabled || !isNetworkUrl(url)) {
                null
            } else {
                // 通过缓存目录和文件名生成器构建路径
                val cacheDir = if (allowExternalStorage && isExternalStorageWritable()) {
                    getExternalCacheDirectory() ?: getInternalCacheDirectory()
                } else {
                    getInternalCacheDirectory()
                }

                cacheDir?.let { dir ->
                    val fileName = Md5FileNameGenerator().generate(url)
                    val cacheFile = File(dir, fileName)
                    cacheFile.takeIf { it.exists() }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取缓存文件路径失败", e)
            null
        }
    }

    /**
     * 清除指定URL的缓存
     *
     * @param url 原始URL
     */
    fun clearCache(url: String) {
        try {
            if (appContext != null && isCacheEnabled && isNetworkUrl(url)) {
                val cacheFile = getCacheFile(url)
                if (cacheFile != null && cacheFile.exists()) {
                    val deleted = cacheFile.delete()
                    Log.d(TAG, "清除缓存: $url, 结果: $deleted")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清除缓存失败", e)
        }
    }

    /**
     * 清除所有缓存
     *
     * 注意：此操作不可逆，请谨慎调用
     */
    fun clearAllCache() {
        try {
            if (appContext != null) {
                // 清除内部存储缓存
                val internalCacheDir = getInternalCacheDirectory()
                internalCacheDir?.let { dir ->
                    deleteRecursive(dir)
                    Log.d(TAG, "已清除内部存储缓存: ${dir.absolutePath}")
                }

                // 清除外部存储缓存
                if (allowExternalStorage) {
                    val externalCacheDir = getExternalCacheDirectory()
                    externalCacheDir?.let { dir ->
                        deleteRecursive(dir)
                        Log.d(TAG, "已清除外部存储缓存: ${dir.absolutePath}")
                    }
                }

                Log.i(TAG, "所有缓存已清除")
            }
        } catch (e: Exception) {
            Log.e(TAG, "清除所有缓存失败", e)
        }
    }

    /**
     * 获取当前缓存统计信息
     *
     * @return CacheStats 对象，包含总大小和文件数量
     */
    fun getCacheStats(): CacheStats {
        var totalSize = 0L
        var fileCount = 0

        try {
            if (appContext != null) {
                // 统计内部存储
                val internalCacheDir = getInternalCacheDirectory()
                if (internalCacheDir?.exists() == true) {
                    val stats = calculateDirectoryStats(internalCacheDir)
                    totalSize += stats.first
                    fileCount += stats.second
                }

                // 统计外部存储
                if (allowExternalStorage) {
                    val externalCacheDir = getExternalCacheDirectory()
                    if (externalCacheDir?.exists() == true) {
                        val stats = calculateDirectoryStats(externalCacheDir)
                        totalSize += stats.first
                        fileCount += stats.second
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取缓存统计信息失败", e)
        }

        return CacheStats(
            totalSize = totalSize,
            fileCount = fileCount,
            formattedSize = formatFileSize(totalSize)
        )
    }

    /**
     * 关闭缓存服务器
     *
     * 应在应用退出时调用以释放资源
     */
    fun shutdown() {
        try {
            // 尝试关闭缓存服务器（如果已初始化）
            // 注意：lazy 属性无法直接检查初始化状态，通过异常捕获处理
            proxyCacheServer.shutdown()
            Log.d(TAG, "缓存服务器已关闭")
        } catch (e: UninitializedPropertyAccessException) {
            // 服务器未初始化，无需关闭
            Log.d(TAG, "缓存服务器未初始化，无需关闭")
        } catch (e: Exception) {
            Log.e(TAG, "关闭缓存服务器失败", e)
        }
    }

    // ==================== 内部实现 ====================

    /**
     * 懒初始化的 HttpProxyCacheServer 实例
     */
    private val proxyCacheServer: HttpProxyCacheServer by lazy {
        createProxyCacheServer()
    }

    /**
     * 创建并配置 HttpProxyCacheServer
     */
    private fun createProxyCacheServer(): HttpProxyCacheServer {
        val context = appContext ?: throw IllegalStateException("VideoCacheManager 未初始化")

        // 确定缓存目录
        val cacheDir = if (allowExternalStorage && isExternalStorageWritable()) {
            getExternalCacheDirectory() ?: getInternalCacheDirectory()
        } else {
            getInternalCacheDirectory()
        }

        // 确保缓存目录存在
        cacheDir?.mkdirs()

        Log.d(TAG, "缓存目录: ${cacheDir?.absolutePath}")
        Log.d(TAG, "最大缓存大小: ${formatFileSize(maxCacheSizeBytes)}")

        return HttpProxyCacheServer.Builder(context)
            .cacheDirectory(cacheDir)
            .maxCacheSize(maxCacheSizeBytes)
            .fileNameGenerator(Md5FileNameGenerator())
            .build()
    }

    /**
     * 获取内部存储缓存目录
     */
    private fun getInternalCacheDirectory(): File? {
        return try {
            appContext?.getDir(INTERNAL_CACHE_DIR_NAME, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            Log.e(TAG, "获取内部缓存目录失败", e)
            null
        }
    }

    /**
     * 获取外部存储缓存目录
     */
    private fun getExternalCacheDirectory(): File? {
        return try {
            if (isExternalStorageWritable()) {
                val externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                File(externalDir, EXTERNAL_CACHE_DIR_NAME)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取外部缓存目录失败", e)
            null
        }
    }

    /**
     * 检查URL是否为网络地址
     */
    private fun isNetworkUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    /**
     * 检查外部存储是否可写
     */
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 递归删除目录
     */
    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { child ->
                deleteRecursive(child)
            }
        }
        fileOrDirectory.delete()
    }

    /**
     * 计算目录统计信息
     */
    private fun calculateDirectoryStats(directory: File): Pair<Long, Int> {
        var size = 0L
        var count = 0

        directory.listFiles()?.forEach { file ->
            if (file.isFile) {
                size += file.length()
                count++
            } else if (file.isDirectory) {
                val subStats = calculateDirectoryStats(file)
                size += subStats.first
                count += subStats.second
            }
        }

        return Pair(size, count)
    }

    /**
     * 格式化文件大小显示
     */
    private fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.2f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.2f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }
}

/**
 * MD5 文件名生成器
 *
 * 将 URL 进行 MD5 哈希作为文件名，避免特殊字符问题
 * 实现 AndroidVideoCache 库的 FileNameGenerator 接口
 */
class Md5FileNameGenerator : CacheFileNameGenerator {

    override fun generate(url: String): String {
        // 提取扩展名
        val extension = url.substringAfterLast('.', "").takeIf { it.isNotEmpty() } ?: ""

        // 计算 MD5 哈希
        val md5Hash = calculateMd5(url)

        // 组合文件名
        return if (extension.isNotEmpty()) "$md5Hash.$extension" else md5Hash
    }

    /**
     * 计算字符串的 MD5 哈希值
     */
    private fun calculateMd5(str: String): String {
        try {
            val digest = MessageDigest.getInstance("MD5")
            digest.update(str.toByteArray())
            val messageDigest = digest.digest()

            // 转换为十六进制字符串
            val hexString = StringBuilder()
            for (byte in messageDigest) {
                // 将 byte 转换为无符号 int（0-255）
                val unsignedByte = byte.toInt() and 0xff
                val hex = Integer.toHexString(unsignedByte)
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        } catch (e: Exception) {
            // MD5 失败时使用简单的哈希作为后备方案
            return Integer.toHexString(str.hashCode())
        }
    }
}

/**
 * 缓存统计信息数据类
 *
 * @param totalSize 总缓存大小（字节）
 * @param fileCount 缓存文件数量
 * @param formattedSize 格式化后的总大小字符串
 */
data class CacheStats(
    val totalSize: Long,
    val fileCount: Int,
    val formattedSize: String = ""
)
