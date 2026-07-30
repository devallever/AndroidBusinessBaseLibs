package app.allever.android.lib.common.compose

import androidx.compose.runtime.Composable
import app.allever.android.lib.common.compose.widget.FunctionList
import app.allever.android.lib.common.compose.widget.TextClickItem

abstract class ListComposeActivity: BaseComposeActivity() {
    @Composable
    override fun ContentPage() {
        FunctionList(getList())
    }

    override fun init() {
        initTopBar(getPageTitle(), true)
    }

    abstract fun getPageTitle(): String
    abstract fun getList(): MutableList<TextClickItem>
}