package app.allever.android.lib.imageloader.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import app.allever.android.lib.imageloader.core.request.ImageListener
import app.allever.android.lib.imageloader.core.request.ImageLoader
import app.allever.android.lib.imageloader.core.request.ImageRequest
import app.allever.android.lib.imageloader.core.source.ImageSource
import app.allever.android.lib.imageloader.core.target.ImageTarget
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation as GlideTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners as GlideRoundedCorners
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Glide 适配器
 *
 * 将 ImageLoader 接口委托给 Glide 实现。
 * 使用方式：
 * ```
 * ImageLoaderConfig.init(context) {
 *     loader = GlideImageLoader()
 * }
 * ```
 *
 * 之后所有 .load() 调用自动走 Glide，业务代码无需任何改动。
 */
class GlideImageLoader : ImageLoader {

    override fun load(request: ImageRequest) {
        when (val target = request.target) {
            is ImageTarget.ImageViewTarget -> loadIntoImageView(request, target.view)
            is ImageTarget.CallbackTarget -> loadIntoCallback(request, target)
        }
    }

    override fun cancel(target: ImageTarget) {
        (target as? ImageTarget.ImageViewTarget)?.view?.let { view ->
            Glide.with(view.context).clear(view)
        }
    }

    override fun clearMemoryCache() {
        // Glide 需要在主线程调用: Glide.get(context).clearMemory()
    }

    override fun clearDiskCache() {
        // Glide 需要在后台线程调用: Glide.get(context).clearDiskCache()
    }

    // ==================== 内部实现 ====================

    private fun loadIntoImageView(request: ImageRequest, imageView: ImageView) {
        val context = imageView.context
        val source = request.source

        @Suppress("UNCHECKED_CAST")
        val builder = when (source) {
            is ImageSource.Url -> Glide.with(context).load(source.url)
            is ImageSource.ResId -> Glide.with(context).load(source.resId)
            is ImageSource.Bitmap -> Glide.with(context).load(source.bitmap)
            is ImageSource.Drawable -> Glide.with(context).load(source.drawable)
            is ImageSource.File -> Glide.with(context).load(source.file)
            is ImageSource.ContentUri -> Glide.with(context).load(source.uri)
            is ImageSource.Bytes -> Glide.with(context).load(source.data)
        }

        builder.apply {
            // 占位图
            request.placeholderResId?.let { placeholder(it) }
            request.placeholderDrawable?.let { placeholder(it) }

            // 错误图
            request.errorResId?.let { error(it) }
            request.errorDrawable?.let { error(it) }

            // 缓存策略映射
            when (request.cachePolicy) {
                ImageRequest.CachePolicy.NONE -> diskCacheStrategy(DiskCacheStrategy.NONE)
                ImageRequest.CachePolicy.MEMORY_ONLY -> diskCacheStrategy(DiskCacheStrategy.NONE)
                ImageRequest.CachePolicy.DISK_ONLY -> diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(true)
                ImageRequest.CachePolicy.ALL -> diskCacheStrategy(DiskCacheStrategy.ALL)
            }

            // 变换效果映射到 Glide Transformation
            val glideTransformations = mapTransformations(request.transformations, request.cacheKey())
            if (glideTransformations.isNotEmpty()) {
                transform(*glideTransformations.toTypedArray())
            }

            // 渐显动画
            if (!request.crossfadeEnabled) {
                dontAnimate()
            }
        }.into(imageView)
    }

    private fun loadIntoCallback(request: ImageRequest, target: ImageTarget.CallbackTarget) {
        val context = findContext(request)
        if (context == null) {
            target.onError?.invoke(IllegalStateException("无法获取 Context"))
            return
        }

        val source = request.source

        @Suppress("UNCHECKED_CAST")
        val builder = when (source) {
            is ImageSource.Url -> Glide.with(context).asBitmap().load(source.url)
            is ImageSource.ResId -> Glide.with(context).asBitmap().load(source.resId)
            is ImageSource.Bitmap -> Glide.with(context).asBitmap().load(source.bitmap)
            is ImageSource.Drawable -> Glide.with(context).asBitmap().load(source.drawable)
            is ImageSource.File -> Glide.with(context).asBitmap().load(source.file)
            is ImageSource.ContentUri -> Glide.with(context).asBitmap().load(source.uri)
            is ImageSource.Bytes -> Glide.with(context).asBitmap().load(source.data)
        }

        builder.apply {
            request.placeholderResId?.let { placeholder(it) }
            request.errorResId?.let { error(it) }

            val glideTransformations = mapTransformations(request.transformations, request.cacheKey())
            if (glideTransformations.isNotEmpty()) {
                transform(*glideTransformations.toTypedArray())
            }
        }.into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
            ) {
                target.onSuccess(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    /**
     * 将自定义 Transformation 映射为 Glide Transformation
     */
    private fun mapTransformations(
        transformations: List<app.allever.android.lib.imageloader.core.transformation.Transformation>,
        cacheKey: String
    ): List<GlideTransformation<Bitmap>> {
        return transformations.mapNotNull { t ->
            when (t) {
                is app.allever.android.lib.imageloader.core.transformation.RoundedCorners ->
                    GlideRoundedCorners(t.radius.toInt())

                is app.allever.android.lib.imageloader.core.transformation.CircleTransformation ->
                    CircleCrop()

                else ->
                    // 其他变换（模糊、灰度等）通过包装器适配到 Glide
                    WrapperGlideTransformation(t, cacheKey)
            }
        }
    }

    /**
     * 包装自定义 Transformation 为 Glide Transformation
     */
    private class WrapperGlideTransformation(
        private val transformation: app.allever.android.lib.imageloader.core.transformation.Transformation,
        private val cacheKey: String
    ) : GlideTransformation<Bitmap> {

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(cacheKey.toByteArray(Charsets.UTF_8))
        }

        override fun transform(
            context: android.content.Context,
            resource: com.bumptech.glide.load.engine.Resource<Bitmap>,
            outWidth: Int,
            outHeight: Int
        ): com.bumptech.glide.load.engine.Resource<Bitmap> {
            val bitmap = resource.get()
            val result = transformation.transform(bitmap)
            return com.bumptech.glide.load.resource.SimpleResource(result)
        }
    }

    private fun findContext(request: ImageRequest): Context? {
        return (request.target as? ImageTarget.ImageViewTarget)?.view?.context
    }
}
