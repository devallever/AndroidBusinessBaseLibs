package app.allever.android.lib.media.core.query

import app.allever.android.lib.media.core.model.MediaType
import app.allever.android.lib.media.core.model.Pagination
import app.allever.android.lib.media.core.model.SortBy

/**
 * 查询构建器 — DSL 风格配置查询参数
 *
 * 使用示例：
 * ```kotlin
 * val query = mediaQuery {
 *     type = MediaType.IMAGE or MediaType.VIDEO
 *     pagination = Pagination.Paged(0, 30)
 *     sortBy = SortBy.DATE_DESC
 * }
 * ```
 */
class MediaQueryBuilder {
    var typeFlags: Int = MediaType.ALL
    var pagination: Pagination = Pagination.All
    @SortBy.Type
    var sortBy: Int = SortBy.DATE_DESC
    var bucketId: Long? = null
    var mimeTypePattern: String? = null
    var minDuration: Long = 0
    var maxDuration: Long = Long.MAX_VALUE

    fun build(): MediaQuery = MediaQuery(
        typeFlags = typeFlags,
        pagination = pagination,
        sortBy = sortBy,
        bucketId = bucketId,
        mimeTypePattern = mimeTypePattern,
        minDuration = minDuration,
        maxDuration = maxDuration,
    )
}

/**
 * DSL 构建器入口函数
 */
inline fun mediaQuery(block: MediaQueryBuilder.() -> Unit): MediaQuery =
    MediaQueryBuilder().apply(block).build()
