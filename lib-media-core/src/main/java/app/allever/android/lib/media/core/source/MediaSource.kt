package app.allever.android.lib.media.core.source

import android.provider.MediaStore
import app.allever.android.lib.media.core.model.MediaStoreColumn
import app.allever.android.lib.media.core.model.MediaType

/**
 * 媒体数据源接口
 * 定义查询目录列表、目录详情、全局资源的能力
 */
internal interface MediaSource {

    /** 查询目录列表 */
    suspend fun queryFolders(query: app.allever.android.lib.media.core.query.MediaQuery): List<app.allever.android.lib.media.core.model.MediaFolder>

    /** 查询某个目录的详情（含该目录下的媒体文件列表） */
    suspend fun queryFolderDetail(query: app.allever.android.lib.media.core.query.FolderDetailQuery): app.allever.android.lib.media.core.query.MediaFolderDetail

    /** 全局查询（不分目录） */
    suspend fun queryAll(query: app.allever.android.lib.media.core.query.MediaQuery): List<app.allever.android.lib.media.core.model.MediaItem>
}

/**
 * 投影列构建器
 * 根据查询类型动态生成所需的列，避免不必要的列读取
 */
internal object ProjectionBuilder {

    /**
     * 目录/详情查询的投影列
     * 包含：基础信息 + bucket 信息 + 类型相关列
     *
     * @param types 查询的媒体类型集合
     */
    fun buildForFolders(types: Set<MediaType.Type>): Array<String> {
        val columns = mutableListOf(
            // 基础列
            MediaStoreColumn.ID,
            MediaStoreColumn.DATA,
            MediaStoreColumn.DISPLAY_NAME,
            MediaStoreColumn.MIME_TYPE,
            MediaStoreColumn.MEDIA_TYPE,
            MediaStoreColumn.SIZE,
            MediaStoreColumn.DATE_ADDED,
            // 目录分组列
            MediaStoreColumn.BUCKET_ID,
            MediaStoreColumn.BUCKET_DISPLAY_NAME,
        )

        // 图片特有列
        if (types.contains(MediaType.Type.IMAGE)) {
            columns.addAll(listOf(MediaStoreColumn.WIDTH, MediaStoreColumn.HEIGHT, MediaStoreColumn.ORIENTATION))
        }

        // 视频/音频特有列
        if (types.contains(MediaType.Type.VIDEO) || types.contains(MediaType.Type.AUDIO)) {
            columns.add(MediaStoreColumn.DURATION)
        }

        // 视频尺寸列
        if (types.contains(MediaType.Type.VIDEO)) {
            columns.addAll(listOf(MediaStoreColumn.WIDTH, MediaStoreColumn.HEIGHT))
        }

        // 音频特有列
        if (types.contains(MediaType.Type.AUDIO)) {
            columns.addAll(listOf(MediaStoreColumn.TITLE, MediaStoreColumn.ARTIST, MediaStoreColumn.ALBUM))
        }

        return columns.toTypedArray()
    }

    /**
     * 全局查询的投影列（同 buildForFolders，保持一致）
     */
    fun buildForAll(types: Set<MediaType.Type>): Array<String> = buildForFolders(types)

    /**
     * 最小投影列（仅用于 count 等轻量操作）
     */
    val MINIMAL: Array<String> = arrayOf(
        MediaStoreColumn.ID,
        MediaStoreColumn.MEDIA_TYPE,
        MediaStoreColumn.BUCKET_ID,
    )
}
