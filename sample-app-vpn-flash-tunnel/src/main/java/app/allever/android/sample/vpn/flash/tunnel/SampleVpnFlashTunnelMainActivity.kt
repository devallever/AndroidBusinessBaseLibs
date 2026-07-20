package app.allever.android.sample.vpn.flash.tunnel

import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.ListActivity
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.ActivityListBinding
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.ActivityHelper
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.page.HomeActivity
import app.flash.tunnel.vpn.page.SplashActivity
import com.therouter.router.Route
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.coroutines.launch

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

    override fun init() {
        TunnelApp.initTunnelApp()
        super.init()
        lifecycleScope.launch {
            TunnelHelper.init(App.context)
        }
    }

}