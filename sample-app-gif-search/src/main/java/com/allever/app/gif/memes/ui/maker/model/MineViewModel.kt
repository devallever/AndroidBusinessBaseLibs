package com.allever.app.gif.memes.ui.maker.model

import com.allever.app.gif.memes.ui.maker.PickActivity
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.manager.IntentManager

class MineViewModel: BaseViewModelKt<IBaseView>() {
    override fun onCreated() {

    }

    fun onClickChooseVideo() {
        IntentManager.startActivity(mCxt, PickActivity::class.java)
    }
}