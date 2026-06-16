package app.allever.android.sample.vpn.flash.tunnel

import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.helper.ActivityHelper
import app.flash.tunnel.vpn.page.HomeActivity
import app.flash.tunnel.vpn.page.SplashActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.chad.library.adapter.base.BaseQuickAdapter

@Route(path = "/flashtunnel/main")
class SampleVpnFlashTunnelMainActivity: ListActivity<ActivityListBinding, ListViewModel, TextClickItem>() {
    override fun getPageTitle(): String = "Sample VPN Flash Tunnel"

    override fun getAdapter(): BaseQuickAdapter<TextClickItem, *> = TextClickAdapter()

    override fun getList(): MutableList<TextClickItem> = mutableListOf(
        TextClickItem("启动页") {
            ActivityHelper.startActivity<SplashActivity>()
        },
        TextClickItem("主页") {
            ActivityHelper.startActivity<HomeActivity>()
        }
    )
}