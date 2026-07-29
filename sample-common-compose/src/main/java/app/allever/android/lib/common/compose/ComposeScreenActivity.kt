package app.allever.android.lib.common.compose

import android.os.Bundle
import android.text.TextUtils
import androidx.compose.runtime.Composable
import app.allever.android.lib.common.compose.theme.ComposeProjectTheme
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.ActivityHelper

/**
 * Compose 动态页面容器 Activity
 * 
 * 支持通过反射动态加载实现 [ComposeScreen] 接口的页面类，
 * 类似 FragmentActivity 动态加载 Fragment 的机制。
 * 
 * 使用方式：
 * ```kotlin
 * // 基础用法
 * ComposeActivity.start<MyComposeScreen>("页面标题")
 * 
 * // 带参数
 * ComposeActivity.start<MyComposeScreen>("页面标题") { bundle ->
 *     bundle.putString("key", "value")
 * }
 * ```
 */
class ComposeScreenActivity : BaseComposeActivity() {

    companion object {
        /**
         * 通过反射启动 Compose 页面
         * 
         * @param title 页面标题
         * @param clz 页面类
         */
        fun <T : Class<*>> start(
            title: String,
            clz: T
        ) {
            ActivityHelper.startActivity<ComposeScreenActivity> {
                putExtra("screenClassName", clz.name)
                putExtra("title", title)
            }
        }

        /**
         * 通过反射启动 Compose 页面（内联泛型版本）
         * 
         * @param title 页面标题
         * @param showTopBar 是否显示顶部栏
         * @param darkMode 深色模式
         */
        inline fun <reified T> start(
            title: String,
            showTopBar: Boolean = true,
            darkMode: Boolean = false
        ) {
            ActivityHelper.startActivity<ComposeScreenActivity> {
                putExtra("screenClassName", T::class.java.name)
                putExtra("title", title)
                putExtra("showTopBar", showTopBar)
                putExtra("darkMode", darkMode)
            }
        }

        /**
         * 通过反射启动 Compose 页面（带参数版本）
         * 
         * @param title 页面标题
         * @param block 配置参数的 lambda
         */
        inline fun <reified T> start(
            title: String,
            showTopBar: Boolean = true,
            darkMode: Boolean = false,
            block: (args: Bundle) -> Unit
        ) {
            val args = Bundle()
            block.invoke(args)
            ActivityHelper.startActivity<ComposeScreenActivity> {
                putExtra("screenClassName", T::class.java.name)
                putExtra("title", title)
                putExtra("showTopBar", showTopBar)
                putExtra("darkMode", darkMode)
                putExtra("screenArgs", args)
            }
        }
    }

    private var screenInstance: ComposeScreen? = null
    private var screenArgs: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        loadScreen()
        super.onCreate(savedInstanceState)
        val title = intent?.getStringExtra("title") ?: "ComposeActivity"
    }

    /**
     * 通过反射加载 Compose 页面
     */
    private fun loadScreen() {
        screenArgs = intent?.getBundleExtra("screenArgs")
        try {
            val className = intent?.getStringExtra("screenClassName")
            if (TextUtils.isEmpty(className)) {
                log("screenClassName: Empty")
                return
            }
            log("screenClassName: $className")
            val instance = Class.forName(className!!).getConstructor().newInstance()
            if (instance is ComposeScreen) {
                screenInstance = instance
            } else {
                log("Class does not implement ComposeScreen: $className")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            log("Failed to load screen: ${e.message}")
        }
    }

    @Composable
    override fun ContentPage() {
        ComposeProjectTheme {
            val screen = screenInstance
            if (screen != null) {
                screen.Content(screenArgs)
            } else {
                ErrorScreen()
            }
        }
    }

    /**
     * 错误/空页面提示
     */
    @Composable
    private fun ErrorScreen() {
        androidx.compose.material3.Text(
            text = "页面加载失败",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        screenInstance = null
    }
}
