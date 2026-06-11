package z.app.allever.android.sample.ui.ui.rebound

import z.app.allever.android.sample.ui.databinding.FragmentReboundScrollViewDemoBinding
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel

class ReboundScrollViewFragment :
    BaseMvvmFragment<FragmentReboundScrollViewDemoBinding, ReboundScrollViewViewModel>() {

    override fun inflate() = FragmentReboundScrollViewDemoBinding.inflate(layoutInflater)

    override fun init() {
    }
}

class ReboundScrollViewViewModel : BaseViewModel() {
    override fun init() {

    }
}