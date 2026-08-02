package com.allever.business.lib.project.compose

import app.allever.android.lib.common.compose.ListComposeContent
import app.allever.android.lib.common.compose.widget.TextClickAdapter
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.data.TextClickItem
import app.allever.android.lib.core.ext.toast

class LibListComposeContent : ListComposeContent<TextClickItem>() {
    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("基础组件") {
            toast(it.title)
        }
    )

    override fun getLayoutAdapter(): LayoutAdapter<TextClickItem> = TextClickAdapter()
}