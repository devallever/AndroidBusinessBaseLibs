package com.allever.compose.project.compose.ad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.allever.compose.core.ActivityHelper
import com.allever.compose.core.TextClickItem
import com.allever.compose.core.ui.ComposeProjectTheme
import com.allever.compose.core.ui.FunctionList

class ComposeAdMainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeProjectTheme {
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
    }
}