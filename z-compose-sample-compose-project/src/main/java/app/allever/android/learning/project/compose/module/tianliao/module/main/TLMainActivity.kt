package app.allever.android.learning.project.compose.module.tianliao.module.main

import androidx.compose.runtime.Composable
import app.allever.android.learning.project.compose.module.tianliao.module.main.ui.MainPage
import app.allever.android.lib.common.compose.BaseComposeActivity

class TLMainActivity : BaseComposeActivity() {
    override fun init() {

    }

    @Composable
    override fun ContentPage() {
        MainPage(this)
    }
}