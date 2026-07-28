package com.alsg.bakericon.base

import androidx.viewbinding.ViewBinding
import com.allever.lib.base.mvvm.BaseMvvmFragment
import com.allever.lib.base.mvvm.BaseViewModel

/**
 *@Description
 *@author: allever
 *@date: 2024/1/9
 */
abstract class AppFragment<DB : ViewBinding, VM : BaseViewModel> : BaseMvvmFragment<DB, VM>() {
}