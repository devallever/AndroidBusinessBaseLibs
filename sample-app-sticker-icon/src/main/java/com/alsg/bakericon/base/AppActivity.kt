package com.alsg.bakericon.base

import androidx.viewbinding.ViewBinding
import com.allever.lib.base.mvvm.BaseMvvmActivity
import com.allever.lib.base.mvvm.BaseViewModel
import com.alsg.bakericon.ad.AdRepository

/**
 *@Description
 *@author: allever
 *@date: 2024/1/9
 */
abstract class AppActivity<DB : ViewBinding, VM : BaseViewModel> : BaseMvvmActivity<DB, VM>() {

    override fun onResume() {
        super.onResume()
        AdRepository.instance.registerTopActivity(this)
    }

    override fun onPause() {
        super.onPause()
        AdRepository.instance.unRegisterTopActivity()
    }
}