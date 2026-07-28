package com.alsg.bakericon.base

import androidx.viewbinding.ViewBinding
import app.allever.android.lib.mvvm.base.BaseMvvmActivity
import app.allever.android.lib.mvvm.base.BaseViewModel

/**
 *@Description
 *@author: allever
 *@date: 2024/1/9
 */
abstract class AppActivity<DB : ViewBinding, VM : BaseViewModel> : BaseMvvmActivity<DB, VM>() {

}