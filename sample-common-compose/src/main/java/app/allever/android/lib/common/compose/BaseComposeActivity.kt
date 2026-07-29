package app.allever.android.lib.common.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import app.allever.android.lib.common.compose.theme.ComposeProjectTheme
import app.allever.android.lib.core.compose.base.AbstractComposeActivity

abstract class BaseComposeActivity: AbstractComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeProjectTheme {
                ContentPage()
            }
        }
    }

    @Composable
    abstract fun ContentPage()
}