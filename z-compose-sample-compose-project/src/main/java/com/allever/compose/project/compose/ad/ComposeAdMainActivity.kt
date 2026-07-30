package com.allever.compose.project.compose.ad

import androidx.compose.runtime.Composable
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.common.compose.widget.FunctionList
import app.allever.android.lib.common.compose.widget.TextClickItem
import app.allever.android.lib.core.helper.ActivityHelper

class ComposeAdMainActivity: BaseComposeActivity() {
    override fun init() {

    }

    @Composable
    override fun ContentPage() {
        FunctionList(list = mutableListOf<TextClickItem>().apply {
            add(TextClickItem("Compose-AdMob内置广告", "横幅，插屏，激励") {
                ActivityHelper.startActivity<AdMobBasicActivity>(this@ComposeAdMainActivity) {  }
            })
            add(TextClickItem("Compose-AdMob原生广告", "原生广告, Banner广告，大图广告") {
                ActivityHelper.startActivity<AdMobNativeActivity>(this@ComposeAdMainActivity) {}
            })
        })
    }
}