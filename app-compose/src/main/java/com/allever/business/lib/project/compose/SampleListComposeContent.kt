package com.allever.business.lib.project.compose

import app.allever.android.lib.common.compose.ComposeContentActivity
import app.allever.android.lib.common.compose.ListComposeContent
import app.allever.android.lib.common.compose.widget.TextClickAdapter
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.data.TextClickItem
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ActivityHelper

/**
 * 示例代码
 */
class SampleListComposeContent: ListComposeContent<TextClickItem>() {
    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem(
            "ComposeProject-旧项目代码", "z-compose-sample-compose-project"
        ) {
            Navi.navigateTo(RouterPath.PATH_Z_COMPOSE_SAMPLE_PROJECT)
        },
        TextClickItem("测试ComposContent") { it ->
            ComposeContentActivity.start<SampleComposeContent>(
                it.title,
                showTopBar = true,
                showBackIcon = true
            ) {
                it.putString("message", "Hello Compose Android!")
            }
        },
        TextClickItem("SampleListComposeContent") { it ->
            ListComposeContent.start<SampleListComposeContent>(it.title, adaptStatusBar = true, showTopBar = true)
        },
        TextClickItem("TestTabComposeActivity") {
            ActivityHelper.startActivity(TestTabComposeActivity::class.java)
        },
        TextClickItem("测试TextDetailClickItem3") {
            toast("点击了TextDetailClickIte3")
        },
    )

    override fun getLayoutAdapter(): LayoutAdapter<TextClickItem> = TextClickAdapter()
}