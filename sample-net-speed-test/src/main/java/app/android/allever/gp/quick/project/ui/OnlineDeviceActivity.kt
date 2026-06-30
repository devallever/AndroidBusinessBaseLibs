package app.android.allever.gp.quick.project.ui

import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityOnlineDeviceBinding
import app.android.allever.gp.quick.project.util.IPHelper

class OnlineDeviceActivity: AppActivity<ActivityOnlineDeviceBinding, BaseViewModel>() {
    override fun inflate() = ActivityOnlineDeviceBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener { finish() }
            tvIp.text = IPHelper.getInternalIp()
        }
    }
}