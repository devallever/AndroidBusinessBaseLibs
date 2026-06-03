package app.allever.android.lib.media.core.model

import androidx.annotation.IntDef

/**
 * 排序方式
 */
object SortBy {
    const val DATE_DESC = 0           // 按修改时间降序（最新在前）
    const val DATE_ASC = 1            // 按修改时间升序（最旧在前）
    const val NAME_ASC = 2            // 按文件名升序（A-Z）
    const val NAME_DESC = 3           // 按文件名降序（Z-A）
    const val SIZE_DESC = 4           // 按文件大小降序（最大在前）
    const val SIZE_ASC = 5            // 按文件大小升序（最小在前）

    @IntDef(DATE_DESC, DATE_ASC, NAME_ASC, NAME_DESC, SIZE_DESC, SIZE_ASC)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Type

    /** 转换为 SQL ORDER BY 子句 */
    fun toOrderByClause(@Type sortBy: Int, bucketGrouped: Boolean = false): String {
        val dateColumn = if (bucketGrouped) {
            "${MediaStoreColumn.DATE_TAKEN} DESC"
        } else {
            "${MediaStoreColumn.DATE_ADDED} DESC, ${MediaStoreColumn.ID} ASC"
        }
        return when (sortBy) {
            DATE_DESC -> dateColumn
            DATE_ASC -> "${MediaStoreColumn.DATE_ADDED} ASC"
            NAME_ASC -> "${MediaStoreColumn.DISPLAY_NAME} ASC"
            NAME_DESC -> "${MediaStoreColumn.DISPLAY_NAME} DESC"
            SIZE_DESC -> "${MediaStoreColumn.SIZE} DESC"
            SIZE_ASC -> "${MediaStoreColumn.SIZE} ASC"
            else -> dateColumn
        }
    }
}
