package app.allever.android.lib.media.core.query

import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.Pagination
import app.allever.android.lib.media.core.model.SortBy

/**
 * 目录详情查询构建器 — DSL 风格配置
 *
 * 使用示例：
 * ```kotlin
 * val query = folderDetailQuery {
 *     bucketId = 123L
 *     types = MediaType.ALL
 *     pagination = Pagination.Paged(0, 30)
 * }
 * ```
 */
class FolderDetailQueryBuilder {
    var bucketId: Long = -1L
    var types: Set<MediaType.Type> = MediaType.ALL
    var pagination: Pagination = Pagination.All
    @SortBy.Type
    var sortBy: Int = SortBy.DATE_DESC

    fun build(): FolderDetailQuery {
        require(bucketId != 0.toLong()) { "bucketId is required" }
        return FolderDetailQuery(
            bucketId = bucketId,
            types = types,
            pagination = pagination,
            sortBy = sortBy,
        )
    }
}

/**
 * DSL 构建器入口函数
 */
inline fun folderDetailQuery(block: FolderDetailQueryBuilder.() -> Unit): FolderDetailQuery =
    FolderDetailQueryBuilder().apply(block).build()
