package z.app.allever.android.sample.ui.ui.rebound

import z.app.allever.android.sample.ui.databinding.FragmentReboundViewDragHelperDemoBinding
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel

class ReboundViewDragHelperFragment :
    BaseMvvmFragment<FragmentReboundViewDragHelperDemoBinding, ReboundViewDragHelperViewViewModel>() {

    override fun inflate() = FragmentReboundViewDragHelperDemoBinding.inflate(layoutInflater)

    override fun init() {
    }
}

class ReboundViewDragHelperViewViewModel : BaseViewModel() {
    override fun init() {

    }
}