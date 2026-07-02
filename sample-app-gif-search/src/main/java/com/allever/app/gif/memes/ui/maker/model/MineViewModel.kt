package com.allever.app.gif.memes.ui.maker.model

import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.allever.app.gif.memes.ui.maker.PickActivity

class MineViewModel: BaseViewModel() {

    fun onClickChooseVideo() {
        ActivityHelper.startActivity<PickActivity>()
    }
}