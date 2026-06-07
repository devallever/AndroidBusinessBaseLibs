package app.allever.android.lib.imageloader.core.target

import android.graphics.Bitmap
import android.widget.ImageView

/**
 * 图片加载目标抽象
 *
 * 封装图片最终显示的位置，默认支持 ImageView
 * 可扩展为自定义 Target（如回调获取 Bitmap、设置到自定义 View 等）
 */
sealed interface ImageTarget {

    /** 加载到 ImageView */
    data class ImageViewTarget(val view: ImageView) : ImageTarget

    /** 仅获取 Bitmap 回调（不显示） */
    data class CallbackTarget(
        val onSuccess: (Bitmap) -> Unit,
        val onError: ((Throwable) -> Unit)? = null
    ) : ImageTarget
}
