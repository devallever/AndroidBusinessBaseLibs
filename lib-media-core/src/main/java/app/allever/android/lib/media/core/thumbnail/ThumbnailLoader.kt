package app.allever.android.lib.media.core.thumbnail

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size
import app.allever.android.lib.core.app.App
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.SoftReference
import java.util.Collections

/**
 * 异步缩略图加载器
 *
 * 特性：
 * - API 29+ 使用 ContentResolver.loadThumbnail()（系统原生，高效）
 * - API < 29 使用 ThumbnailUtils / MediaMetadataRetriever 兼容
 * - LRU 内存缓存 + SoftReference 防止 OOM
 * - 全程协程 IO 线程，不阻塞主线程
 */
object ThumbnailLoader {

    /** 默认缩略图尺寸 */
    private val DEFAULT_SIZE = Size(512, 512)

    /** 最大缓存数量 */
    private const val MAX_CACHE_SIZE = 100

    /** 缓存：uri key → SoftReference<Bitmap> */
    private val cache =
        Collections.synchronizedMap(
            object : LinkedHashMap<String, SoftReference<Bitmap>>(MAX_CACHE_SIZE + 1, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, SoftReference<Bitmap>>?): Boolean {
                    return size > MAX_CACHE_SIZE
                }
            }
        )

    /**
     * 加载缩略图（带内存缓存）
     *
     * @param uri 媒体文件的 ContentUri
     * @param size 目标尺寸，默认 512x512
     * @return Bitmap? 加载失败返回 null
     */
    suspend fun loadThumbnail(uri: Uri?, size: Size = DEFAULT_SIZE): Bitmap? =
        withContext(Dispatchers.IO) {
            uri ?: return@withContext null

            // 1. 查缓存
            val cacheKey = "${uri}_w${size.width}h${size.height}"
            getCached(cacheKey)?.let { return@withContext it }

            // 2. 实际加载
            val bitmap = loadThumbnailInternal(uri, size)

            // 3. 写入缓存
            if (bitmap != null && !bitmap.isRecycled) {
                cache[cacheKey] = SoftReference(bitmap)
            }

            bitmap
        }

    /**
     * 批量加载缩略图（并行）
     *
     * @return Map<Uri, Bitmap> 成功的映射
     */
    suspend fun loadThumbnails(
        uris: Collection<Uri>,
        size: Size = DEFAULT_SIZE,
    ): Map<Uri, Bitmap> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<Uri, Bitmap>()
        for (uri in uris) {
            loadThumbnail(uri, size)?.let { results[uri] = it }
        }
        results
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        cache.clear()
    }

    // ==================== 内部实现 ====================

    private suspend fun loadThumbnailInternal(uri: Uri, size: Size): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    loadThumbnailApi29(uri, size)
                } else {
                    loadThumbnailLegacy(uri)
                }
            } catch (e: Exception) {
                null
            }
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadThumbnailApi29(uri: Uri, size: Size): Bitmap? {
        return try {
            App.context.contentResolver.loadThumbnail(uri, size, null)
        } catch (e: Exception) {
            // 某些情况下系统会抛异常（如文件不存在），降级到兼容方案
            loadThumbnailLegacy(uri)
        }
    }

    private fun loadThumbnailLegacy(uri: Uri): Bitmap? {
        return try {
            // 先尝试用 MediaMetadataRetriever 获取视频/音频的第一帧
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(App.context, uri)
            val frameAtTime = retriever.frameAtTime
            retriever.release()
            frameAtTime
        } catch (e: Exception) {
            try {
                // 最后尝试 BitmapFactory 解码（仅适用于图片）
                val inputStream = App.context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val opts = android.graphics.BitmapFactory.Options().apply {
                        inSampleSize = calculateInSampleSize(inputStream)
                    }
                    inputStream.close()
                    val retryStream = App.context.contentResolver.openInputStream(uri)
                    val bitmap = android.graphics.BitmapFactory.decodeStream(retryStream, null, opts)
                    retryStream?.close()
                    bitmap
                } else null
            } catch (e2: Exception) { null }
        }
    }

    /**
     * 计算采样率以控制解码后的图片大小
     */
    private fun calculateInSampleSize(inputStream: java.io.InputStream): Int {
        return try {
            val opts = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(inputStream, null, opts)
            var inSampleSize = 1
            val reqWidth = DEFAULT_SIZE.width
            val reqHeight = DEFAULT_SIZE.height
            if (opts.outHeight > reqHeight || opts.outWidth > reqWidth) {
                val halfHeight = opts.outHeight / 2
                val halfWidth = opts.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth
                ) {
                    inSampleSize *= 2
                }
            }
            inSampleSize
        } catch (e: Exception) { 4 }
    }

    private fun getCached(key: String): Bitmap? {
        return cache[key]?.get()
    }
}
