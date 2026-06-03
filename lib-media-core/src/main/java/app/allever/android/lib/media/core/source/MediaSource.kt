package app.allever.android.lib.media.core.source

import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaStoreColumn
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.SortBy
import app.allever.android.lib.media.core.query.FolderDetailQuery
import app.allever.android.lib.media.core.query.MediaFolderDetail
import app.allever.android.lib.media.core.query.MediaQuery

/**
 * 数据源接口
 * 负责从系统媒体库查询数据，返回结构化的模型对象
 */
internal interface MediaSource {

    /**
     * 查询目录列表
     * 一次 Cursor 遍历完成：按 bucket_id 分组 → 每个 Folder 内按类型分类存放
     */
    suspend fun queryFolders(query: MediaQuery): List<MediaFolder>

    /**
     * 查询某个目录下的详情（支持分页）
     */
    suspend fun queryFolderDetail(query: FolderDetailQuery): MediaFolderDetail

    /**
     * 全局查询所有资源（不分目录）
     */
    suspend fun queryAll(query: MediaQuery): List<MediaItem>
}

/**
 * 投影列构建器 — 根据请求的类型动态计算需要查询的列
 * 只查需要的列，避免 SELECT * 的性能浪费
 */
internal object ProjectionBuilder {

    /**
     * 构建目录列表查询的投影列（包含 bucket 相关列 + 各类型的专属列）
     */
    fun buildForFolders(typeFlags: Int): Array<String> {
        val columns = mutableListOf<String>().apply {
            // 公共列（必须）
            add(MediaStoreColumn.ID)
            add(MediaStoreColumn.MEDIA_TYPE)
            add(MediaStoreColumn.DATA)
            add(MediaStoreColumn.DATE_ADDED)
            add(MediaStoreColumn.DATE_TAKEN)
            add(MediaStoreColumn.SIZE)
            add(MediaStoreColumn.MIME_TYPE)
            add(MediaStoreColumn.DISPLAY_NAME)
            // 目录分组用
            add(MediaStoreColumn.BUCKET_ID)
            add(MediaStoreColumn.BUCKET_DISPLAY_NAME)

            // 图片独有列
            if (MediaType.contains(typeFlags, MediaType.IMAGE)) {
                add(MediaStoreColumn.WIDTH)
                add(MediaStoreColumn.HEIGHT)
                add(MediaStoreColumn.ORIENTATION)
            }
            // 视频独有列
            if (MediaType.contains(typeFlags, MediaType.VIDEO)) {
                add(MediaStoreColumn.DURATION)
                add(MediaStoreColumn.WIDTH)
                add(MediaStoreColumn.HEIGHT)
            }
            // 音频独有列（Files 表中可用的列）
            if (MediaType.contains(typeFlags, MediaType.AUDIO)) {
                add(MediaStoreColumn.DURATION)
                add(MediaStoreColumn.TITLE)
                add(MediaStoreColumn.ARTIST)
                add(MediaStoreColumn.ALBUM)
                // 注意: album_id 不在 MediaStore.Files 表中，仅在 Audio 表中存在
            }
        }
        return columns.toTypedArray()
    }

    /**
     * 构建全局资源查询的投影列（不含 bucket 列）
     */
    fun buildForAll(typeFlags: Int): Array<String> {
        val columns = mutableListOf<String>().apply {
            add(MediaStoreColumn.ID)
            add(MediaStoreColumn.MEDIA_TYPE)
            add(MediaStoreColumn.DATA)
            add(MediaStoreColumn.DATE_ADDED)
            add(MediaStoreColumn.DATE_TAKEN)
            add(MediaStoreColumn.SIZE)
            add(MediaStoreColumn.MIME_TYPE)
            add(MediaStoreColumn.DISPLAY_NAME)

            if (MediaType.contains(typeFlags, MediaType.IMAGE)) {
                add(MediaStoreColumn.WIDTH)
                add(MediaStoreColumn.HEIGHT)
                add(MediaStoreColumn.ORIENTATION)
            }
            if (MediaType.contains(typeFlags, MediaType.VIDEO)) {
                add(MediaStoreColumn.DURATION)
                add(MediaStoreColumn.WIDTH)
                add(MediaStoreColumn.HEIGHT)
            }
            if (MediaType.contains(typeFlags, MediaType.AUDIO)) {
                add(MediaStoreColumn.DURATION)
                add(MediaStoreColumn.TITLE)
                add(MediaStoreColumn.ARTIST)
                add(MediaStoreColumn.ALBUM)
                // album_id 不在 Files 表中
            }
        }
        return columns.toTypedArray()
    }
}
