package app.allever.android.lib.media.core.query

import app.allever.android.lib.media.core.model.MediaFolder
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.Pagination
import app.allever.android.lib.media.core.model.SortBy

/**
 * 媒体查询参数
 * 用于目录列表查询和全局资源查询
 *
 * @property typeFlags 媒体类型组合标志（MediaType 位运算组合）
 * @property pagination 分页策略（全量或分页）
 * @property sortBy 排序方式
 * @property bucketId 指定目录 ID（可选，不传则查所有目录）
 * @property mimeTypePattern MIME 类型过滤模式（可选，如 "image/gif"）
 * @property minDuration 最小时长限制（视频/音频，毫秒）
 * @property maxDuration 最大时长限制（视频/音频，毫秒）
 */
data class MediaQuery(
    val typeFlags: Int = MediaType.ALL,
    val pagination: Pagination = Pagination.All,
    @SortBy.Type val sortBy: Int = SortBy.DATE_DESC,
    val bucketId: Long? = null,
    val mimeTypePattern: String? = null,
    val minDuration: Long = 0,
    val maxDuration: Long = Long.MAX_VALUE,
) {
    /** 是否为全量查询 */
    val isAllType: Boolean get() = typeFlags == MediaType.ALL

    /** 是否指定了单个目录 */
    val isBucketSpecific: Boolean get() = bucketId != null

    /**
     * 复制并修改分页参数，用于 Flow 分页模式逐页请求
     */
    fun copyForPage(page: Int): MediaQuery = copy(
        pagination = (pagination as? Pagination.Paged)?.copy(page = page)
            ?: Pagination.Paged(page)
    )
}

/**
 * 目录详情查询参数
 * 用于进入某个目录后查看其内部资源列表
 *
 * @property bucketId 目录 ID（必填）
 * @property typeFlags 媒体类型组合标志
 * @property pagination 分页策略
 * @property sortBy 排序方式
 */
data class FolderDetailQuery(
    val bucketId: Long,
    val typeFlags: Int = MediaType.ALL,
    val pagination: Pagination = Pagination.All,
    @SortBy.Type val sortBy: Int = SortBy.DATE_DESC,
)

/**
 * 目录详情查询结果
 */
data class MediaFolderDetail(
    val folder: MediaFolder,
    val images: List<MediaItem.Image>,
    val videos: List<MediaItem.Video>,
    val audios: List<MediaItem.Audio>,
) {
    /** 该目录下请求类型的总数量 */
    fun totalCount(@MediaType.Type typeFlags: Int): Int {
        var count = 0
        if (MediaType.contains(typeFlags, MediaType.IMAGE)) count += images.size
        if (MediaType.contains(typeFlags, MediaType.VIDEO)) count += videos.size
        if (MediaType.contains(typeFlags, MediaType.AUDIO)) count += audios.size
        return count
    }

    val allItems: List<MediaItem>
        get() = buildList {
            addAll(images)
            addAll(videos)
            addAll(audios)
        }
}
