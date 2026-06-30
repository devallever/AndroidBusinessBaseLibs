package app.android.allever.gp.quick.project.ui

import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityVideoSpeedBinding

class VideoSpeedActivity: AppActivity<ActivityVideoSpeedBinding, BaseViewModel>() {
    override fun inflate() = ActivityVideoSpeedBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener {
                finish()
            }
        }
    }
}