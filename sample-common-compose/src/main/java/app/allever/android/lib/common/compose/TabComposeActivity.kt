package app.allever.android.lib.common.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * TabComposeActivity - 带 Tab 切换的 Compose Activity
 *
 * 类似 app.allever.android.lib.common.TabActivity，使用 Compose 实现。
 * 支持 Tab 栏点击切换、页面滑动切换、页面切换回调等功能。
 *
 * 使用方式：
 * ```kotlin
 * class MyTabActivity : TabComposeActivity() {
 *     override fun getPageTitle() = "我的Tab"
 *     override fun getTabTitles() = listOf("Tab1", "Tab2", "Tab3")
 *     override fun getPages() = listOf(
 *         Page1Content(),
 *         Page2Content(),
 *         Page3Content()
 *     )
 * }
 *
 * class Page1Content : ComposeContent {
 *     @Composable
 *     override fun Content(args: Bundle?) {
 *         Text("Page 1")
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
abstract class TabComposeActivity : BaseComposeActivity() {

    @Composable
    override fun ContentPage() {
        val tabTitles = getTabTitles()
        val pages = getPages()
        val pagerState = rememberPagerState(pageCount = { pages.size })
        val coroutineScope = rememberCoroutineScope()

        // 页面切换监听
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .distinctUntilChanged()
                .collect { page ->
                    onPageChanged(page)
                }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Tab 栏
            if (isScrollableTabs()) {
                ScrollableTabRow(
                    indicator = { tabPositions ->
                        TabIndicator(tabPositions, pagerState.currentPage)
                    },
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = getTabEdgePadding(),
                    containerColor = getTabContainerColor(),
                    contentColor = getTabContentColor(),
                    divider = {}) {
                    tabTitles.forEachIndexed { index, title ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = title, color = if (selected) getSelectedColor() else getUnselectedColor()) })
                    }
                }
            } else {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = getTabContainerColor(),
                    contentColor = getTabContentColor(),
                    indicator = { tabPositions ->
                        TabIndicator(tabPositions, pagerState.currentPage)
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = title, color = if (selected) getSelectedColor() else getUnselectedColor()) }
                        )
                    }
                }
            }

            // ViewPager 内容
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                pages[page].Content(null)
            }
        }
    }

    /**
     * 自定义 Tab 指示器
     * 当高度为 0 时不显示指示器
     */
    @Composable
    private fun TabIndicator(
        tabPositions: List<androidx.compose.material3.TabPosition>,
        currentPage: Int
    ) {
        if (currentPage < tabPositions.size) {
            val height = getIndicatorHeight()
            if (height > 0.dp) {
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[currentPage])
                        .height(height)
                        .background(getIndicatorColor())
                )
            }
        }
    }

    override fun init() {
        initTopBar(getPageTitle())
    }

    /**
     * 获取页面标题
     */
    abstract fun getPageTitle(): String

    /**
     * 获取 Tab 标题列表
     */
    abstract fun getTabTitles(): MutableList<String>

    /**
     * 获取页面内容列表
     *
     * @return 返回 ComposeContent 列表，每个对象对应一个 Tab 的内容
     */
    abstract fun getPages(): MutableList<ComposeContent>

    /**
     * 页面切换回调
     */
    open fun onPageChanged(position: Int) {}

    /**
     * 是否使用可滚动 Tab
     * true: ScrollableTabRow（Tab 较多时可滚动）
     * false: TabRow（Tab 平分屏幕宽度）
     */
    protected open fun isScrollableTabs(): Boolean = true

    /**
     * 可滚动 Tab 的边缘间距
     */
    protected open fun getTabEdgePadding(): Dp = 0.dp

    /**
     * Tab 容器颜色
     */
    protected open fun getTabContainerColor(): Color = Color.White

    /**
     * Tab 内容颜色
     */
    protected open fun getTabContentColor(): Color = Color.Black

    /**
     * Tab 选中颜色
     */
    protected open fun getSelectedColor(): Color = Color.Black

    /**
     * Tab 未选中颜色
     */
    protected open fun getUnselectedColor(): Color = Color(0xFF999999)

    /**
     * Tab 指示器高度
     * 设置为 0.dp 可隐藏指示器
     */
    protected open fun getIndicatorHeight(): Dp = 2.dp

    /**
     * Tab 指示器颜色
     */
    protected open fun getIndicatorColor(): Color = getTabContentColor()
}
