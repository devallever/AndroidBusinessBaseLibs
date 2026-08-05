package app.allever.android.lib.common.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.allever.android.lib.common.compose.theme.ComposeProjectTheme
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.compose.base.AbstractComposeActivity
import app.allever.android.lib.core.helper.DisplayHelper

abstract class BaseComposeActivity : AbstractComposeActivity() {

    // 标题状态，修改此值会自动刷新 UI
    protected var title by mutableStateOf("")
    protected var showBackIcon by mutableStateOf(false)
    protected var showTopBar by mutableStateOf(false)
    protected var statusBarColor by mutableStateOf(Color(0xFFFFFFFF))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeProjectTheme {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                ) {
                    if (adaptStatusBar()) {
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
                                .background(statusBarColor)
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
                        ContentPage()
                    }
                }

            }
        }
        init()
    }

    abstract fun init()

    @Composable
    abstract fun ContentPage()

    protected open fun adaptStatusBar(): Boolean {
        return true
    }

    /**
     * 初始化/修改标题栏文本
     * 调用后会自动刷新 Compose UI
     */
    protected fun initTopBar(title: String = "AppBar", showBackIcon: Boolean = true) {
        this.title = title
        this.showBackIcon = showBackIcon
        this.showTopBar = true
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
                    finish()
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
}