package z.app.allever.android.sample.ui.ui.sticktop

import app.allever.android.lib.common.BaseActivity
import z.app.allever.android.sample.ui.databinding.ActivityCoordinatorStickyTopBinding
import app.allever.android.lib.mvvm.base.BaseViewModel

class CoordinatorStickyTopActivity :
    BaseActivity<ActivityCoordinatorStickyTopBinding, CoordinatorStickyTopViewModel>() {

    override fun inflateChildBinding() = ActivityCoordinatorStickyTopBinding.inflate(layoutInflater)

    override fun init() {
        initTopBar("CoordinatorLayout + AppbarLayout")

    }


}

class CoordinatorStickyTopViewModel : BaseViewModel() {
    override fun init() {

    }
}