package com.allever.business.lib.project.compose

import app.allever.android.lib.common.compose.ComposeContent
import app.allever.android.lib.common.compose.TabComposeActivity
import app.allever.android.lib.router.annotation.Route

@Route(path = "/appcompose/main")
class MainActivity : TabComposeActivity() {
    override fun getPageTitle() = "Compose Project"
    override fun getTabTitles(): MutableList<String>  = mutableListOf("示例代码", "项目代码", "基础组件")

    override fun getPages(): MutableList<ComposeContent> = mutableListOf(
        SampleListComposeContent(),
        AppListComposeContent(),
        LibListComposeContent()
    )
}