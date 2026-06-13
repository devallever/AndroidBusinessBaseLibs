package app.allever.android.lib.imageloader.core.internal.cache

import android.content.Context
import android.util.Log
import app.allever.android.lib.core.util.DiskLruCache
import java.io.File
import java.io.IOException

/** DiskCache 日志 TAG */
private const val TAG = "ImageLoader-Disk"

/**
 * 磁盘缓存（基于 DiskLruCache）
 *
 * 使用项目内置的 DiskLruCache 实现，支持：
 * - LRU 淘汰策略（按容量自动清理）
 * - Journal 日志保证数据一致性
 * - 原子性写入（commit/abort）
 */
class DiskCache(
    private var maxSize: Long = DEFAULT_MAX_SIZE
) {

    private var diskLruCache: DiskLruCache? = null

    /** 初始化磁盘缓存（必须在 get/put 之前调用） */
    fun init(context: Context): Boolean {
        return try {
            if (diskLruCache != null && !diskLruCache!!.isClosed) {
                Log.d(TAG, "已初始化，跳过")
                return true
            }
            val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
            // valueCount=1: 每个 key 只存一个值（图片字节数组）
            diskLruCache = DiskLruCache.open(cacheDir, APP_VERSION, VALUE_COUNT, maxSize, true)
            Log.d(TAG, "初始化完成 | dir=$cacheDir | maxSize=${maxSize / 1024 / 1024}MB")
            true
        } catch (_: Exception) {
            Log.e(TAG, "初始化失败")
            false
        }
    }

    /** 是否已初始化 */
    val isInitialized: Boolean get() = diskLruCache != null && !diskLruCache!!.isClosed

    /**
     * 获取缓存数据
     * @return 缓存的原始字节数组，未命中返回 null
     */
    fun get(key: String): ByteArray? {
        val cache = diskLruCache ?: run { Log.w(TAG, "get() 未初始化"); return null }
        val safeKey = CacheKey.md5(key)

        return try {
            val snapshot = cache.get(safeKey)
            if (snapshot != null) {
                snapshot.getInputStream(0).use { stream ->
                    stream.readBytes().also {
                        Log.d(TAG, "get() 命中 | key=$key | size=${it.size}B")
                    }
                }.also { snapshot.close() }
            } else {
                Log.d(TAG, "get() 未命中 | key=$key")
                null
            }
        } catch (_: IOException) {
            Log.e(TAG, "get() 异常 | key=$key")
            null
        }
    }

    /**
     * 写入缓存数据
     * @param data 图片原始字节数组
     */
    fun put(key: String, data: ByteArray) {
        val cache = diskLruCache ?: run { Log.w(TAG, "put() 未初始化"); return }
        val safeKey = CacheKey.md5(key)

        try {
            val editor = cache.edit(safeKey)
            if (editor == null) {
                Log.e(TAG, "put() 编辑冲突 | key=$key（另一线程正在编辑）")
                return
            }

            editor.newOutputStream(0).use { output ->
                output.write(data)
            }
            editor.commit()
            Log.d(TAG, "put() 写入 | key=$key | size=${data.size}B")
        } catch (_: IOException) {
            Log.e(TAG, "put() 写入失败 | key=$key")
            try { cache.edit(safeKey)?.abort() } catch (_: Exception) {}
        }
    }

    /** 删除指定 key 的缓存 */
    fun remove(key: String): Boolean {
        val cache = diskLruCache ?: return false
        val safeKey = CacheKey.md5(key)

        return try {
            cache.remove(safeKey).also {
                if (it) Log.d(TAG, "remove() 成功 | key=$key")
            }
        } catch (_: IOException) {
            false
        }
    }

    /** 缓存是否存在 */
    fun contains(key: String): Boolean {
        val cache = diskLruCache ?: return false
        return try {
            cache.get(CacheKey.md5(key))?.let { it.close(); true } ?: false
        } catch (_: IOException) { false }
    }

    /** 清空所有缓存 */
    fun clear() {
        val cache = diskLruCache ?: return
        try {
            cache.delete()
            Log.d(TAG, "clear() 已清空")
        } catch (_: IOException) {}
    }

    /** 刷新日志到磁盘 */
    fun flush() {
        try { diskLruCache?.flush() } catch (_: IOException) {}
    }

    /** 关闭缓存 */
    fun close() {
        try { diskLruCache?.close() } catch (_: IOException) {}
        diskLruCache = null
    }

    companion object {
        private const val CACHE_DIR_NAME = "image_loader_disk_cache"
        private const val APP_VERSION = 1
        private const val VALUE_COUNT = 1  // 每个缓存条目只存一个值
        private const val DEFAULT_MAX_SIZE = 100L * 1024 * 1024  // 100MB
    }
}
