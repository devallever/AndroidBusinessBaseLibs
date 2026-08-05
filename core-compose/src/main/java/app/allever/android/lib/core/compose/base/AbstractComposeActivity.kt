package app.allever.android.lib.core.compose.base

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsControllerCompat
import app.allever.android.lib.core.base.AbstractSwipeBackActivity
import app.allever.android.lib.core.util.StatusBarCompat

abstract class AbstractComposeActivity: AbstractSwipeBackActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportEnableEdgeToEdge()) {
            enableEdgeToEdge()
        }
//
//        // 适配导航栏：根据主题设置不透明颜色
//        window.navigationBarColor = if (isDarkMode()) Color.BLACK else Color.WHITE
//        // 导航栏按钮图标颜色与背景相反
//        val navController = WindowInsetsControllerCompat(window, window.decorView)
//        navController.isAppearanceLightNavigationBars = !isDarkMode()


        //状态栏颜色
        if (isDarkMode()) {
            StatusBarCompat.cancelLightStatusBar(this)
        } else {
            StatusBarCompat.changeToLightStatusBar(this)
        }
    }

    protected open fun supportEnableEdgeToEdge(): Boolean {
        return true
    }

    /**
     * true: 黑夜模式，白色字体
     * false：白光模式，黑色字体
     *
     * @return isDarkMode
     */
    protected open fun isDarkMode(): Boolean {
        return false
    }
}