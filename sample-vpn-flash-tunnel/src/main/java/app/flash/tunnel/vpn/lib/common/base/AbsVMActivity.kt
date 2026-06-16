package app.flash.tunnel.vpn.lib.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class AbsVMActivity<VB : ViewBinding, VM : ViewModel> : AbsBindingActivity<VB>() {

    protected val mViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[viewModelClass()]
    }

    abstract fun viewModelClass(): Class<VM>
}