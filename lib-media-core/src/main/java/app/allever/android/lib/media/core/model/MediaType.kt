package app.allever.android.lib.media.core.model

import androidx.annotation.IntDef

/**
 * 媒体类型标志位，支持任意组合（位运算）
 *
 * 使用示例：
 * MediaType.IMAGE or MediaType.VIDEO  // 图片和视频
 * MediaType.ALL                       // 全部
 */
object MediaType {
    const val NONE = 0
    const val IMAGE = 1 shl 0       // 0b001
    const val VIDEO = 1 shl 1       // 0b010
    const val AUDIO = 1 shl 2       // 0b100
    const val ALL = IMAGE or VIDEO or AUDIO // 0b111

    /** 常用预设组合 */
    const val IMAGE_AND_VIDEO = IMAGE or VIDEO        // 0b011
    const val IMAGE_AND_AUDIO = IMAGE or AUDIO        // 0b101
    const val VIDEO_AND_AUDIO = VIDEO or AUDIO        // 0b110

    @IntDef(
        flag = true,
        value = [NONE, IMAGE, VIDEO, AUDIO, ALL, IMAGE_AND_VIDEO, IMAGE_AND_AUDIO, VIDEO_AND_AUDIO]
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    /** 判断 typeFlags 中是否包含指定类型 */
    fun contains(@Type typeFlags: Int, @Type type: Int): Boolean {
        return (typeFlags and type) != 0
    }

    /** 将 MediaStore 的 MEDIA_TYPE 值转换为 MediaType 标志 */
    fun fromMediaStoreMediaType(mediaStoreType: Int): Int {
        return when (mediaStoreType) {
            1 -> IMAGE   // MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            2 -> VIDEO   // MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            3 -> AUDIO   // MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
            else -> NONE
        }
    }

    fun name(@Type type: Int): String {
        return when (type) {
            IMAGE -> "Image"
            VIDEO -> "Video"
            AUDIO -> "Audio"
            ALL -> "All"
            else -> "Unknown"
        }
    }
}
