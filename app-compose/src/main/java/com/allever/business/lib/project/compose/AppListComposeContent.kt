package com.allever.business.lib.project.compose

import app.allever.android.lib.common.compose.ListComposeContent
import app.allever.android.lib.common.compose.widget.TextClickAdapter
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.data.TextClickItem
import app.allever.android.lib.core.ext.toast

class AppListComposeContent : ListComposeContent<TextClickItem>() {
    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("Green VPN", "compose-sample-app-green-vpn") {
            Navi.navigateTo(RouterPath.PATH_COMPOSE_SAMPLE_GREEN_VPN)
        })

    override fun getLayoutAdapter(): LayoutAdapter<TextClickItem> = TextClickAdapter()
}