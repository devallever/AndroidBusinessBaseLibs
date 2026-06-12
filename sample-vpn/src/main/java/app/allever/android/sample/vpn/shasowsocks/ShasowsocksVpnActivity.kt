package app.allever.android.sample.vpn.shasowsocks

import android.annotation.SuppressLint
import android.os.Build
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.vpn.databinding.ActivityShadowsocksVpnBinding
import com.github.shadowsocks.Core
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.log

class ShasowsocksVpnActivity: BaseActivity<ActivityShadowsocksVpnBinding, BaseViewModel>() {
    override fun inflateChildBinding(): ActivityShadowsocksVpnBinding = ActivityShadowsocksVpnBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("Shadowsocks VPN")
        ShadowsocksHelper.adaptSdk33Notification(this)
        initObserver()
        ShadowsocksHelper.init()
        binding.apply {
            btnSelect.setOnClickListener {
                ShadowsocksHelper.switchNode()
            }
            btnConnect.setOnClickListener {
                when (DataStore.serviceState) {
                    BaseService.State.Stopped, BaseService.State.Idle -> {
                        //Connect
                        ShadowsocksHelper.getSelectedNodeItem()?.entity?.let {
                            Core.startService()
                        }
                    }
                    else -> {}
                }
            }
            btnDisConnect.setOnClickListener {
                when (DataStore.serviceState) {

                    BaseService.State.Connected -> {
                        Core.stopService()
                        ShadowsocksHelper.resetConnectTime()
                    }
                    else -> {
                    }
                }
            }
            btnAddTime.setOnClickListener {
                ShadowsocksHelper.appendConnectTime()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        ShadowsocksHelper.currentNodeLiveData.observe(this) {
            log("nodeChange: ${it?.nn}")
            binding.tvNode.text = "Node: ${it?.entity?.host?:""}"
        }

        ShadowsocksHelper.remainTimeLiveData.observe(this) {
            if (it >= 0L) {
                binding.tvTimer.text = TimeHelper.formatTimeStampToHMS(it)
            }
        }

        ShadowsocksHelper.serviceStateLiveData.collect(this) {
            binding.tvState.text = "State: $it"
            onReceiveServerStateChanged(it)
        }
    }

    private fun onReceiveServerStateChanged(
        state: BaseService.State
    ) {
        if (state == BaseService.State.Connected) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationHelper.cancelNotification()
            }
        } else if (state == BaseService.State.Stopped) {

        }
    }

}