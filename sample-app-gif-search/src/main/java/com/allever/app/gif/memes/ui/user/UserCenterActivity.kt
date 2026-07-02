package com.allever.app.gif.memes.ui.user

import com.allever.app.gif.memes.BR
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.BaseDataActivity2
import com.allever.app.gif.memes.databinding.ActivityUserCenterBinding
import com.allever.app.gif.memes.ui.user.model.UserCenterViewModel
import com.xm.lib.base.config.DataBindingConfig

class UserCenterActivity : BaseDataActivity2<ActivityUserCenterBinding, UserCenterViewModel>(){

    override fun initDataBindingConfig() = DataBindingConfig(R.layout.activity_user_center, BR.userCenterViewModel)

    override fun initDataAndEvent() {
        mBinding.includeTapBar.tvLabel.text = getStringRes(R.string.mine)
        mBinding.includeTapBar.ivLeft.setOnClickListener {
            finish()
        }
    }

    override fun destroyView() {
    }

}