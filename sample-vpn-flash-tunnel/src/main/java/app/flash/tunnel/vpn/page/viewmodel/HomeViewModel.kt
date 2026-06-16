package app.flash.tunnel.vpn.page.viewmodel

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import app.flash.tunnel.vpn.helper.EventHelper
import app.flash.tunnel.vpn.helper.LogScene
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.lib.common.base.AbsViewModel
import app.flash.tunnel.vpn.lib.common.util.PollingTask
import app.flash.tunnel.vpn.lib.common.util.StoreManager
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.lib.common.util.toast
import app.flash.tunnel.vpn.page.LoadingActivity
import com.github.shadowsocks.Core
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : AbsViewModel() {
    lateinit var vpnRequestLauncher: ActivityResultLauncher<Void?>

    var mConnectTask: PollingTask? = null

    private var agreeVpnPermission = false

    fun handleClickConnect(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        scene: String = LogScene.DEFAULT,
        isSwitchNode: Boolean = false,
        connectFailTask: () -> Unit = {}
    ) {
        log("handleClickConnect: $scene")


        if (TunnelHelper.isServiceConnecting()) {
            toast("VPN is connecting")
            return
        }

        if (!agreeVpnPermission) {
            log("launch vpn on !mAgreeVpnPermission")
            vpnRequestLauncher.launch(null)
            return
        }

        TunnelHelper.launchLoading(context, LoadingActivity.LOADING_CONNECT)

        viewModelScope.launch {
            launch(Dispatchers.IO) {

                if (TunnelHelper.isSmartMode()) {
                    TunnelHelper.updateSmartModeItem()
                }

                TunnelHelper.getSelectedNodeItem()?.entity?.let {
                    Core.startService()
                }
            }
        }
    }

    fun handleVpnPermission(rejectVpnPermission: Boolean, connectAction: () -> Unit) {
        if (rejectVpnPermission) {
            //reject permission
            log("registerForActivityResult: reject permission")
            agreeVpnPermission = false
        } else {
            //agree permission
            log("registerForActivityResult: agree permission")
            //first agree
            if (!agreeVpnPermission) {
                agreeVpnPermission = true
                //for kill from running task then open app
                if (TunnelHelper.isServiceConnected()) {
                    return
                }
                if (StoreManager.getBoolean("firstAgree", true)) {
                    EventHelper.logAgreeVpnPermission(EventHelper.evsValue)
                    StoreManager.putBoolean("firstAgree", false)
                }
                connectAction.invoke()
                return
            }

            agreeVpnPermission = true
        }
    }

}