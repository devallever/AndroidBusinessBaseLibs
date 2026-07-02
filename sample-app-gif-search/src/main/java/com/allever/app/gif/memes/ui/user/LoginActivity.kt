package com.allever.app.gif.memes.ui.user

import com.allever.app.gif.memes.BR
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.BaseDataActivity2
import com.allever.app.gif.memes.databinding.ActivityLoginBinding
import com.allever.app.gif.memes.ui.user.model.LoginViewModel
import com.xm.lib.base.config.DataBindingConfig

class LoginActivity : BaseDataActivity2<ActivityLoginBinding, LoginViewModel>() {

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_login, BR.loginViewModel)

    override fun initDataAndEvent() {
        mBinding.includeTapBar.tvLabel.text = getStringRes(R.string.login)
        mBinding.includeTapBar.ivLeft.setOnClickListener {
            finish()
        }
    }

    override fun destroyView() {
    }

}