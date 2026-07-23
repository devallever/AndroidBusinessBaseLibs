package app.allever.android.lib.imageloader.core.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.exifinterface.media.ExifInterface
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.CoroutineHelper
import app.allever.android.lib.imageloader.core.ILoader
import app.allever.android.lib.imageloader.core.ImageLoaderCore
import app.allever.android.lib.imageloader.core.internal.cache.DiskCache
import app.allever.android.lib.imageloader.core.internal.cache.MemoryCache
import app.allever.android.lib.imageloader.core.internal.engine.HttpEngine
import app.allever.android.lib.imageloader.core.internal.engine.ImageExecutor
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream

/**
 * 内置图片加载引擎实现
 *
 * 零第三方依赖，基于以下组件构建：
 * - HttpURLConnection（HttpEngine）网络下载
 * - LRU Cache（MemoryCache）内存缓存
 * - DiskLruCache（DiskCache）磁盘缓存
 *
 * 支持的数据源：String(URL/文件路径), Int(ResId), Uri, File, Bitmap, Drawable
 */
@SuppressLint("StaticFieldLeak")
@Deprecated("使用CoilLoader")
object DefaultLoader : ILoader {

    private val TAG = "ImageLoader-DefaultLoader"

    /** 大图降采样阈值（超过此尺寸自动采样） */
    private val MAX_BITMAP_SIZE = 2048

    private var mContext: Context? = null
    private val mMemoryCache by lazy { MemoryCache() }
    private var mDiskCache: DiskCache? = null

    override fun init(context: Context) {
        mContext = context.applicationContext
        mDiskCache = DiskCache().also { it.init(context) }
        log(TAG, "初始化完成")
    }

    // ==================== 基础加载 ====================

    override fun load(resource: Any, imageView: ImageView, errorResId: Int?, placeholder: Int?) {
        if (!ImageLoaderCore.checkCanLoad(imageView)) return

        placeholder?.let { imageView.setImageResource(it) }

        val cacheKey = generateCacheKey(resource)

        // 1. 内存缓存命中
        val cached = mMemoryCache[cacheKey]
        if (cached != null && !cached.isRecycled) {
            log(TAG, "内存缓存命中 | key=$cacheKey | ${cached.width}x${cached.height}")
            setBitmap(imageView, cached)
            return
        }

        log(TAG, "内存缓存未命中 | key=$cacheKey")

        // 2. 异步加载
        ImageExecutor.execute {
            try {
                val bitmap = decodeResource(resource, cacheKey) ?: return@execute
                postToMain { setBitmap(imageView, bitmap) }
            } catch (e: Exception) {
                log(TAG, "加载失败 | key=$cacheKey | error=${e.message}")
                errorResId?.let { resId ->
                    postToMain { imageView.setImageResource(resId) }
                }
            }
        }
    }

    // ==================== 圆角加载 ====================

    override fun loadRound(
        resource: Any,
        imageView: ImageView,
        radiusDp: Float?,
        errorResId: Int?,
        placeholder: Int?
    ) {
        if (!ImageLoaderCore.checkCanLoad(imageView)) return
        placeholder?.let { imageView.setImageResource(it) }

        val cacheKey = generateCacheKey(resource) + "_round_${radiusDp ?: 0}"

        ImageExecutor.execute {
            try {
                val raw = decodeResource(resource, cacheKey) ?: return@execute
                val radiusPx = dpToPx(radiusDp ?: 10f)
                val result = applyRoundedCorners(raw, radiusPx)
                postToMain { setBitmap(imageView, result) }
            } catch (e: Exception) {
                errorResId?.let { postToMain { imageView.setImageResource(it) } }
            }
        }
    }

    // ==================== 圆形加载 ====================

    override fun loadCircle(
        resource: Any,
        imageView: ImageView,
        borderWidthDp: Int?,
        borderColor: Int?,
        errorResId: Int?,
        placeholder: Int?
    ) {
        if (!ImageLoaderCore.checkCanLoad(imageView)) return
        placeholder?.let { imageView.setImageResource(it) }

        val cacheKey = generateCacheKey(resource) + "_circle"

        ImageExecutor.execute {
            try {
                val raw = decodeResource(resource, cacheKey) ?: return@execute
                val borderPx = dpToPx((borderWidthDp ?: 0).toFloat()).toInt()
                val result = applyCircleCrop(raw, borderPx, borderColor)
                postToMain { setBitmap(imageView, result) }
            } catch (e: Exception) {
                errorResId?.let { postToMain { imageView.setImageResource(it) } }
            }
        }
    }

    // ==================== 模糊加载 ====================

    override fun loadBlur(resource: Any, imageView: ImageView, radius: Float?) {
        if (!ImageLoaderCore.checkCanLoad(imageView)) return

        val cacheKey = generateCacheKey(resource) + "_blur_${radius ?: 0}"

        ImageExecutor.execute {
            try {
                val raw = decodeResource(resource, cacheKey) ?: return@execute
                val result = applyBlur(imageView.context, raw, (radius ?: 10f).toInt())
                postToMain { setBitmap(imageView, result) }
            } catch (e: Exception) {
                log(TAG, "模糊加载失败 | error=${e.message}")
            }
        }
    }

    // ==================== GIF 加载 ====================

    override fun loadGif(resource: Any, imageView: ImageView) {
        // 内置实现不支持动画 GIF，降级为静态图加载
        load(resource, imageView, null, null)
    }

    // ==================== 下载 ====================

    override fun download(url: String, block: ((Boolean, File?) -> Unit)) {
        ImageExecutor.execute {
            try {
                val bytes = HttpEngine.load(url)
                val file = File(mContext?.cacheDir, "download_${System.currentTimeMillis()}.tmp")
                file.writeBytes(bytes)
                log(TAG, "下载成功 | url=$url | size=${bytes.size}B")
                postToMain { block(true, file) }
            } catch (e: Exception) {
                log(TAG, "下载失败 | url=$url | error=${e.message}")
                postToMain { block(false, null) }
            }
        }
    }

    // ==================== 核心解码逻辑 ====================

    /**
     * 解码资源为 Bitmap（带缓存）
     */
    private fun decodeResource(resource: Any, cacheKey: String): Bitmap? {
        // 1. 内存缓存
        mMemoryCache[cacheKey]?.let { return it }

        // 2. 磁盘缓存
        val bytes = when (resource) {
            is String -> {
                if (resource.startsWith("http")) {
                    mDiskCache?.get(cacheKey) ?: HttpEngine.load(resource).also { data ->
                        mDiskCache?.put(cacheKey, data)
                    }
                } else {
                    // 本地文件路径
                    val file = File(resource)
                    if (file.exists()) FileInputStream(file).use { it.readBytes() } else null
                }
            }
            is Uri -> {
                mDiskCache?.get(cacheKey) ?: mContext?.contentResolver?.openInputStream(resource)?.use {
                    it.readBytes()
                }?.also { data -> mDiskCache?.put(cacheKey, data) }
            }
            is File -> {
                mDiskCache?.get(cacheKey) ?: FileInputStream(resource).use { it.readBytes() }.also { data ->
                    mDiskCache?.put(cacheKey, data)
                }
            }
            is Bitmap -> return resource
            is Drawable -> return drawableToBitmap(resource)
            is Int -> return decodeResId(resource)
            else -> null
        } ?: return null

        // 3. 字节数组解码（带采样）
        val bitmap = decodeSampledBitmapFromBytes(bytes)

        // 4. EXIF 修正
        val corrected = correctExifOrientation(bitmap, bytes, resource)

        // 5. 写入内存缓存
        mMemoryCache.put(cacheKey, corrected)

        return corrected
    }

    /**
     * 从字节数组解码 Bitmap（大图自动采样）
     */
    private fun decodeSampledBitmapFromBytes(data: ByteArray): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        val sampleSize = calculateInSampleSize(options.outWidth, options.outHeight, MAX_BITMAP_SIZE, MAX_BITMAP_SIZE)
        if (sampleSize > 1) {
            log(
                TAG,
                "大图降采样 | ${options.outWidth}x${options.outHeight} → inSampleSize=$sampleSize"
            )
        }

        return BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }.let { opts -> BitmapFactory.decodeByteArray(data, 0, data.size, opts)!! }
    }

    /**
     * 从 ResId 解码 Bitmap
     */
    private fun decodeResId(@DrawableRes resId: Int): Bitmap? {
        val ctx = mContext ?: return null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(ctx.resources, resId, options)

        val sampleSize = calculateInSampleSize(options.outWidth, options.outHeight, MAX_BITMAP_SIZE, MAX_BITMAP_SIZE)
        return BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }.let { opts -> BitmapFactory.decodeResource(ctx.resources, resId, opts) }
    }

    /**
     * 计算 inSampleSize（确保解码后尺寸 ≤ maxWidth/maxHeight）
     */
    private fun calculateInSampleSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Int {
        var sampleSize = 1
        while (width / sampleSize > maxWidth || height / sampleSize > maxHeight) {
            sampleSize *= 2
        }
        return sampleSize
    }

    /**
     * EXIF 方向修正
     */
    private fun correctExifOrientation(bitmap: Bitmap?, bytes: ByteArray, resource: Any): Bitmap {
        if (bitmap == null) return bitmap!!

        try {
            val exif = when {
                bytes.isNotEmpty() -> ExifInterface(ByteArrayInputStream(bytes))
                resource is File -> ExifInterface(resource)
                resource is Uri -> {
                    val stream = mContext?.contentResolver?.openInputStream(resource)
                    if (stream != null) ExifInterface(stream).also { stream.close() } else return bitmap
                }
                else -> return bitmap
            }

            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
            if (orientation == 1) return bitmap

            log(TAG, "EXIF 修正 | orientation=$orientation | 原始=${bitmap.width}x${bitmap.height}")

            val matrix = Matrix()
            when (orientation) {
                2 -> matrix.setScale(-1f, 1f)
                3 -> matrix.postRotate(180f)
                4 -> matrix.setScale(1f, -1f)
                5 -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
                6 -> matrix.postRotate(90f)
                7 -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
                8 -> matrix.postRotate(270f)
            }

            val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (result != bitmap && !bitmap.isRecycled) bitmap.recycle()
            return result
        } catch (_: Exception) {
            return bitmap
        }
    }

    // ==================== 变换效果 ====================

    /**
     * 圆角裁切（BitmapShader 方式，兼容所有设备）
     */
    private fun applyRoundedCorners(source: Bitmap, radiusPx: Float): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val paint = Paint().apply {
            isAntiAlias = true
            this.shader = shader
        }

        Canvas(output).drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radiusPx, radiusPx, paint)

        if (output != source && !source.isRecycled) source.recycle()
        log(TAG, "圆角完成 | ${width}x${height} | radius=${radiusPx}px")
        return output
    }

    /**
     * 圆形裁切（BitmapShader 方式）
     */
    private fun applyCircleCrop(source: Bitmap, borderWidthPx: Int, borderColor: Int?): Bitmap {

        val size = minOf(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }

        // 计算居中裁切区域
        val left = (source.width - size) / 2f
        val top = (source.height - size) / 2f

        // 绘制圆形
        val path = Path().apply {
            addCircle(size / 2f, size / 2f, size / 2f, Path.Direction.CW)
        }
        canvas.drawPath(path, paint)

        // 裁切原图到圆形区域
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, -left, -top, paint)

        val cx = size / 2f
        val cy = size / 2f
        val radius = size / 2f - borderWidthPx / 2f

        // 绘制边框 //todo 可能效果不尽人意
        if (borderWidthPx > 0 && borderColor != null) {
            canvas.drawCircle(cx, cy, radius + borderWidthPx / 2f, Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = borderWidthPx.toFloat()
                color = borderColor
            })
        }

        if (source != output && !source.isRecycled) {
            source.recycle()
        }

        return output
    }

    /**
     * 高斯模糊（Stack Blur 算法，无 RenderScript 依赖）
     */
    private fun applyBlur(context: Context, source: Bitmap, radius: Int): Bitmap {
        return fallbackBlur(source, radius)
    }

    // ==================== 工具方法 ====================

    private fun generateCacheKey(resource: Any): String = when (resource) {
        is String -> if (resource.startsWith("http")) "url:$resource" else "file:$resource"
        is Int -> "res:$resource"
        is Uri -> "uri:$resource"
        is File -> "file:${resource.absolutePath}"
        is Bitmap -> "bitmap:${resource.hashCode()}"
        is Drawable -> "drawable:${resource.hashCode()}"
        else -> "other:${resource.hashCode()}"
    }

    private fun setBitmap(view: ImageView, bitmap: Bitmap?) {
        if (ImageLoaderCore.checkCanLoad(view) && bitmap != null && !bitmap.isRecycled) {
            view.setImageBitmap(bitmap)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(Canvas(bitmap))
        return bitmap
    }

    private fun dpToPx(dp: Float): Float {
        val density = App.Companion.context.resources.displayMetrics.density
        return dp * density
    }

    private fun postToMain(action: () -> Unit) {
        CoroutineHelper.MAIN.launch { action() }
    }

    private fun blurWithRenderScript(context: Context, source: Bitmap, radius: Int): Bitmap {
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(rs, source)
        val output = Allocation.createTyped(rs, input.type)

        ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).apply {
            setInput(input)
            setRadius(radius.toFloat().coerceIn(1f, 25f))
            forEach(output)
        }

        val blurred = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        output.copyTo(blurred)

        input.destroy()
        output.destroy()
        rs.destroy()

        if (source != blurred && !source.isRecycled) {
            source.recycle()
        }
        return blurred
    }

    /** 降级模糊：使用 Stack Blur 算法（纯 Java 实现） */
    private fun fallbackBlur(source: Bitmap, radius: Int): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        stackBlur(pixels, width, height, radius)

        output.setPixels(pixels, 0, width, 0, 0, width, height)

        if (source != output && !source.isRecycled) {
            source.recycle()
        }
        return output
    }

    /** Stack Blur 算法实现 */
    private fun stackBlur(pixels: IntArray, w: Int, h: Int, r: Int) {
        if (r < 1) return
        val wm = w - 1
        val hm = h - 1
        val div = r + r + 1
        val rSum = IntArray(w * h)
        val gSum = IntArray(w * h)
        val bSum = IntArray(w * h)
        val aSum = IntArray(w * h)
        val vmin = IntArray(maxOf(w, h))
        val vmax = IntArray(maxOf(w, h))
        val divSum = IntArray((div shl 3) + 3)
        for (i in divSum.indices) {
            divSum[i] = i / div
        }

        // 函数级变量，两个循环共享
        var sum = 0
        var rsum = 0
        var gsum = 0
        var bsum = 0
        var asum = 0

        // 水平方向模糊
        var y = 0
        while (y < h) {
            sum = 0; rsum = 0; gsum = 0; bsum = 0; asum = 0
            for (i in -r..r) {
                val p = pixels[clamp(y, 0, hm) * w + clamp(i, 0, wm)]
                rsum += (p shr 16) and 0xff
                gsum += (p shr 8) and 0xff
                bsum += p and 0xff
                asum += p ushr 24
            }
            var x = 0
            while (x < w) {
                aSum[y * w + x] = asum / div
                rSum[y * w + x] = rsum / div
                gSum[y * w + x] = gsum / div
                bSum[y * w + x] = bsum / div

                val yi = clamp(x - r, 0, wm).also { vmax[x] = clamp(x + r + 1, 0, wm) }
                val yw = yi + y * w
                sum -= pixels[yw].ushr(24)
                rsum -= (pixels[yw] shr 16) and 0xff
                gsum -= (pixels[yw] shr 8) and 0xff
                bsum -= pixels[yw] and 0xff
                val px = clamp(x + r + 1, 0, wm)
                val py = px + y * w
                sum += pixels[py].ushr(24)
                rsum += (pixels[py] shr 16) and 0xff
                gsum += (pixels[py] shr 8) and 0xff
                bsum += pixels[py] and 0xff
                asum += sum
                x++
            }
            y++
        }

        // 垂直方向模糊
        var x2 = 0
        while (x2 < w) {
            sum = 0; rsum = 0; gsum = 0; bsum = 0; asum = 0
            for (i in -r..r) {
                val p = pixels[clamp(i, 0, hm) * w + clamp(x2, 0, wm)]
                rsum += (p shr 16) and 0xff
                gsum += (p shr 8) and 0xff
                bsum += p and 0xff
                asum += p ushr 24
            }
            y = 0
            while (y < h) {
                pixels[y * w + x2] = ((aSum[y * w + x2] shl 24) or
                        (rSum[y * w + x2] shl 16) or
                        (gSum[y * w + x2] shl 8) or
                        bSum[y * w + x2])
                val xi = clamp(y - r, 0, hm).also { vmax[x2] = clamp(y + r + 1, 0, hm) }
                val xw = x2 + xi * w
                sum -= pixels[xw].ushr(24)
                rsum -= (pixels[xw] shr 16) and 0xff
                gsum -= (pixels[xw] shr 8) and 0xff
                bsum -= pixels[xw] and 0xff
                val py = clamp(y + r + 1, 0, hm)
                val pw = x2 + py * w
                sum += pixels[pw].ushr(24)
                rsum += (pixels[pw] shr 16) and 0xff
                gsum += (pixels[pw] shr 8) and 0xff
                bsum += pixels[pw] and 0xff
                asum += sum
                y++
            }
            x2++
        }
    }

    private fun clamp(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)
}