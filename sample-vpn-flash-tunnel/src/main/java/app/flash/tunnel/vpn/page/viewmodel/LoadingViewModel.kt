package app.flash.tunnel.vpn.page.viewmodel

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.helper.EventHelper
import app.flash.tunnel.vpn.helper.LogScene
import app.flash.tunnel.vpn.helper.NotificationHelper
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.admob.AdCallback
import app.flash.tunnel.vpn.lib.common.base.AbsViewModel
import app.flash.tunnel.vpn.lib.common.ext.collect
import app.flash.tunnel.vpn.lib.common.util.ActivityManager
import app.flash.tunnel.vpn.lib.common.util.MultiStepProgress
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.page.DisconnectActivity
import app.flash.tunnel.vpn.page.LoadingActivity
import app.flash.tunnel.vpn.page.ResultActivity
import com.github.shadowsocks.Core
import com.github.shadowsocks.bg.BaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LoadingViewModel : AbsViewModel() {
    fun initLoadingType(activity: LoadingActivity, adCallback: AdCallback) {
        when (mType) {
            LoadingActivity.LOADING_CONNECT -> {
                initConnectTypeLoading(activity, adCallback)
            }

            LoadingActivity.LOADING_DISCONNECT -> {
                AdHelper.loadDisconnectInter(activity, adCallback)
            }

            LoadingActivity.LOADING_REWARD -> {
                AdHelper.loadReward(activity, adCallback = adCallback)
            }

            LoadingActivity.LOADING_RETURN_APP -> {
                AdHelper.loadReturnAppInter(activity, adCallback)
            }
        }
    }

    fun handleActivityStop(activity: LoadingActivity) {
        if (!TunnelApp.currentInBackground()) {
            return
        }

        if (mType == LoadingActivity.LOADING_CONNECT) {
            Core.stopService()
            NotificationHelper.showNotification(
                activity.getString(R.string.connecting_when_app_in_bg)
            )
            val usedTime = System.currentTimeMillis() - EventHelper.ssTimeStart
            EventHelper.logConnectingEnterBg(usedTime)
        }
        activity.finish()
    }

    private fun initConnectTypeLoading(
        activity: AppCompatActivity, adCallback: AdCallback
    ) {
        if (TunnelHelper.isServiceConnected()) {
            loadAfterConnectInter(activity, adCallback, "connected")
        } else {
            if (!TunnelHelper.dataReady()) {
                TunnelHelper.nodeListLiveData.observe(activity) {
                    if (it.isNotEmpty()) {
                        Core.startService()
                    }
                }
                viewModelScope.launch {
                    TunnelHelper.fetchNodeList(fail = {
                        multiStepProgress.finish()
                    })
                }
            }
            if (TunnelHelper.dataReady()) {
                viewModelScope.launch {
                    delay(Constants.CONNECT_TIME_OUT)
                    if (!TunnelHelper.isServiceConnected() && isActive) {
                        multiStepProgress.finish()
                    }
                }
            }
            TunnelHelper.connectFailFlow.collect(activity) {
                if (it && TunnelHelper.dataReady()) {
                    activity.setResult(RESULT_OK, Intent().apply {
                        putExtra(Constants.EXTRA_SHOW_CONNECT_FAIL, true)
                    })
                    multiStepProgress.finish()
                }
            }
            TunnelHelper.serviceStateLiveData.collect(activity) {
                if (it == BaseService.State.Connected) {
                    viewModelScope.launch {
                        delay(1000)
                        //fix state == connected but check isConnect = false
                        loadAfterConnectInter(activity, adCallback,"observer connected")
                    }
                }
            }
        }
    }

    private fun startSuccessPage(context: Context, scene: String = LogScene.DEFAULT) {
        log("startSuccessPage: $scene")
        ActivityManager.start(context, ResultActivity::class.java)
    }

    private fun startDisconnectPage(context: Context, scene: String = LogScene.DEFAULT) {
        //log("startDisconnectPage: $scene")
        ActivityManager.start(context, DisconnectActivity::class.java)
    }

    fun handleFinish(context: Activity, scene: String = LogScene.DEFAULT) {
        log("handleFinish: $scene")
        when (mType) {
            LoadingActivity.LOADING_CONNECT -> {
                if (TunnelHelper.isServiceConnected()) {
                    startSuccessPage(context, scene)
                } else {
                    log("handleFinish: putExtra ")
                    context.setResult(RESULT_OK, Intent().apply {
                        putExtra(Constants.EXTRA_SHOW_CONNECT_FAIL, true)
                    })
                }
            }

            LoadingActivity.LOADING_DISCONNECT -> {
                startDisconnectPage(context, scene)
            }

            LoadingActivity.LOADING_REWARD -> {
                //add connect time
                if (mRewardSuccess) {
                    TunnelHelper.appendConnectTime()
                    context.setResult(RESULT_OK, Intent().apply {
                        putExtra(Constants.EXTRA_SHOW_ADD_TIME_SUCCESS_DIALOG, true)
                    })
                } else {
                    context.setResult(RESULT_OK, Intent().apply {
                        putExtra(Constants.EXTRA_SHOW_ADD_TIME_FAIL_DIALOG, true)
                    })
                }
            }

            LoadingActivity.LOADING_RETURN_APP -> {

            }
        }
    }

    fun handleAdShow() {
        mAdShow = true
        multiStepProgress.finish()
    }

    private fun loadAfterConnectInter(activity: ComponentActivity, adCallback: AdCallback, scene: String = "Default") {
        log("loadAfterConnectInter: $scene")
        AdHelper.loadConnectSuccessInter(activity, adCallback)
    }

    val DEFAULT_DELAY = Constants.LOADING_DELAY_DEFAULT
    val CONNECT_DELAY = Constants.LOADING_DELAY_CONNECT
    var mProgressDuration = DEFAULT_DELAY

    val multiStepProgress by lazy {
        MultiStepProgress().apply {
            duration = mProgressDuration
            listSpeed.add(MultiStepProgress.Speed(duration, 0.71, 0.2))
            listSpeed.add(MultiStepProgress.Speed(duration, 0.26, 0.3))
            listSpeed.add(MultiStepProgress.Speed(duration, 0.03, 0.5))
        }
    }

    var mType = LoadingActivity.LOADING_DEFAULT
    var mAdShow = false

    var mRewardSuccess = false

    var mEnterPageStartTime = 0L


}