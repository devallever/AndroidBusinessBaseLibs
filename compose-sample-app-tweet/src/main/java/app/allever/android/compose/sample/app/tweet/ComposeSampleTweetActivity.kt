package app.allever.android.compose.sample.app.tweet

import app.allever.android.lib.common.compose.ListComposeActivity
import app.allever.android.lib.common.compose.data.TextClickItem
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.widget.TextClickAdapter
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.router.annotation.Route

@Route(path = "/composetweet/main")
class ComposeSampleTweetActivity: ListComposeActivity<TextClickItem>() {
    override fun getPageTitle(): String = "Tweet"

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("登录") {
            toast("登录")
        },
        TextClickItem("注册") {
            toast("注册")
        },
    )

    override fun getLayoutAdapter(): LayoutAdapter<TextClickItem> = TextClickAdapter()
}