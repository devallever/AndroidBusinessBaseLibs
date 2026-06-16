package app.flash.tunnel.vpn.page

import android.app.Dialog
import android.content.Intent
import androidx.activity.viewModels
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.databinding.ActivityResultBinding
import app.flash.tunnel.vpn.helper.TunnelHelper
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.common.util.TimeManager
import app.flash.tunnel.vpn.lib.common.util.toast
import app.flash.tunnel.vpn.page.dialog.DialogHelper
import app.flash.tunnel.vpn.page.viewmodel.ResultViewModel

class ResultActivity : BaseActivity<ActivityResultBinding>() {

    private var mAddTimeSuccessDialog: Dialog? = null
    private var mAddTimeFailDialog: Dialog? = null

    private val mWatchAdDialog by lazy {
        DialogHelper.obtainAddTimeDialog(this) {
            mViewModel.handleClickAddTimeDiaogConfirm(this)
        }
    }

    private val mViewModel by viewModels<ResultViewModel>()

    override fun inflate() = ActivityResultBinding.inflate(layoutInflater)

    override fun init() {
        fixStatusBar(mBinding.topBar)
        mBinding.ivClose.setOnClickListener { finish() }

        TunnelHelper.getConnectedNodeItem()?.let {
            TunnelHelper.loadRegionsFlag(mBinding.ivFlag, it.cc)
        }

        TunnelHelper.remainTimeLiveData.observe(this) {
            mBinding.tvTime.text = TimeManager.formatTimeStampToHMS(it)
        }

        mBinding.apply {
            btnAddTime.setOnClickListener {
                if (TunnelHelper.isServiceConnected()) {
                    showAddTimeDialog()
                } else {
                    toast("connect time is over!")
                }

            }
        }

        AdHelper.loadConnectSuccessNative(mBinding.adContainer)
    }


    private fun showAddTimeDialog() {
        mWatchAdDialog.show()
    }

    private fun showAddTimeFailDialog() {
        //can't lazy init
        if (mAddTimeFailDialog == null) {
            mAddTimeFailDialog = DialogHelper.obtainAdLoadFailDialog(this) {
                mViewModel.handleClickAddTimeDiaogConfirm(this) {
                    showAddTimeSuccessDialog()
                }
            }
        }
        mAddTimeFailDialog?.show()
    }

    private fun showAddTimeSuccessDialog() {
        if (mAddTimeSuccessDialog == null) {
            mAddTimeSuccessDialog = DialogHelper.obtainAddTimeSuccessDialog(this)
        }
        mAddTimeSuccessDialog?.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        AdHelper.destroyNative(mBinding.adContainer)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (data.getBooleanExtra(Constants.EXTRA_REWARD_CANCEL, false)) {
                mViewModel.clickWatchAdFlag = false
                toast("add time fail")
            }
            if (data.getBooleanExtra(Constants.EXTRA_SHOW_ADD_TIME_SUCCESS_DIALOG, false)) {
                mAddTimeFailDialog?.dismiss()
                showAddTimeSuccessDialog()
            }
            if (data.getBooleanExtra(Constants.EXTRA_SHOW_ADD_TIME_FAIL_DIALOG, false)) {
                mAddTimeSuccessDialog?.dismiss()
                showAddTimeFailDialog()
            }
        }
    }
}