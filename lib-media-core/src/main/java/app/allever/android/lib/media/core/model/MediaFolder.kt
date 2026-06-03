package app.allever.android.lib.media.core.model

import android.net.Uri

/**
 * 媒体文件夹 / 相册 / 专辑
 * 作为一级节点，内部按图片、视频、音频三种类型分组存放列表
 */
data class MediaFolder(
    val bucketId: Long,               // 文件夹唯一标识（来自 MediaStore 的 bucket_id）
    val name: String,                 // 显示名称（如"相机"、"截图"、"Download"）
    val path: String,                 // 文件夹路径（如 "/storage/emulated/0/DCIM"）
    val coverUri: Uri?,              // 封面 Uri（取该目录下最新的一条记录）

    // 该目录下的资源，按类型分组
    val images: List<MediaItem.Image> = emptyList(),
    val videos: List<MediaItem.Video> = emptyList(),
    val audios: List<MediaItem.Audio> = emptyList(),
) {

    /**
     * 该目录下指定类型的总数量
     * @param typeFlags MediaType 组合标志
     */
    fun totalCount(@MediaType.Type typeFlags: Int): Int {
        var count = 0
        if (MediaType.contains(typeFlags, MediaType.IMAGE)) count += images.size
        if (MediaType.contains(typeFlags, MediaType.VIDEO)) count += videos.size
        if (MediaType.contains(typeFlags, MediaType.AUDIO)) count += audios.size
        return count
    }

    /**
     * 该目录下所有资源的总数量
     */
    val totalCount: Int get() = images.size + videos.size + audios.size

    /**
     * 是否为空文件夹
     */
    val isEmpty: Boolean get() = totalCount == 0
}
