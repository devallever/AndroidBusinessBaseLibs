package z.app.allever.android.sample.ui.ui

import app.allever.android.lib.common.BaseFragment
import z.app.allever.android.sample.ui.databinding.FragmentRippleBinding
import app.allever.android.lib.mvvm.base.BaseViewModel

class RippleFragment: BaseFragment<FragmentRippleBinding, BaseViewModel>() {
    override fun inflate() = FragmentRippleBinding.inflate(layoutInflater)

    override fun init() {

    }
}