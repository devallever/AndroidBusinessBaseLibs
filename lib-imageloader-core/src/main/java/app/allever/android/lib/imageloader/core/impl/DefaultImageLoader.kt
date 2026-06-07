package app.allever.android.lib.imageloader.core.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import app.allever.android.lib.imageloader.core.cache.DiskCache
import app.allever.android.lib.imageloader.core.cache.MemoryCache
import app.allever.android.lib.imageloader.core.engine.HttpEngine
import app.allever.android.lib.imageloader.core.engine.ImageExecutor
import app.allever.android.lib.imageloader.core.engine.NetworkEngine
import app.allever.android.lib.imageloader.core.request.ImageListener
import app.allever.android.lib.imageloader.core.request.ImageLoader
import app.allever.android.lib.imageloader.core.request.ImageRequest
import app.allever.android.lib.imageloader.core.source.ImageSource
import app.allever.android.lib.imageloader.core.target.ImageTarget
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import android.util.Log

/** ImageLoader 日志 TAG */
private const val TAG = "ImageLoader"

/**
 * 内置图片加载器实现
 *
 * 完整的加载流程：
 * 1. 判断数据源类型（本地/内存/网络）
 * 2. 查内存缓存 → 命中则直接返回
 * 3. 查磁盘缓存 → 命中则解码为 Bitmap
 * 4. 加载原始数据（本地读取 / 网络下载）
 * 5. 写入磁盘缓存
 * 6. 应用 Transformation 变换链
 * 7. 写入内存缓存
 * 8. 回调主线程显示结果
 */
class DefaultImageLoader(
    private val memoryCache: MemoryCache = MemoryCache(),
    private var diskCache: DiskCache? = null,
    private val networkEngine: NetworkEngine = HttpEngine
) : ImageLoader {

    private val mainHandler = Handler(Looper.getMainLooper())
    /** 记录正在进行的请求，用于 cancel */
    private val inflightRequests = ConcurrentHashMap<String, Boolean>()

    // ==================== 核心加载入口 ====================

    override fun load(request: ImageRequest) {
        val cacheKey = request.cacheKey()
        Log.d(TAG, "load() 开始 | source=${request.source::class.simpleName} | cacheKey=$cacheKey | policy=${request.cachePolicy}")
        inflightRequests[cacheKey] = true

        // 1. 通知开始加载
        postOnMainThread { request.listener?.onStart() }

        // 2. 设置占位图
        setPlaceholder(request)

        // 3. 异步执行加载流程
        ImageExecutor.execute {
            try {
                if (!isInflight(cacheKey)) return@execute

                val bitmap = loadInternal(request, cacheKey)
                if (bitmap != null && isInflight(cacheKey)) {
                    deliverResult(request, bitmap, cacheKey)
                }
            } catch (e: Exception) {
                if (isInflight(cacheKey)) {
                    deliverError(request, e, cacheKey)
                }
            } finally {
                inflightRequests.remove(cacheKey)
            }
        }
    }

    override fun cancel(target: ImageTarget) {
        // 移除所有与该 target 相关的 in-flight 请求
        inflightRequests.keys.forEach { key ->
            // 简单策略：清除所有进行中的请求
            // 更精细的做法是记录 target→key 的映射，此处简化处理
        }
        inflightRequests.clear()
    }

    override fun clearMemoryCache() { memoryCache.clear() }

    override fun clearDiskCache() { diskCache?.clear() }

    // ==================== 内部加载逻辑 ====================

    /**
     * 内部加载流程
     */
    private fun loadInternal(request: ImageRequest, cacheKey: String): Bitmap? {
        val source = request.source
        val policy = request.cachePolicy

        // ===== Step 1: 内存缓存 =====
        if (policy != ImageRequest.CachePolicy.DISK_ONLY && policy != ImageRequest.CachePolicy.NONE) {
            memoryCache[cacheKey]?.let {
                Log.d(TAG, "内存缓存命中 | cacheKey=$cacheKey")
                return it
            }
            Log.d(TAG, "内存缓存未命中 | cacheKey=$cacheKey")
        }

        // ===== Step 2: 根据数据源类型处理 =====
        val rawBitmap: Bitmap? = when (source) {
            is ImageSource.Url -> loadFromUrl(source.url, policy, cacheKey)
            is ImageSource.ResId -> loadFromResId(source.resId, request.getContext())
            is ImageSource.Bitmap -> source.bitmap
            is ImageSource.Drawable -> drawableToBitmap(source.drawable)
            is ImageSource.File -> decodeFile(source.file)
            is ImageSource.ContentUri -> loadFromUri(source.uri, request.getContext())
            is ImageSource.Bytes -> decodeBytes(source.data)
        } ?: return null

        // ===== Step 3: 应用变换 =====
        val transformedBitmap = applyTransformations(rawBitmap!!, request.transformations)

        // ===== Step 4: 写入内存缓存 =====
        if (policy != ImageRequest.CachePolicy.DISK_ONLY && policy != ImageRequest.CachePolicy.NONE) {
            memoryCache.put(cacheKey, transformedBitmap)
        }

        return transformedBitmap
    }

    /**
     * 从 URL 加载：缓存检查 → 网络下载
     */
    private fun loadFromUrl(
        url: String,
        policy: ImageRequest.CachePolicy,
        cacheKey: String
    ): Bitmap? {
        Log.d(TAG, "loadFromUrl() | url=$url")

        // 磁盘缓存
        if (policy != ImageRequest.CachePolicy.MEMORY_ONLY && policy != ImageRequest.CachePolicy.NONE) {
            diskCache?.get(cacheKey)?.let { bytes ->
                Log.d(TAG, "磁盘缓存命中 | cacheKey=$cacheKey")
                decodeBytes(bytes)?.let { return it }
            }
            Log.d(TAG, "磁盘缓存未命中 | cacheKey=$cacheKey")
        }

        // 网络下载
        Log.d(TAG, "开始网络下载 | url=$url")
        val bytes = networkEngine.load(url)
        Log.d(TAG, "网络下载完成 | size=${bytes.size}B")

        // 写入磁盘缓存
        if (policy != ImageRequest.CachePolicy.MEMORY_ONLY && policy != ImageRequest.CachePolicy.NONE) {
            diskCache?.put(cacheKey, bytes)
        }

        return decodeBytes(bytes)
    }

    /**
     * 从资源 ID 加载
     */
    private fun loadFromResId(resId: Int, context: Context?): Bitmap? {
        context ?: return null
        return try {
            BitmapFactory.decodeResource(context.resources, resId)
        } catch (_: Exception) { null }
    }

    /**
     * 从 Content URI 加载
     */
    private fun loadFromUri(uri: android.net.Uri, context: Context?): Bitmap? {
        context ?: return null
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) { null }
    }

    /**
     * 从文件解码
     */
    private fun decodeFile(file: java.io.File): Bitmap? =
        try { BitmapFactory.decodeFile(file.absolutePath) } catch (_: Exception) { null }

    /**
     * 从字节数组解码
     */
    private fun decodeBytes(data: ByteArray): Bitmap? =
        try { BitmapFactory.decodeByteArray(data, 0, data.size) } catch (_: Exception) { null }

    /**
     * Drawable 转 Bitmap
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 应用变换链
     */
    private fun applyTransformations(
        bitmap: Bitmap,
        transformations: List<app.allever.android.lib.imageloader.core.transformation.Transformation>
    ): Bitmap {
        if (transformations.isEmpty()) return bitmap
        Log.d(TAG, "应用变换 | count=${transformations.size} | keys=${transformations.joinToString(",") { it.key() }}")
        return transformations.fold(bitmap) { current, transform ->
            Log.d(TAG, "  → 执行变换: ${transform.key()}")
            transform.transform(current)
        }
    }

    // ==================== 结果分发 ====================

    /** 分发成功结果到主线程 */
    private fun deliverResult(request: ImageRequest, bitmap: Bitmap, cacheKey: String) {
        Log.d(TAG, "加载成功 | cacheKey=$cacheKey | bitmap=${bitmap.width}x${bitmap.height}")
        postOnMainThread {
            if (!isInflight(cacheKey)) return@postOnMainThread

            when (val target = request.target) {
                is ImageTarget.ImageViewTarget -> {
                    setBitmapToView(target.view, bitmap, request.crossfadeEnabled, request.crossfadeDuration)
                }
                is ImageTarget.CallbackTarget -> {
                    target.onSuccess(bitmap)
                }
            }
            request.listener?.onSuccess(bitmap)
        }
    }

    /** 分发错误到主线程 */
    private fun deliverError(request: ImageRequest, error: Throwable, cacheKey: String) {
        Log.e(TAG, "加载失败 | cacheKey=$cacheKey | error=${error.message}", error)
        postOnMainThread {
            setErrorImage(request)
            request.listener?.onError(error)
        }
    }

    // ==================== UI 操作 ====================

    private fun setPlaceholder(request: ImageRequest) {
        postOnMainThread {
            (request.target as? ImageTarget.ImageViewTarget)?.view?.let { view ->
                request.placeholderResId?.let { resId ->
                    view.setImageResource(resId)
                    return@postOnMainThread
                }
                request.placeholderDrawable?.let { drawable ->
                    view.setImageDrawable(drawable)
                }
            }
        }
    }

    private fun setErrorImage(request: ImageRequest) {
        (request.target as? ImageTarget.ImageViewTarget)?.view?.let { view ->
            request.errorResId?.let { view.setImageResource(it) }
                ?: request.errorDrawable?.let { view.setImageDrawable(it) }
        }
    }

    private fun setBitmapToView(view: ImageView, bitmap: Bitmap, crossfade: Boolean, duration: Int) {
        if (crossfade && duration > 0) {
            view.alpha = 0f
            view.setImageBitmap(bitmap)
            view.animate().alpha(1f).setDuration(duration.toLong()).start()
        } else {
            view.setImageBitmap(bitmap)
        }
    }

    // ==================== 工具方法 ====================

    private fun isInflight(key: String): Boolean = inflightRequests.containsKey(key)

    private fun postOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    /** 尝试从请求中获取 Context */
    private fun ImageRequest.getContext(): Context? {
        return (target as? ImageTarget.ImageViewTarget)?.view?.context
    }

    // ==================== 配置方法 ====================

    /**
     * 初始化磁盘缓存（需要 Context）
     * 建议在 Application.onCreate() 中调用
     */
    fun initDiskCache(context: Context) {
        if (diskCache == null) {
            synchronized(this) {
                if (diskCache == null) {
                    diskCache = DiskCache(context)
                }
            }
        }
    }
}
