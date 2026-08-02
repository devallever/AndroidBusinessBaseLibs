package com.allever.compose.project.compose.basic

import androidx.compose.runtime.Composable
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.common.compose.data.TextClickItem
import app.allever.android.lib.common.compose.widget.FunctionList

/**
 *@Description
 *@author: zq
 *@date: 2024/3/15
 */
class ComposeBasicMainActivity : BaseComposeActivity() {

    override fun init() {
        initTopBar("Compose 组件")
    }

    @Composable
    override fun ContentPage() {
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