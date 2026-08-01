package app.allever.android.lib.common.compose

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.widget.LayoutManager
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.DisplayHelper
import app.allever.android.lib.core.helper.KeyEventHelper

abstract class ListComposeContent<T>: ComposeContent {

    companion object {
        inline fun <reified T> start(
            title: String,
            adaptStatusBar: Boolean = false,
            showTopBar: Boolean = false,
            showBackIcon: Boolean = true,
            darkMode: Boolean = false,
        ) {
            ComposeContentActivity.start<T>("", adaptStatusBar = false) {
                it.putString("title", title)
                it.putBoolean("adaptStatusBar", adaptStatusBar)
                it.putBoolean("showTopBar", showTopBar)
                it.putBoolean("showBackIcon", showBackIcon)
                it.putBoolean("darkMode", darkMode)
            }
        }
    }
    private var title: String = ""
    private var showBackIcon = false
    @Composable
    override fun Content(args: Bundle?) {
        title = args?.getString("title") ?: "ListComposeContent"
        val adaptStatusBar = args?.getBoolean("adaptStatusBar", false) ?: false
        val showTopBar = args?.getBoolean("showTopBar", false) ?: false
        showBackIcon = args?.getBoolean("showBackIcon", true) ?: true

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = Color(0xFFFFFFFF)),
        ) {
            if (adaptStatusBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(
                            DisplayHelper.px2dip(
                                DisplayHelper.getStatusBarHeight(
                                    App.context
                                )
                            ).dp
                        )
                )
            }
            if (showTopBar) {
                TopBar()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(color = Color(0xFFEFEFEF))
            ) {
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
        }
    }

    @Preview
    @Composable
    private fun TopBar() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = Color(0xFFFFFFFF))
        ) {
            if (showBackIcon) {
                IconButton(onClick = {
                    KeyEventHelper.clickBack()
                }, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        painter = painterResource(id = app.allever.android.lib.common.R.drawable.ic_back),
                        contentDescription = "",
                        modifier = Modifier
                            .size(42.dp)
                            .padding(12.dp)
                    )
                }
            }
            Text(text = title, modifier = Modifier.align(Alignment.Center), fontSize = 17.sp)
        }
    }

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