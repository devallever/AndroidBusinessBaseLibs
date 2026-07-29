package app.allever.android.lib.common.compose

import android.os.Bundle
import androidx.compose.runtime.Composable

/**
 * Compose 动态页面接口
 * 
 * ComposeScreen 用于通过反射动态加载 Compose 页面，类似传统 Fragment 的动态加载机制。
 * 
 * 使用方式：
 * 1. 实现此接口的类会被 ComposeActivity 通过反射创建
 * 2. 在 Content 方法中定义 Compose UI
 * 3. 通过 ComposeActivity.start<你的页面类>() 启动
 * 
 * 示例：
 * ```kotlin
 * class MyComposeScreen : ComposeScreen {
 *     @Composable
 *     override fun Content(args: Bundle?) {
 *         Text("Hello Compose!")
 *     }
 * }
 * 
 * // 启动页面
 * ComposeActivity.start<MyComposeScreen>("标题")
 * ```
 */
interface ComposeContent {
    /**
     * Compose 页面内容
     * 
     * @param args 从启动 Activity 传入的参数
     */
    @Composable
    fun Content(args: Bundle?)
}
