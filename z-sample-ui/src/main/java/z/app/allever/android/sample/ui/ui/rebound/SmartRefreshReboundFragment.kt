package z.app.allever.android.sample.ui.ui.rebound

import z.app.allever.android.sample.ui.databinding.FragmentSmartRefreshReboundBinding
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel

class SmartRefreshReboundFragment :
    BaseMvvmFragment<FragmentSmartRefreshReboundBinding, BaseViewModel>() {

    override fun inflate() = FragmentSmartRefreshReboundBinding.inflate(layoutInflater)

    override fun init() {

        mBinding.smartRefreshLayout.setEnableRefresh(false)
        mBinding.smartRefreshLayout.setEnableLoadMore(false)
        mBinding.smartRefreshLayout.setEnableOverScrollDrag(true)

    }
}