package com.allever.business.lib.project.compose

import app.allever.android.lib.common.compose.ComposeContent
import app.allever.android.lib.common.compose.TabComposeActivity

class TestTabComposeActivity: TabComposeActivity() {
    override fun getPageTitle(): String {
        return "TestTabCompose"
    }

    override fun getTabTitles(): MutableList<String> = mutableListOf("SampleComposeContent", "SampleListComposeContent")

    override fun getPages(): MutableList<ComposeContent>  = mutableListOf(
        SampleComposeContent(),
        SampleListComposeContent()
    )

}

