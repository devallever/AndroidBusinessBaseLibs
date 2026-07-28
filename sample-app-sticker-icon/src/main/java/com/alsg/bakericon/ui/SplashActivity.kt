package com.alsg.bakericon.ui

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.Constant
import com.alsg.bakericon.SlideMainActivity
import com.alsg.bakericon.base.AppActivity
import com.alsg.bakericon.databinding.SiActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/12
 */
class SplashActivity : AppActivity<SiActivitySplashBinding, BaseViewModel>() {
    override fun inflate() = SiActivitySplashBinding.inflate(layoutInflater)

    override fun init() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }

        })
        lifecycleScope.launch {
            delay(1000)
            ActivityHelper.startActivity<SlideMainActivity> { }
            finish()
        }
    }
}