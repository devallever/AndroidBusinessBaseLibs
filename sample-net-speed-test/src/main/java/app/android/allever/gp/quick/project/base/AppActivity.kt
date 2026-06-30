package app.android.allever.gp.quick.project.base

import androidx.viewbinding.ViewBinding
import app.allever.android.lib.mvvm.base.BaseMvvmActivity
import app.allever.android.lib.mvvm.base.BaseViewModel

/**
 *@Description
 *@author: zq
 *@date: 2024/1/23
 */
abstract class AppActivity<DB : ViewBinding, VM : BaseViewModel> : BaseMvvmActivity<DB, VM>() {
}