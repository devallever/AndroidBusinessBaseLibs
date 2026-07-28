package com.alsg.bakericon.base

import androidx.viewbinding.ViewBinding
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel

/**
 *@Description
 *@author: allever
 *@date: 2024/1/9
 */
abstract class AppFragment<DB : ViewBinding, VM : BaseViewModel> : BaseMvvmFragment<DB, VM>() {
}