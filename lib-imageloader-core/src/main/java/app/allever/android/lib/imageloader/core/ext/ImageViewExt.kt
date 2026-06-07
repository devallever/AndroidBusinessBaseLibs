package app.allever.android.lib.imageloader.core.ext

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import app.allever.android.lib.imageloader.core.request.ImageListener
import app.allever.android.lib.imageloader.core.request.ImageLoader
import app.allever.android.lib.imageloader.core.request.ImageRequest
import app.allever.android.lib.imageloader.core.source.ImageSource
import app.allever.android.lib.imageloader.core.transformation.BlurTransformation
import app.allever.android.lib.imageloader.core.transformation.CircleTransformation
import app.allever.android.lib.imageloader.core.transformation.GrayscaleTransformation
import app.allever.android.lib.imageloader.core.transformation.RoundedCorners
import app.allever.android.lib.imageloader.core.transformation.Transformation
import android.util.Log

private const val TAG = "ImageLoader"

/**
 * 图片加载 DSL 选项
 *
 * 提供简洁的链式 API 配置加载参数，
 * 内部构建为 ImageRequest 后交给 ImageLoader 执行。
 */
class ImageOptions {
    @DrawableRes var placeholderResId: Int? = null
    var placeholderDrawable: Drawable? = null
    @DrawableRes var errorResId: Int? = null
    var errorDrawable: Drawable? = null
    var cachePolicy: ImageRequest.CachePolicy = ImageRequest.CachePolicy.ALL
    var crossfadeEnabled: Boolean = true
    var crossfadeDuration: Int = 300
    private val transformations = mutableListOf<Transformation>()
    var listener: ImageListener? = null

    /** 占位图（资源 ID） */
    fun placeholder(@DrawableRes resId: Int) {
        placeholderResId = resId
    }

    /** 占位图（Drawable） */
    fun placeholder(drawable: Drawable) {
        placeholderDrawable = drawable
    }

    /** 错误图（资源 ID） */
    fun error(@DrawableRes resId: Int) {
        errorResId = resId
    }

    /** 错误图（Drawable） */
    fun error(drawable: Drawable) {
        errorDrawable = drawable
    }

    /** 圆角（单位：像素） */
    fun roundedCorners(radius: Float) {
        transformations.add(RoundedCorners(radius))
    }

    /** 圆形裁切 */
    fun circle() {
        transformations.add(CircleTransformation)
    }

    /** 高斯模糊 (1-25) */
    fun blur(radius: Int) {
        transformations.add(BlurTransformation(radius))
    }

    /** 灰度化 */
    fun grayscale() {
        transformations.add(GrayscaleTransformation)
    }

    /** 添加自定义变换 */
    fun transform(transformation: Transformation) {
        transformations.add(transformation)
    }

    internal fun buildTransforms(): List<Transformation> = transformations.toList()
}

/**
 * ImageView 扩展 - 加载图片
 *
 * 最简用法：
 * ```
 * imageView.load("https://example.com/photo.jpg")
 * ```
 *
 * 带参数：
 * ```
 * imageView.load(R.drawable.icon) {
 *     placeholder(R.drawable.loading)
 *     roundedCorners(16f)
 * }
 * ```
 *
 * @param source 数据源：String(Url), Int(ResId), Bitmap, Drawable, File, Uri, ByteArray
 * @param block 配置选项 DSL，可为 null 使用默认配置
 */
fun ImageView.load(source: Any?, block: (ImageOptions.() -> Unit)? = null) {
    if (source == null) return

    Log.d(TAG, "load() DSL 入口 | sourceType=${source::class.simpleName} | view=${this::class.simpleName}")

    val imageSource = ImageSource.from(source)
    if (imageSource == null) {
        Log.w(TAG, "不支持的数据源类型 | type=${source::class.simpleName}")
        return
    }

    val options = ImageOptions().apply { block?.invoke(this) }

    val request = ImageRequest.Builder(imageSource)
        .into(this)
        .apply {
            options.placeholderResId?.let { placeholder(it) }
            options.placeholderDrawable?.let { placeholder(it) }
            options.errorResId?.let { error(it) }
            options.errorDrawable?.let { error(it) }
            cache(options.cachePolicy)
            crossfade(options.crossfadeEnabled, options.crossfadeDuration)
            options.buildTransforms().forEach { transform(it) }
            options.listener?.let { listener(it) }
        }
        .build()

    ImageLoader.getInstance().load(request)
}
