package app.allever.android.lib.common.compose.widget

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 布局适配器 - 用于在 LazyListScope 中填充列表内容
 *
 * 注意：AdapterContent 不需要 @Composable 注解，
 * 因为 LazyListScope.itemsIndexed 等函数本身不是 composable，
 * 它们只是在 LazyList 的作用域内添加 items。
 * 只有 itemContent lambda 内部才是 composable 上下文。
 */
abstract class LayoutAdapter<T> {
    /**
     * 在 LazyListScope 中填充列表内容
     *
     * @param lazyListScope LazyList 作用域（由 LazyColumn/LazyRow/LazyVerticalGrid 提供）
     * @param list 数据列表
     */
    abstract fun adapterContent(lazyScope: Any, list: List<T>)
}

/**
 * 默认文本列表适配器
 */
class DefaultLayoutAdapter : LayoutAdapter<TextClickItem>() {

    @Composable
    fun Content(item: TextClickItem) {
        Column(Modifier
            .fillMaxWidth()
            .clickable {
                item.block.invoke(item)
            }
            .padding(vertical = 10.dp, horizontal = 10.dp)) {
            Text(
                item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )
            Text(
                item.desc,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    override fun adapterContent(
        lazyScope: Any, list: List<TextClickItem>
    ) {
        when (lazyScope) {
            is LazyListScope -> {
                lazyScope.itemsIndexed(list) { index, item ->
                    Content(item)
                }
            }

            is LazyGridScope -> {
                lazyScope.itemsIndexed(list) { index, item ->
                    Content(item)
                }
            }
        }
    }
}
