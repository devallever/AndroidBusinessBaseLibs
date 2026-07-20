package app.allever.android.sample.mvvm

import app.allever.android.lib.mvvm.base.BaseMvvmActivity
import app.allever.android.sample.mvvm.databinding.ActivityMvvmMainBinding
import com.therouter.router.Route

@Route(path = "/mvvm/main")
class MvvmActivity : BaseMvvmActivity<ActivityMvvmMainBinding, MainViewModel>() {
    override fun inflate() = ActivityMvvmMainBinding.inflate(layoutInflater)

    override fun init() {
        mViewModel.login()
        mBinding.tvTitle.text = ""
    }
}