package app.android.allever.gp.quick.project.base

import androidx.viewbinding.ViewBinding
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel

/**
 *@Description
 *@author: zq
 *@date: 2024/1/23
 */
abstract class AppFragment<DB : ViewBinding, VM : BaseViewModel> : BaseMvvmFragment<DB, VM>() {
}