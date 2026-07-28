package com.alsg.bakericon.ui

import androidx.lifecycle.lifecycleScope
import app.allever.lib.billing.BillingHelper
import com.allever.lib.base.helper.ActivityHelper
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.Constant
import com.alsg.bakericon.MainActivity
import com.alsg.bakericon.SlideMainActivity
import com.alsg.bakericon.base.AppActivity
import com.alsg.bakericon.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *@Description
 *@author: zq
 *@date: 2024/1/12
 */
class SplashActivity : AppActivity<ActivitySplashBinding, BaseViewModel>() {
    override fun inflate() = ActivitySplashBinding.inflate(layoutInflater)

    override fun init() {
        lifecycleScope.launch {
            delay(1000)
            BillingHelper.getProductDetails(Constant.PRODUCT_ID_LIST, finish = null)
            ActivityHelper.startActivity<SlideMainActivity> { }
            finish()
        }
    }

    override fun onBackPressed() {

    }

    override fun initObserver() {

    }
}