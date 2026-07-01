package app.flash.tunnel.vpn.page.viewmodel

import android.app.Activity
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.admob.AdCallback
import app.flash.tunnel.vpn.lib.common.base.AbsViewModel
import app.flash.tunnel.vpn.lib.common.util.toast
import app.flash.tunnel.vpn.page.LoadingActivity

class ResultViewModel : AbsViewModel() {
    var clickWatchAdFlag = false

    private var mRewardSuccess = false
    fun checkCanShowAddTimeResult() = TunnelHelper.isServiceConnected() && clickWatchAdFlag

    fun handleClickAddTimeDiaogConfirm(context: Activity, successCallback: () -> Unit = {}) {
        handleClickWatchAD(context, successCallback)
    }

    private fun handleClickWatchAD(context: Activity, successCallback: () -> Unit = {}) {
        clickWatchAdFlag = true
        mRewardSuccess = false
        if (AdHelper.hasRewardAdCache()) {
            AdHelper.showRewardAdCache(context, object : AdCallback {
                override fun onDismiss() {
                    if (mRewardSuccess) {
                        TunnelHelper.appendConnectTime()

                    } else {
                        toast("add time fail")
                    }
                }

                override fun onShowFailed(code: Int, err: String) {
                    LoadingActivity.launch(context, LoadingActivity.LOADING_REWARD)
                }

                override fun onRewarded() {
                    mRewardSuccess = true
                }
            })
        } else {
            LoadingActivity.launch(context, LoadingActivity.LOADING_REWARD)
        }
    }
}