package app.allever.android.lib.imageloader.core.request

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import app.allever.android.lib.imageloader.core.source.ImageSource
import app.allever.android.lib.imageloader.core.target.ImageTarget
import app.allever.android.lib.imageloader.core.transformation.Transformation

/**
 * 图片加载请求模型
 *
 * 包含一次图片加载所需的全部参数：数据源、目标、变换、缓存策略等
 * 通过 Builder 模式构建，保证不可变性
 */
class ImageRequest private constructor(
    val source: ImageSource,
    val target: ImageTarget,
    val placeholderResId: Int?,
    val placeholderDrawable: Drawable?,
    val errorResId: Int?,
    val errorDrawable: Drawable?,
    val transformations: List<Transformation>,
    val cachePolicy: CachePolicy,
    val crossfadeEnabled: Boolean,
    val crossfadeDuration: Int,
    val listener: ImageListener?
) {

    /**
     * 缓存策略枚举
     */
    enum class CachePolicy {
        /** 不使用缓存 */
        NONE,
        /** 仅内存缓存 */
        MEMORY_ONLY,
        /** 仅磁盘缓存 */
        DISK_ONLY,
        /** 内存 + 磁盘缓存 (默认) */
        ALL
    }

    /**
     * 生成请求的唯一 Key（用于缓存命中判断）
     * Key = source.key() + transformations.key()
     */
    fun cacheKey(): String {
        val transformKey = transformations.joinToString(",") { it.key() }
        return if (transformKey.isEmpty()) source.key()
        else "${source.key()}|[$transformKey]"
    }

    // ==================== Builder ====================

    class Builder(private val source: ImageSource) {

        private var target: ImageTarget? = null
        private var placeholderResId: Int? = null
        private var placeholderDrawable: Drawable? = null
        private var errorResId: Int? = null
        private var errorDrawable: Drawable? = null
        private val transformations = mutableListOf<Transformation>()
        private var cachePolicy = CachePolicy.ALL
        private var crossfadeEnabled = true
        private var crossfadeDuration = 300
        private var listener: ImageListener? = null

        /** 设置加载目标 - ImageView */
        fun into(imageView: android.widget.ImageView): Builder = apply {
            target = ImageTarget.ImageViewTarget(imageView)
        }

        /** 设置加载目标 - 回调方式获取 Bitmap */
        fun intoCallback(onSuccess: (android.graphics.Bitmap) -> Unit, onError: ((Throwable) -> Unit)? = null): Builder = apply {
            target = ImageTarget.CallbackTarget(onSuccess, onError)
        }

        /** 占位图 - 资源 ID */
        fun placeholder(@DrawableRes resId: Int): Builder = apply { placeholderResId = resId }

        /** 占位图 - Drawable */
        fun placeholder(drawable: Drawable): Builder = apply { placeholderDrawable = drawable }

        /** 错误图 - 资源 ID */
        fun error(@DrawableRes resId: Int): Builder = apply { errorResId = resId }

        /** 错误图 - Drawable */
        fun error(drawable: Drawable): Builder = apply { errorDrawable = drawable }

        /** 添加变换效果（可多次调用叠加） */
        fun transform(vararg ts: Transformation): Builder = apply { transformations.addAll(ts) }

        /** 缓存策略 */
        fun cache(policy: CachePolicy): Builder = apply { this.cachePolicy = policy }

        /** 是否启用渐显动画 */
        fun crossfade(enabled: Boolean, durationMs: Int = 300): Builder = apply {
            crossfadeEnabled = enabled
            crossfadeDuration = durationMs.coerceAtLeast(0)
        }

        /** 加载事件监听 */
        fun listener(l: ImageListener): Builder = apply { listener = l }

        /** 构建请求对象 */
        fun build(): ImageRequest {
            checkNotNull(target) { "必须调用 into() 或 intoCallback() 指定加载目标" }
            return ImageRequest(
                source = source,
                target = target!!,
                placeholderResId = placeholderResId,
                placeholderDrawable = placeholderDrawable,
                errorResId = errorResId,
                errorDrawable = errorDrawable,
                transformations = transformations.toList(),
                cachePolicy = cachePolicy,
                crossfadeEnabled = crossfadeEnabled,
                crossfadeDuration = crossfadeDuration,
                listener = listener
            )
        }
    }
}

/** 快捷创建 ImageRequest 的扩展函数 */
fun imageRequest(source: ImageSource, block: ImageRequest.Builder.() -> Unit): ImageRequest =
    ImageRequest.Builder(source).apply(block).build()
