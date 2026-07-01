package app.flash.tunnel.vpn.page

import app.flash.tunnel.vpn.databinding.ActivityDisconnectBinding
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.common.util.TimeManager
import com.github.shadowsocks.Core

class DisconnectActivity : BaseActivity<ActivityDisconnectBinding>() {

    override fun inflate() = ActivityDisconnectBinding.inflate(layoutInflater)

    override fun init() {
        fixStatusBar(mBinding.topBar)
        mBinding.ivClose.setOnClickListener { finish() }

        mBinding.tvTime.text = TimeManager.formatTimeStampToHMS(TunnelHelper.getConnectDuration())

        TunnelHelper.loadRegionsFlag(
            mBinding.ivFlag,
            TunnelHelper.getConnectedNodeItem()?.cc ?: return
        )

        AdHelper.loadDisconnectNative(mBinding.adContainer)
    }

    override fun onDestroy() {
        super.onDestroy()
        AdHelper.destroyNative(mBinding.adContainer)
        Core.stopService()
        TunnelHelper.resetConnectTime()
    }
}