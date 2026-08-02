package app.allever.android.lib.common.compose.widget

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.allever.android.lib.common.compose.data.TextClickItem

/**
 *@Description
 *@author: zq
 *@date: 2024/3/15
 */


/**
 * 列表
 */
@Composable
fun FunctionList(list: List<TextClickItem>) {
    LazyColumn {
        itemsIndexed(list) { index, item ->
            Column(Modifier
                .fillMaxSize()
                .clickable {
                    item.block.invoke(item)
                    Log.d("", "FunctionList: ${item.title}")
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
    }
}
