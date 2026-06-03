package app.allever.android.lib.media.core.model

/**
 * 分页策略，使用 sealed class 区分全量加载和分页加载两种模式
 */
sealed class Pagination {
    /**
     * 全量加载，不分页
     */
    data object All : Pagination()

    /**
     * 分页加载
     * @param page 页码，从 0 开始
     * @param pageSize 每页条数，默认 50
     */
    data class Paged(
        val page: Int = 0,
        val pageSize: Int = DEFAULT_PAGE_SIZE,
    ) : Pagination() {
        /** 当前页的偏移量 */
        val offset: Int get() = page * pageSize

        init {
            require(page >= 0) { "page must >= 0, but was $page" }
            require(pageSize > 0) { "pageSize must > 0, but was $pageSize" }
        }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 50
        const val MAX_PAGE_SIZE = 200
    }
}

/**
 * 对列表进行分页切片
 */
fun <T> List<T>.paginate(pagination: Pagination): List<T> {
    return when (pagination) {
        is Pagination.All -> this
        is Pagination.Paged -> {
            val from = pagination.offset.coerceAtMost(this.size)
            val end = (from + pagination.pageSize).coerceAtMost(this.size)
            if (from >= this.size) emptyList()
            else subList(from, end)
        }
    }
}
