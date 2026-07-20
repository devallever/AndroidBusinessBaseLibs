package app.allever.android.sample.vpn

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.sample.vpn.shasowsocks.ShasowsocksVpnActivity
import com.therouter.router.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/vpn/main")
class VPNSampleMainActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "VPN"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("VPN-Android") {
        },
        TextClickItem("VPN-Shadowsocks") {
            ActivityHelper.startActivity<ShasowsocksVpnActivity>()
        }
    )
}