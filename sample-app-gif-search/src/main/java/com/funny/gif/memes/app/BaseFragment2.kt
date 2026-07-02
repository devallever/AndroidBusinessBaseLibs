package com.funny.gif.memes.app

import android.view.KeyEvent
import androidx.databinding.ViewDataBinding
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModel
import com.xm.lib.base.ui.BaseFragmentKt

abstract class BaseFragment2<DB : ViewDataBinding, VM : BaseViewModel<IBaseView>> :
    BaseFragmentKt<DB, VM>() {
    override fun isUseImmersionBar(): Boolean {
        return false
    }

    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }
}