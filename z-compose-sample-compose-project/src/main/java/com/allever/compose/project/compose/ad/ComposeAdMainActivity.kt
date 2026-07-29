package com.allever.compose.project.compose.ad

import androidx.compose.runtime.Composable
import app.allever.android.lib.common.compose.BaseComposeActivity
import com.allever.compose.core.TextClickItem
import app.allever.android.lib.core.helper.ActivityHelper
import com.allever.compose.core.ui.FunctionList

class ComposeAdMainActivity: BaseComposeActivity() {
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