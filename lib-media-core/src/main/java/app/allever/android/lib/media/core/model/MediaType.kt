package app.allever.android.lib.media.core.model

/**
 * 媒体类型枚举
 * 支持任意组合，通过 List/Set 传入
 *
 * 使用示例：
 * listOf(MediaType.Type.IMAGE, MediaType.Type.VIDEO)  // 图片和视频
 * MediaType.ALL                                         // 全部类型列表
 */
object MediaType {

    /** 媒体类型枚举 */
    enum class Type {
        IMAGE,
        VIDEO,
        AUDIO,
    }

    /** 全部类型 */
    val ALL: Set<Type> = setOf(Type.IMAGE, Type.VIDEO, Type.AUDIO)

    /** 常用预设组合 */
    val IMAGE_AND_VIDEO: Set<Type> = setOf(Type.IMAGE, Type.VIDEO)
    val IMAGE_AND_AUDIO: Set<Type> = setOf(Type.IMAGE, Type.AUDIO)
    val VIDEO_AND_AUDIO: Set<Type> = setOf(Type.VIDEO, Type.AUDIO)

    /**
     * 将 MediaStore 的 MEDIA_TYPE 值转换为 Type 枚举
     * @return 对应的 Type，未知值返回 null
     */
    fun fromMediaStoreMediaType(mediaStoreType: Int): Type? {
        return when (mediaStoreType) {
            1 -> Type.IMAGE   // MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            2 -> Type.AUDIO   // MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
            3 -> Type.VIDEO   // MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
            else -> null
        }
    }

    fun name(type: Type): String = when (type) {
        Type.IMAGE -> "Image"
        Type.VIDEO -> "Video"
        Type.AUDIO -> "Audio"
    }

    /** 将类型集合转为可读标签，如 "IMG+VID" */
    fun label(types: Set<Type>): String = buildString {
        if (types == ALL) { append("ALL"); return@buildString }
        if (types.contains(Type.IMAGE)) append("IMG")
        if (types.contains(Type.VIDEO)) append(if (isNotEmpty()) "+VID" else "VID")
        if (types.contains(Type.AUDIO)) append(if (isNotEmpty()) "+AUD" else "AUD")
    }
}
