package com.allever.business.lib.project

import android.util.Log
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.cleaner.core.CleanEngine
import com.allever.business.lib.project.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override fun init() {
        initTopBar(getString(R.string.app_name), showBackIcon = false)
        FragmentHelper.addToContainer(
            supportFragmentManager,
            MainTabFragment(),
            R.id.fragmentContainer
        )
    }

    override fun inflateChildBinding() = ActivityMainBinding.inflate(layoutInflater)

    /** 退出项目界面时停止全部扫描并重置状态 */
    override fun onDestroy() {
        super.onDestroy()
        if (CleanEngine.isScanning) {
            Log.d("MainActivity", "[onDestroy] 退出项目，停止全部扫描")
            CleanEngine.stopAll()
        }
    }
}

class MainViewModel : BaseViewModel() {
    override fun init() {
    }
}