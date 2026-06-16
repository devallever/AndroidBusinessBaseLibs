package app.flash.tunnel.vpn.page

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.Formatter
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.databinding.ActivityHomeBinding
import app.flash.tunnel.vpn.helper.EventHelper
import app.flash.tunnel.vpn.helper.LogScene
import app.flash.tunnel.vpn.helper.NotificationHelper
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.admob.AdCallback
import app.flash.tunnel.vpn.lib.common.ext.collect
import app.flash.tunnel.vpn.lib.common.ext.loadCircle
import app.flash.tunnel.vpn.lib.common.util.ActivityManager
import app.flash.tunnel.vpn.lib.common.util.PollingTask
import app.flash.tunnel.vpn.lib.common.util.TimeManager
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.lib.common.util.toast
import app.flash.tunnel.vpn.page.dialog.DialogHelper
import app.flash.tunnel.vpn.page.viewmodel.HomeViewModel
import com.github.shadowsocks.Core
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.StartService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    private val mViewModel by viewModels<HomeViewModel>()

    private var mConnectFailDialog: Dialog? = null
    private var mAddTimeSuccessDialog: Dialog? = null
    private var mAddTimeFailDialog: Dialog? = null
    private var mWatchAdDialog: Dialog? = null

    private var mClickAddTimeFlag = false

    override fun inflate() = ActivityHomeBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Core.cancelAutoNotification()
    }

    override fun init() {
        fixStatusBar(mBinding.topBar)

        showStatusUi(TunnelHelper.isServiceConnected())

        TunnelHelper.adaptSdk33Notification(this)

        initObserver()

        initListener()

        AdHelper.loadHomeNative(mBinding.bannerContainer)

        mViewModel.vpnRequestLauncher =
            registerForActivityResult(StartService()) { rejectPermission ->
                mViewModel.handleVpnPermission(rejectPermission) {
                    handleClickConnect(LogScene.FIRST_AGREE_VPN_PERMISSION)
                }
            }

    }

    override fun onResume() {
        super.onResume()
        AdHelper.resumeBanner(mBinding.bannerContainer)
        if (EventHelper.logEnterMain) {
            return
        }
        val usedTime = System.currentTimeMillis() - EventHelper.launchTimeStart
        EventHelper.logFirstEnterHome(usedTime)
        EventHelper.logEnterMain = true
    }

    override fun onPause() {
        super.onPause()
        AdHelper.pauseBanner(mBinding.bannerContainer)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log("onActivityResult: ")
        data?.let {
            handleActivityResult(data, showConnectFailAction = {
                showConnectFailDialog(LogScene.TIME_OUT)
            }, connectAction = {
                handleClickConnect(LogScene.SWITCH_NODE_BY_LIST, true)
            })

            if (it.getBooleanExtra(Constants.EXTRA_REWARD_CANCEL, false)) {
                mClickAddTimeFlag = false
                log("onActivityResult: getBooleanExtra EXTRA_REWARD_CANCEL")
                toast("add time fail")
            }

            if (data.getBooleanExtra(Constants.EXTRA_SHOW_ADD_TIME_SUCCESS_DIALOG, false)) {
                showAddTimeSuccessDialog()
            }
            if (data.getBooleanExtra(Constants.EXTRA_SHOW_ADD_TIME_FAIL_DIALOG, false)) {
                showAddTimeFailDialog()
            }
        }
    }

    private fun initObserver() {
        TunnelHelper.speedLiveData.observe(this) {
            mBinding.apply {
                try {
                    mBinding.apply {
                        val downloadDisplay =
                            Formatter.formatFileSize(this@HomeActivity, it?.rxRate ?: 0)
                        val downloadSpeed = downloadDisplay.split(" ")[0]
                        val downloadUnit = "${downloadDisplay.split(" ")[1]}/s"
                        val uploadDisplay =
                            Formatter.formatFileSize(this@HomeActivity, it?.txRate ?: 0)
                        val uploadSpeed = uploadDisplay.split(" ")[0]
                        val uploadUnit = "${uploadDisplay.split(" ")[1]}/s"
                        tvDownloadSpeed.text = downloadSpeed
                        tvDownloadUnit.text = downloadUnit
                        tvUploadSpeed.text = uploadSpeed
                        tvUploadUnit.text = uploadUnit
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    tvDownloadSpeed.text = "0"
                    tvDownloadUnit.text = "B/s"
                    tvUploadSpeed.text = "0"
                    tvUploadUnit.text = "B/s"
                }
            }
        }

        TunnelHelper.nodeListLiveData.observe(this) {
            updateConnectedNodeUi(LogScene.NODE_LIST_LIVEDATA)
        }
        TunnelHelper.alreadyAppendLiveData.observe(this) {
            if (it) {
                mAddTimeFailDialog?.dismiss()
                mAddTimeSuccessDialog?.dismiss()
            }
        }

        TunnelHelper.currentNodeLiveData.observe(this) {
            updateConnectedNodeUi(LogScene.CURRENT_NODE_LIVEDATA)
        }

        TunnelHelper.remainTimeLiveData.observe(this) {
            if (it >= 0L) {
                mBinding.tvTime.text = TimeManager.formatTimeStampToHMS(it)
            }
        }

        TunnelHelper.serviceStateLiveData.collect(this) {
            log("receive serviceState: ${it.name}")
            onReceiveServerStateChanged(it)
        }
    }

    private fun initListener() {
        mBinding.btnConnect.setOnClickListener {
            handleTapToConnectOrDisconnect()
        }
        mBinding.ivConnect.setOnClickListener {
            handleTapToConnectOrDisconnect()
        }

        mBinding.btnAddTime.setOnClickListener {
            showAddTimeDialog()
        }

        mBinding.ivFlag.setOnClickListener {
            handleClickNode()
        }
        mBinding.btnBottomNode.setOnClickListener {
            handleClickNode()
        }

        mBinding.ivMenu.setOnClickListener {
            ActivityManager.start(this@HomeActivity, SettingActivity::class.java)
        }
    }

    private fun handleTapToConnectOrDisconnect() {
        when (DataStore.serviceState) {
            BaseService.State.Stopped, BaseService.State.Idle -> {
                //Connect
                handleClickConnect(LogScene.BTN_CONNECT)
            }

            BaseService.State.Connected -> {

                lifecycleScope.launch {
                    //stop from summary
                    //
                    if (AdHelper.hasInterAdCache()) {
                        EventHelper.logShowInterAd(EventHelper.AdPositionValue.DISCONNECT_INTER, 0)
                        AdHelper.showInterAdCache(
                            this@HomeActivity,
                            adCallback = object : AdCallback {
                                override fun onDismiss() {
                                    ActivityManager.start(
                                        this@HomeActivity,
                                        DisconnectActivity::class.java
                                    )
                                }

                                override fun onShowFailed(code: Int, err: String) {
                                    LoadingActivity.launch(
                                        this@HomeActivity,
                                        LoadingActivity.LOADING_DISCONNECT
                                    )
                                }
                            })
                    } else {
                        LoadingActivity.launch(
                            this@HomeActivity,
                            LoadingActivity.LOADING_DISCONNECT
                        )
                    }

                }
            }

            else -> {

            }
        }
    }

    private fun handleAddTimeLiveData(success: Boolean) {
        if (TunnelHelper.isServiceConnected()) {
            //bugfix: show addTimeDialog when enter home from notification
            if (!mClickAddTimeFlag) {
                return
            }
            if (success) {
                mAddTimeFailDialog?.dismiss()
                if (mClickAddTimeFlag) {
                    showAddTimeSuccessDialog()
                }
            } else {
                mAddTimeSuccessDialog?.dismiss()
                if (mClickAddTimeFlag) {
                    showAddTimeFailDialog()
                }
            }
        }
        mClickAddTimeFlag = false
    }

    private fun handleClickConnect(
        scene: String = LogScene.DEFAULT,
        isSwitchNode: Boolean = false
    ) {
        EventHelper.ssTimeStart = System.currentTimeMillis()
        EventHelper.evsValue = if (isSwitchNode) {
            EventHelper.EvsValue.LIST
        } else {
            EventHelper.EvsValue.HOME
        }

        if (scene != LogScene.FIRST_AGREE_VPN_PERMISSION) {
            EventHelper.logClickConnect()
        }
        mViewModel.handleClickConnect(this, this, scene, isSwitchNode) {
            showConnectFailDialog(LogScene.PING_FAIL)
        }
    }

    private fun handleClickNode() {
        ActivityManager.start(this, ListActivity::class.java)
    }

    private fun showDefaultNodeUi() {
        mBinding.tvNodeName.text = getString(R.string.default_server)
        mBinding.ivFlagBottom.loadCircle(R.drawable.icon_default_region)
        mBinding.ivFlag.loadCircle(R.drawable.icon_default_region)
    }

    private fun updateConnectedNodeUi(scene: String = LogScene.DEFAULT) {
        log("updateConnectedNodeUi: $scene")
        val isSmartMode = TunnelHelper.isSmartMode()

        if (TunnelHelper.isServiceConnected()) {
            //update top
            val node = TunnelHelper.getConnectedNodeItem()
            if (node == null) {
                showDefaultNodeUi()
                return
            }
            node.let {
                TunnelHelper.loadRegionsFlag(mBinding.ivFlag, it.cc)
            }
        } else {
            //update bottom
            val node = TunnelHelper.getSelectedNodeItem()
            log("updateConnectedNodeUi: isSmartNode -> $isSmartMode")
            log("updateConnectedNodeUi: node == null -> ${node == null}")
            if (node == null || isSmartMode) {
                showDefaultNodeUi()
            } else {
                node.let {
                    TunnelHelper.loadRegionsFlag(mBinding.ivFlagBottom, it.cc)
                    TunnelHelper.loadRegionsFlag(mBinding.ivFlag, it.cc)
                    mBinding.tvNodeName.text = it.nn
                }
            }
        }
    }

    private fun showStatusUi(connected: Boolean) {
        mBinding.apply {
            downloadContainer.isVisible = connected
            uploadContainer.isVisible = connected
            tvTime.isVisible = connected
            btnAddTime.isVisible = connected
            btnBottomNode.isVisible = !connected

            if (connected) {
                tvBtnConnect.text = getString(R.string.disconnect)
                tvTapTo.text = getString(R.string.tap_to_disconnect)
                ivConnect.setImageResource(R.drawable.icon_logo)
            } else {
                tvBtnConnect.text = getString(R.string.go_start_connect)
                tvTapTo.text = getString(R.string.tap_to_connect)
                ivConnect.setImageResource(R.drawable.icon_logo_gray)
            }
        }
    }

    private fun showConnectedUi() {
        showStatusUi(true)
    }

    private fun showDisconnectUi() {
        showStatusUi(false)
    }

    private var mRewardSuccess = false
    private fun showAddTimeDialog() {
        if (mWatchAdDialog == null) {
            mWatchAdDialog = DialogHelper.obtainAddTimeDialog(this) {
                mClickAddTimeFlag = true
                handleWathchAd()
            }

            mWatchAdDialog?.setOnDismissListener {
//                mClickAddTimeFlag = false
            }
        }
        mWatchAdDialog?.show()
    }

    private fun showAddTimeFailDialog() {
        if (mAddTimeFailDialog == null) {
            mAddTimeFailDialog = DialogHelper.obtainAdLoadFailDialog(this) {
                mClickAddTimeFlag = true
                handleWathchAd()
            }
        }
        mAddTimeFailDialog?.show()
    }

    private fun handleWathchAd() {
        mRewardSuccess = false
        if (AdHelper.hasRewardAdCache()) {
            AdHelper.showRewardAdCache(this, object : AdCallback {
                override fun onDismiss() {
                    if (mRewardSuccess) {
                        TunnelHelper.appendConnectTime()
                        showAddTimeSuccessDialog()
                    } else {
                        toast("add time fail")
                    }
                }

                override fun onRewarded() {
                    mRewardSuccess = true
                }

                override fun onShowFailed(code: Int, err: String) {
                    LoadingActivity.launch(this@HomeActivity, LoadingActivity.LOADING_REWARD)
                }
            })
        } else {
            LoadingActivity.launch(this, LoadingActivity.LOADING_REWARD)
        }
    }

    private fun showAddTimeSuccessDialog() {
        if (mAddTimeSuccessDialog == null) {
            mAddTimeSuccessDialog = DialogHelper.obtainAddTimeSuccessDialog(this)
        }
        mAddTimeSuccessDialog?.show()
    }

    fun showConnectFailDialog(scene: String = LogScene.DEFAULT) {
        mViewModel.mConnectTask?.cancel()
        if (mConnectFailDialog == null) {
            mConnectFailDialog = DialogHelper.obtainConnectFailDialog(this) {
                //reconnect
                handleClickConnect(LogScene.RETRY)

            }
        }
        Core.stopService()
        if (mConnectFailDialog?.isShowing == true) {
            return
        }
        log("showConnectFailDialog: $scene")
        mConnectFailDialog?.show()
    }

    private fun onReceiveServerStateChanged(
        state: BaseService.State
    ) {
        log("home changeState: ${state.name}")

        updateConnectedNodeUi(LogScene.VPN_STATE_CHANGE)
        if (state == BaseService.State.Connected) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NotificationHelper.cancelNotification()
            }
            showStatusUi(true)
            AdHelper.destroyBanner(mBinding.bannerContainer)
            AdHelper.loadHomeNative(mBinding.nativeContainer)
        } else if (state == BaseService.State.Stopped) {

            launchBanner()

            AdHelper.destroyNativeCache()
            AdHelper.destroyNative(mBinding.nativeContainer)
            AdHelper.destroyInterAdCache()
            AdHelper.destroyRewardAdCache()
            showStatusUi(false)
        }
    }

    private fun launchBanner() {
        log("enter launchBanner: ${LogScene.VPN_STATE_STOP}")
        val bannerTask = PollingTask(
            this,
            interval = 1000,
            mCondition = { AdHelper.bannerId().isNotEmpty() },
            mExecute = {
                AdHelper.loadBanner(mBinding.bannerContainer, LogScene.VPN_STATE_STOP)
            })
        bannerTask.start()
    }


    private fun handleActivityResult(
        data: Intent,
        showConnectFailAction: () -> Unit,
        connectAction: () -> Unit
    ) {

        if (data.getBooleanExtra(Constants.EXTRA_SHOW_CONNECT_FAIL, false)) {
            showConnectFailAction.invoke()
        }
        if (data.getBooleanExtra(Constants.EXTRA_NEED_CHANGE_CONNECT_NODE, false)) {
            if (TunnelHelper.isServiceConnected()) {
                Core.stopService()
                lifecycleScope.launch {
                    delay(2000)
                    connectAction.invoke()
                }
            }
        }
    }

}