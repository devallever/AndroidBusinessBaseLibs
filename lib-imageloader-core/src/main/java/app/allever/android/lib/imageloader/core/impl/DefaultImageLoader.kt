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
import android.graphics.Matrix
import android.media.ExifInterface
import java.util.concurrent.ConcurrentHashMap
import android.util.Log

/** ImageLoader 日志 TAG */
private const val TAG = "ImageLoader"

/**
 * 进行中的请求条目，支持请求合并（coalesce）
 *
 * 首个请求触发实际加载，后续相同 cacheKey 的请求加入等待列表，
 * 加载完成后统一广播结果。
 */
private data class InflightEntry(
    /** 等待该 key 加载结果的请求列表（含首发请求） */
    val requests: MutableList<ImageRequest> = mutableListOf(),
    /** 是否已完成加载 */
    @Volatile var completed: Boolean = false
)

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
    /** 进行中的请求（支持合并）：cacheKey → 请求条目 */
    private val inflightRequests = ConcurrentHashMap<String, InflightEntry>()

    // ==================== 核心加载入口 ====================

    override fun load(request: ImageRequest) {
        val cacheKey = request.cacheKey()
        Log.d(TAG, "load() 开始 | source=${request.source::class.simpleName} | cacheKey=$cacheKey | policy=${request.cachePolicy}")

        // ===== 请求合并：检查是否已有相同 key 的加载任务 =====
        while (true) {
            val existing = inflightRequests[cacheKey]
            if (existing != null && !existing.completed) {
                // 已有相同 key 正在加载 → 合并到等待列表
                Log.d(TAG, "请求合并 | cacheKey=$cacheKey | 等待者+1 (当前共 ${existing.requests.size + 1})")
                synchronized(existing) {
                    if (!existing.completed) { // double-check
                        existing.requests.add(request)
                        setPlaceholder(request)
                        postOnMainThread { request.listener?.onStart() }
                        return
                    }
                }
                // existing 在等待期间已完成，退出循环重新发起
                break
            } else {
                break
            }
        }

        // 首发请求：创建 InflightEntry 并触发加载
        val entry = InflightEntry(mutableListOf(request))
        val prev = inflightRequests.putIfAbsent(cacheKey, entry)
        if (prev != null && !prev.completed) {
            // 极端竞态：putIfAbsent 时被别的线程抢先了，重试合并逻辑
            synchronized(prev) {
                if (!prev.completed) {
                    prev.requests.add(request)
                    setPlaceholder(request)
                    postOnMainThread { request.listener?.onStart() }
                    return
                }
            }
        }

        // 1. 通知开始加载
        postOnMainThread { request.listener?.onStart() }

        // 2. 设置占位图
        setPlaceholder(request)

        // 3. 异步执行加载流程（仅首发请求执行一次）
        ImageExecutor.execute {
            try {
                val bitmap = loadInternal(request, cacheKey)
                if (bitmap != null) {
                    broadcastSuccess(entry, bitmap, cacheKey)
                } else {
                    broadcastError(entry, IllegalStateException("加载结果为空"), cacheKey)
                }
            } catch (e: Exception) {
                broadcastError(entry, e, cacheKey)
            } finally {
                entry.completed = true
                inflightRequests.remove(cacheKey)
            }
        }
    }

    override fun cancel(target: ImageTarget) {
        inflightRequests.values.forEach { entry ->
            synchronized(entry) {
                if (!entry.completed) {
                    entry.requests.removeAll { req ->
                        (req.target as? ImageTarget.ImageViewTarget)?.view == (target as? ImageTarget.ImageViewTarget)?.view
                    }
                }
            }
        }
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
            is ImageSource.Url -> loadFromUrl(source.url, policy, cacheKey, request)
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
        cacheKey: String,
        request: ImageRequest
    ): Bitmap? {
        Log.d(TAG, "loadFromUrl() | url=$url")

        // 懒初始化磁盘缓存（从请求中提取 Context）
        ensureDiskCache(request.getContext())

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
                val bitmap = BitmapFactory.decodeStream(stream)
                correctExifOrientation(bitmap, uri, context)
            }
        } catch (_: Exception) { null }
    }

    /**
     * 从文件解码
     */
    private fun decodeFile(file: java.io.File): Bitmap? =
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            correctExifOrientation(bitmap, file)
        } catch (_: Exception) { null }

    /**
     * 从字节数组解码
     */
    private fun decodeBytes(data: ByteArray): Bitmap? =
        try { BitmapFactory.decodeByteArray(data, 0, data.size) } catch (_: Exception) { null }

    // ==================== EXIF 方向修正 ====================

    /** 根据 EXIF 信息修正图片旋转（相机拍摄照片常见问题） */
    private fun correctExifOrientation(bitmap: Bitmap?, file: java.io.File): Bitmap? {
        if (bitmap == null) return null
        val exif = try { ExifInterface(file.absolutePath) } catch (_: Exception) { null }
            ?: return bitmap
        return applyExifRotation(bitmap, exif)
    }

    private fun correctExifOrientation(bitmap: Bitmap?, uri: android.net.Uri, context: Context): Bitmap? {
        if (bitmap == null) return null
        val exif = try { ExifInterface(context.contentResolver.openInputStream(uri)!!) } catch (_: Exception) { null }
            ?: return bitmap
        return applyExifRotation(bitmap, exif)
    }

    /**
     * 根据 EXIF orientation 标签旋转 Bitmap
     *
     * EXIF Orientation 值含义：
     *   1 = 正常 (0°)
     *   3 = 旋转180°
     *   6 = 顺时针90°
     *   8 = 逆时针90°
     */
    private fun applyExifRotation(bitmap: Bitmap, exif: ExifInterface): Bitmap {
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        if (orientation == ExifInterface.ORIENTATION_NORMAL) return bitmap

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(-90f); matrix.postScale(-1f, 1f) }
            else -> return bitmap
        }
        Log.d(TAG, "EXIF 修正 | orientation=$orientation | 原始=${bitmap.width}x${bitmap.height}")
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (result != bitmap) bitmap.recycle()
        return result
    }

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

    // ==================== 结果广播 ====================

    /** 广播成功结果到所有等待该 key 的请求 */
    private fun broadcastSuccess(entry: InflightEntry, bitmap: Bitmap, cacheKey: String) {
        Log.d(TAG, "加载成功 | cacheKey=$cacheKey | bitmap=${bitmap.width}x${bitmap.height} | 广播给 ${entry.requests.size} 个请求")
        val requests = synchronized(entry) { entry.requests.toList() }
        for (req in requests) {
            deliverResult(req, bitmap, cacheKey)
        }
    }

    /** 广播错误到所有等待该 key 的请求 */
    private fun broadcastError(entry: InflightEntry, error: Throwable, cacheKey: String) {
        Log.e(TAG, "加载失败 | cacheKey=$cacheKey | error=${error.message} | 广播给 ${entry.requests.size} 个请求", error)
        val requests = synchronized(entry) { entry.requests.toList() }
        for (req in requests) {
            deliverError(req, error, cacheKey)
        }
    }

    /** 分发单个请求的成功结果到主线程 */
    private fun deliverResult(request: ImageRequest, bitmap: Bitmap, cacheKey: String) {
        postOnMainThread {
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

    /** 分发单个请求的错误到主线程 */
    private fun deliverError(request: ImageRequest, error: Throwable, cacheKey: String) {
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
     * 确保磁盘缓存已初始化（懒加载）
     * 首次加载 URL 图片时自动调用，无需用户手动 init
     */
    private fun ensureDiskCache(context: Context?) {
        if (diskCache == null && context != null) {
            synchronized(this) {
                if (diskCache == null) {
                    Log.d(TAG, "自动初始化磁盘缓存")
                    diskCache = DiskCache(context)
                }
            }
        }
    }

    /**
     * 手动初始化磁盘缓存（需要 Context）
     * 建议在 Application.onCreate() 中调用（可选，不调用也会自动初始化）
     */
    fun initDiskCache(context: Context) {
        ensureDiskCache(context)
    }
}
