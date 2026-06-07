package app.allever.android.lib.imageloader.core.source

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import java.io.File

/**
 * 图片数据源 - 支持多种输入格式
 *
 * 使用 sealed interface 保证类型安全，编译期穷尽检查
 */
sealed interface ImageSource {

    /** 从网络 URL 加载 */
    data class Url(val url: String) : ImageSource

    /** 从资源 ID 加载 */
    data class ResId(@DrawableRes val resId: Int) : ImageSource

    /** 直接使用 Bitmap（内存中已有） */
    data class Bitmap(val bitmap: android.graphics.Bitmap) : ImageSource

    /** 直接使用 Drawable */
    data class Drawable(val drawable: android.graphics.drawable.Drawable) : ImageSource

    /** 从本地文件加载 */
    data class File(val file: java.io.File) : ImageSource

    /** 从 Content URI 加载 */
    data class ContentUri(val uri: android.net.Uri) : ImageSource

    /** 从字节数组加载 */
    data class Bytes(val data: kotlin.ByteArray) : ImageSource

    /**
     * 生成唯一缓存 Key
     * 相同数据源应返回相同 key，用于缓存命中判断
     */
    fun key(): String = when (this) {
        is Url -> "url:$url"
        is ResId -> "res:$resId"
        is Bitmap -> "bitmap:${hashCode()}"
        is Drawable -> "drawable:${hashCode()}"
        is File -> "file:${file.absolutePath}"
        is ContentUri -> "uri:$uri"
        is Bytes -> "bytes:${data.contentHashCode()}"
    }

    companion object {

        /**
         * 将任意类型转换为 ImageSource
         * 支持: String(Url), Int(ResId), Bitmap, Drawable, File, Uri, ByteArray
         */
        fun from(any: Any?): ImageSource? = when (any) {
            null -> null
            is String -> Url(any)
            is Int -> ResId(any)
            is android.graphics.Bitmap -> Bitmap(any)
            is android.graphics.drawable.Drawable -> Drawable(any)
            is java.io.File -> File(any)
            is android.net.Uri -> ContentUri(any)
            is kotlin.ByteArray -> Bytes(any)
            is ImageSource -> any
            else -> null
        }
    }
}
