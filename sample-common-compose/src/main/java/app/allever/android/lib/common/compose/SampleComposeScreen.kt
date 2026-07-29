package app.allever.android.lib.common.compose

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 示例 Compose 页面
 * 
 * 演示如何实现 [ComposeScreen] 接口来创建可动态加载的 Compose 页面。
 * 
 * 使用方式：
 * ```kotlin
 * // 启动页面
 * ComposeActivity.start<SampleComposeScreen>("示例页面")
 * 
 * // 带参数启动
 * ComposeActivity.start<SampleComposeScreen>("示例页面") { bundle ->
 *     bundle.putString("message", "Hello")
 * }
 * ```
 */
class SampleComposeScreen : ComposeScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(args: Bundle?) {
        val message = args?.getString("message") ?: "Hello Compose!"
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("示例页面") }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
