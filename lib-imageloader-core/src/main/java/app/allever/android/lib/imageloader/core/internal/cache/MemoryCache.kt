package app.allever.android.lib.imageloader.core.internal.cache

import android.graphics.Bitmap
import android.os.Build
import android.util.LruCache
import android.util.Log

/** MemoryCache 日志 TAG */
private const val TAG = "ImageLoader-Memory"

/**
 * 内存缓存 - 基于 LRU 策略
 *
 * 使用 Bitmap 的 byte 大小作为 size 计数单位，
 * 当总大小超过 maxSize 时自动淘汰最近最少使用的条目。
 *
 * @param maxSize 最大缓存容量（字节），默认为可用内存的 1/8
 */
class MemoryCache(maxSize: Int = defaultSize()) {

    private val cache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(maxSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            // API 19+ 使用 allocationByteCount，兼容使用 byteCount
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                value.allocationByteCount
            } else {
                value.byteCount
            }
        }
    }

    /** 获取缓存的 Bitmap */
    operator fun get(key: String): Bitmap? = cache.get(key).also { result ->
        if (result != null) Log.d(TAG, "get() 命中 | key=$key")
    }

    /** 存入缓存 */
    fun put(key: String, bitmap: Bitmap): Bitmap? = cache.put(key, bitmap).also {
        val bitmapSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) bitmap.allocationByteCount else bitmap.byteCount
        Log.d(TAG, "put() 写入 | key=$key | bitmap=${bitmap.width}x${bitmap.height} | size=${bitmapSize}B | total=${size()}/${maxSize()}B")
    }

    fun remove(key: String): Bitmap? = cache.remove(key)

    /** 是否包含指定 key */
    fun containsKey(key: String): Boolean = cache.get(key) != null

    /** 清空所有缓存 */
    fun clear() { cache.evictAll() }

    /** 当前缓存大小（字节） */
    fun size(): Int = cache.size()

    /** 最大缓存容量（字节） */
    fun maxSize(): Int = cache.maxSize()

    companion object {
        /**
         * 默认内存缓存大小：可用堆内存的 1/8
         */
        fun defaultSize(): Int {
            val maxMemory = Runtime.getRuntime().maxMemory().toInt()
            return (maxMemory / 8).coerceAtLeast(4 * 1024 * 1024) // 至少 4MB
        }
    }
}
