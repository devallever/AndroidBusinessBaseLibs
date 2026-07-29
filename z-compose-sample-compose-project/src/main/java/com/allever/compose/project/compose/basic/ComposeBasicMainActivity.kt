package com.allever.compose.project.compose.basic

import android.graphics.pdf.PdfDocument.Page
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.allever.android.lib.core.helper.ActivityHelper
import com.allever.compose.core.TextClickItem
import com.allever.compose.core.ui.ComposeProjectTheme
import com.allever.compose.core.ui.FunctionList

/**
 *@Description
 *@author: zq
 *@date: 2024/3/15
 */
class ComposeBasicMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeProjectTheme {
                FunctionList(list = mutableListOf<TextClickItem>().apply {
                    add(TextClickItem("基础控件", "基础控件基本用法") {
                        ActivityHelper.startActivity<BasicWidgetActivity>(this@ComposeBasicMainActivity) { }
                    })
                    add(TextClickItem("高级控件-列表", "高级控件基本用法，列表") {
                        ActivityHelper.startActivity<ListWidgetActivity>(this@ComposeBasicMainActivity) { }
                    })
                    add(TextClickItem("高级控件-网格", "高级控件基本用法，网格") {
                        ActivityHelper.startActivity<GridWidgetActivity>(this@ComposeBasicMainActivity) { }
                    })
                    add(TextClickItem("高级控件-ViewPager", "高级控件基本用法，分页") {
                        ActivityHelper.startActivity<PagerWidgetActivity>(this@ComposeBasicMainActivity) { }
                    })

                    add(TextClickItem("App-页面架构-底部导航+ViewPager", "App页面架构") {
                        ActivityHelper.startActivity<AppFrameBottomNaviActivity>(this@ComposeBasicMainActivity) { }
                    })
                })
            }

        }
    }
}