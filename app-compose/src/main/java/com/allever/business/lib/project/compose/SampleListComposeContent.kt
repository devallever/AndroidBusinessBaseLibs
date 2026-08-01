package com.allever.business.lib.project.compose

import app.allever.android.lib.common.compose.ListComposeContent
import app.allever.android.lib.common.compose.widget.DefaultLayoutAdapter
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.widget.TextClickItem
import app.allever.android.lib.core.ext.toast

class SampleListComposeContent: ListComposeContent<TextClickItem>() {
    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("1") {
            toast("1")
        },
        TextClickItem("2") {
            toast("2")
        },
        TextClickItem("3") {
            toast("3")
        },
        TextClickItem("4") {
            toast("4")
        }
    )

    override fun getLayoutAdapter(): LayoutAdapter<TextClickItem> = DefaultLayoutAdapter()
}