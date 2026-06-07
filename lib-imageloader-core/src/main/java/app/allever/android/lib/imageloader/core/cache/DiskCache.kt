package app.allever.android.lib.imageloader.core.cache

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * 磁盘缓存
 *
 * 将图片原始数据（字节数组）存储到内部存储目录，
 * 文件名为缓存 Key 的 MD5 值，避免特殊字符问题。
 *
 * @param context Android Context
 * @param maxSize 最大缓存容量（字节），默认 100MB
 */
class DiskCache(
    private val context: Context,
    private var maxSize: Long = DEFAULT_MAX_SIZE
) {

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).apply { mkdirs() }
    }

    /** 获取缓存数据 */
    fun get(key: String): ByteArray? {
        val file = getFile(key)
        return if (file.exists() && file.canRead()) {
            FileInputStream(file).use { it.readBytes() }
        } else null
    }

    /** 写入缓存数据 */
    fun put(key: String, data: ByteArray) {
        try {
            val file = getFile(key)
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { it.write(data) }
            trimIfNeeded()
        } catch (_: Exception) {
            // 写入失败静默处理
        }
    }

    /** 删除指定 key 的缓存 */
    fun remove(key: String): Boolean {
        val file = getFile(key)
        return file.exists() && file.delete()
    }

    /** 缓存是否存在 */
    fun contains(key: String): Boolean = getFile(key).exists()

    /** 清空所有缓存 */
    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    /** 当前缓存总大小（字节） */
    fun size(): Long = calculateTotalSize()

    /** 设置最大缓存容量 */
    fun setMaxSize(size: Long) { maxSize = size.coerceAtLeast(MIN_SIZE) }

    private fun getFile(key: String): File =
        File(cacheDir, CacheKey.md5(key))

    /**
     * 超过最大容量时按最后修改时间清理旧文件
     */
    private fun trimIfNeeded() {
        val totalSize = calculateTotalSize()
        if (totalSize <= maxSize) return

        val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return
        var currentSize = totalSize

        for (file in files) {
            if (currentSize <= maxSize) break
            val fileSize = file.length()
            if (file.delete()) {
                currentSize -= fileSize
            }
        }
    }

    private fun calculateTotalSize(): Long =
        cacheDir.listFiles()?.sumOf { it.length() } ?: 0L

    companion object {
        private const val CACHE_DIR_NAME = "image_loader_disk_cache"
        private const val DEFAULT_MAX_SIZE = 100L * 1024 * 1024  // 100MB
        private const val MIN_SIZE = 10L * 1024 * 1024          // 最小 10MB
    }
}
