package app.android.allever.gp.quick.project.ui

import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.base.AppActivity
import app.android.allever.gp.quick.project.databinding.ActivityGameSpeedBinding

class GameSpeedActivity: AppActivity<ActivityGameSpeedBinding, BaseViewModel>() {
    override fun inflate() = ActivityGameSpeedBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            adaptStatusBar(topBar)
            ivBack.setOnClickListener {
                finish()
            }
        }
    }
}