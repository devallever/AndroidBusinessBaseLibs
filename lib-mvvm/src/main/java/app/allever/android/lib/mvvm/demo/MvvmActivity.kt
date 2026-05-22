package app.allever.android.lib.mvvm.demo

import app.allever.android.lib.mvvm.base.BaseMvvmActivity
import app.allever.android.lib.mvvm.databinding.ActivityMvvmBinding
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = "/mvvm/main")
class MvvmActivity : BaseMvvmActivity<ActivityMvvmBinding, MainViewModel>() {
    override fun inflate() = ActivityMvvmBinding.inflate(layoutInflater)

    override fun init() {
        mViewModel.login()
        mBinding.tvTitle.text = ""
    }
}