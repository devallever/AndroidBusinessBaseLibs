package app.allever.android.compose.sample.app.green.vpn

import app.allever.android.lib.common.compose.ListComposeActivity
import app.allever.android.lib.common.compose.data.TextClickItem
import app.allever.android.lib.common.compose.widget.LayoutAdapter
import app.allever.android.lib.common.compose.widget.TextClickAdapter
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.router.annotation.Route
import com.allever.compose.green.vpn.LoadingActivity
import com.allever.compose.green.vpn.MainActivity

@Route(path = "/composegreenvpn/main")
class ComposeSampleGreenVpnActivity: ListComposeActivity<TextClickItem>() {
    override fun getPageTitle(): String = "Green VPN"

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页") {
            ActivityHelper.startActivity<LoadingActivity>()
        },
        TextClickItem("主页") {
            ActivityHelper.startActivity<MainActivity>()
        },
    )

    override fun getLayoutAdapter(): LayoutAdapter<TextClickItem> = TextClickAdapter()
}