package app.flash.tunnel.vpn.page

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.helper.EventHelper
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.common.base.AbsBindingActivity
import app.flash.tunnel.vpn.lib.common.base.AbsViewModel
import app.flash.tunnel.vpn.lib.common.store.dataStore
import app.flash.tunnel.vpn.lib.common.util.StatusBarManager
import app.flash.tunnel.vpn.page.dialog.DialogHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AbsBindingActivity<VB>() {

    private val mViewModel by viewModels<AbsViewModel>()

    private var mConnectFinishDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            dataStore.data.first()
        }
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        StatusBarManager.setDarkStatusBar(this)
        super.onCreate(savedInstanceState)

        if (this is LoadingActivity) {
            return
        }
        if (this is SplashActivity) {
            return
        }

        //case: exit app self, then reopen
        checkAndShowConnectFinisDialog()
        //case: in app
        TunnelHelper.remainTimeLiveData.observe(this) {
            //in background can't invoke this
            if (it < 0L) {
                showConnectFinishDialog()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        //this !is SplashActivity because: home cover loadingActivity
        if (!TunnelApp.alreadyInBackground || this is SplashActivity) {
            return
        }
        EventHelper.logReturnApp()
        if (TunnelHelper.isServiceStopped()) {
            return
        }

        TunnelApp.alreadyInBackground = false
        if (AdHelper.hasInterAdCache()) {
            EventHelper.logShowInterAd(EventHelper.AdPositionValue.RETURN_APP_INTER, 0)
            AdHelper.showInterAdCache(this)
        } else {
            LoadingActivity.launch(
                this,
                LoadingActivity.LOADING_RETURN_APP
            )
        }


    }

    override fun onRestart() {
        super.onRestart()
        checkAndShowConnectFinisDialog()
    }

    private fun checkAndShowConnectFinisDialog() {
        //case: click notification
        if (TunnelHelper.connectAutoStop) {
            TunnelHelper.connectAutoStop = false
            showConnectFinishDialog()
        }
    }

    protected fun fixStatusBar(view: View) {
        view.post {
            val lp = view.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = StatusBarManager.getStatusBarHeight(this)
            view.layoutParams = lp
        }
    }

//    protected fun initTopBar(topBar: View, title: String) {
//        findViewById<View>(R.id.ivBack)?.setOnClickListener {
//            finish()
//        }
//        fixStatusBar(topBar)
//        val tvTitle = findViewById<TextView>(R.id.tvTitle)
//        tvTitle?.text = title
//    }

    private fun showConnectFinishDialog() {
        if (mConnectFinishDialog == null) {
            mConnectFinishDialog = DialogHelper.obtainConnectFinishDialog(this)
        }
        mConnectFinishDialog?.show()
    }
}