package com.allever.business.lib.project

import androidx.activity.enableEdgeToEdge
import app.allever.android.lib.common.BaseActivity
import app.allever.android.lib.core.helper.FragmentHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.allever.business.lib.project.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override fun init() {
        initTopBar(getString(R.string.app_name), showBackIcon = false)
        FragmentHelper.addToContainer(
            supportFragmentManager,
            MainListFragment(),
            R.id.fragmentContainer
        )
    }

    override fun inflateChildBinding() = ActivityMainBinding.inflate(layoutInflater)
}

class MainViewModel : BaseViewModel() {
    override fun init() {
    }
}