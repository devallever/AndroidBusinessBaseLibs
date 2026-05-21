package com.allever.business.lib.project

import android.app.Activity
import android.os.Bundle
import app.allever.android.lib.core.base.BaseSimpleActivity
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.mvvm.demo.MvvmActivity
import com.allever.business.lib.project.databinding.ActivityMainBinding

class MainActivity: BaseSimpleActivity<ActivityMainBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun inflate() = ActivityMainBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.btnMvvm.setOnClickListener {
            ActivityHelper.startActivity(MvvmActivity::class.java)
        }
    }
}