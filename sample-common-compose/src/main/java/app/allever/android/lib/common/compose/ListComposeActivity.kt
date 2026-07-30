package app.allever.android.lib.common.compose

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.widget.LayoutManager

/**
 * 通用列表 Activity - 支持垂直、水平、网格布局
 *
 * 使用方式：
 * ```kotlin
 * class MyActivity : ListComposeActivity2<TextClickItem>() {
 *     override fun getPageTitle() = "标题"
 *     override fun getList() = mutableListOf(...)
 *     override fun getLayoutAdapter() = DefaultLayoutAdapter()
 *     // 可选：指定布局类型
 *     override fun getLayoutManager() = LayoutManager.GRID_VERTICAL
 * }
 * ```
 */
abstract class ListComposeActivity<T> : BaseComposeActivity() {

    @Composable
    override fun ContentPage() {
        val adapter = getLayoutAdapter()
        val list = getList()
        when (getLayoutManager()) {
            LayoutManager.VERTICAL -> {
                LazyColumn {
                    adapter.adapterContent(this, list)
                }
            }
            LayoutManager.HORIZONTAL -> {
                LazyRow {
                    adapter.adapterContent(this, list)
                }
            }
            LayoutManager.GRID_VERTICAL -> {
                LazyVerticalGrid(columns = GridCells.Fixed(getSpanCount())) {
                    adapter.adapterContent(this, list)
                }
            }
            LayoutManager.GRID_HORIZONTAL -> {
                LazyHorizontalGrid(rows = GridCells.Fixed(getSpanCount())) {
                    adapter.adapterContent(this, list)
                }
            }
        }
    }

    override fun init() {
        initTopBar(getPageTitle(), true)
    }

    /**
     * 获取页面标题
     */
    abstract fun getPageTitle(): String

    /**
     * 获取列表数据
     */
    abstract fun getList(): MutableList<T>

    /**
     * 获取布局适配器
     */
    abstract fun getLayoutAdapter(): LayoutAdapter<T>

    /**
     * 获取布局管理器类型
     */
    protected open fun getLayoutManager(): LayoutManager = LayoutManager.VERTICAL

    /**
     * 获取网格行列数（仅 GRID 布局生效）
     */
    protected open fun getSpanCount(): Int = 2
}
