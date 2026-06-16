package app.flash.tunnel.vpn.page.dialog

import android.app.Activity
import android.app.Dialog
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.TunnelApp
import com.github.shadowsocks.Core

object DialogHelper {
    fun obtainConnectFailDialog(activity: Activity, confirm: () -> Unit = {}): Dialog {
        return AppDialog(activity).apply {
            message = TunnelApp.context.getString(R.string.dialog_connect_fail_message)
            icon = R.drawable.icon_dialog_connect_fail
            confirmText = TunnelApp.context.getString(R.string.reconnect)
            callback = {
                it.dismiss()
                confirm.invoke()
            }
        }
    }

    fun obtainAdLoadFailDialog(activity: Activity, block: () -> Unit): Dialog {
        return AppDialog(activity).apply {
            message = TunnelApp.context.getString(R.string.dialog_ad_load_fail_message)
            icon = R.drawable.icon_dialog_add_time_fail
            confirmText = TunnelApp.context.getString(R.string.retry)
            callback = {
                it.dismiss()
                block.invoke()
            }
        }
    }

    fun obtainConnectFinishDialog(activity: Activity): Dialog {
        return AppDialog(activity).apply {
            message = TunnelApp.context.getString(R.string.dialog_conect_finish_message)
            icon = R.drawable.icon_dialog_time_over
            showClose = false
            confirmText = TunnelApp.context.getString(R.string.got_it)
            callback = {
                it.dismiss()
            }
            setOnShowListener {
                Core.cancelAutoNotification()
            }
        }
    }

    fun obtainAddTimeSuccessDialog(activity: Activity): Dialog {
        return AppDialog(activity).apply {
            message = TunnelApp.context.getString(R.string.dialog_add_time_success_message)
            icon = R.drawable.icon_dialog_add_time_success
            confirmText = TunnelApp.context.getString(R.string.got_it)
            showClose = false
            callback = {
                it.dismiss()
            }
        }
    }

    fun obtainRateDialog(activity: Activity): Dialog {
        return RateForUsDialog(activity)
    }

    fun obtainAddTimeDialog(activity: Activity, confirm: () -> Unit = {}): Dialog {
        val dialog = RewardTipsDialog(activity) {
            confirm.invoke()
            it.dismiss()
        }
        return dialog
    }

}