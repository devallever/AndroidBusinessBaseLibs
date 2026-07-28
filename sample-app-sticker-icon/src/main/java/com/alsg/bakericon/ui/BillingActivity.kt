package com.alsg.bakericon.ui

import app.allever.lib.billing.BillingHelper
import com.allever.lib.base.helper.ViewHelper
import com.allever.lib.base.mvvm.BaseViewModel
import com.allever.lib.base.util.BarUtils
import com.alsg.bakericon.Constant
import com.alsg.bakericon.base.AppActivity
import com.alsg.bakericon.databinding.ActivityBillingBinding

/**
 *@Description
 *@author: zq
 *@date: 2024/1/18
 */
class BillingActivity : AppActivity<ActivityBillingBinding, BaseViewModel>() {
    override fun inflate() = ActivityBillingBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.apply {
            ViewHelper.setMarginTop(ivClose, BarUtils.getStatusBarHeight())
            btnBilling.setOnClickListener {
                BillingHelper.subScribe(
                    this@BillingActivity,
                    Constant.PRODUCT_WEEKLY
                ) { success, code, message ->
                    if (success) {
                        finish()
                    }
                }
            }

            ivClose.setOnClickListener {
                finish()
            }
        }
    }

    override fun initObserver() {
    }

    override fun isDarkMode() = true
}