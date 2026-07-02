package com.allever.app.gif.memes.ui.main.model

import com.funny.gif.memes.app.Global
import com.allever.app.gif.memes.ui.user.LoginActivity
import com.allever.app.gif.memes.ui.user.UserCenterActivity
import com.xm.lib.base.inters.IBaseView
import com.xm.lib.base.model.BaseViewModelKt
import com.xm.lib.manager.IntentManager

class GifMainViewModel: BaseViewModelKt<IBaseView>() {
    override fun onCreated() {




    }

    fun onClickRecommend() {
        if (Global.checkLogin()) {
            IntentManager.startActivity(mCxt, UserCenterActivity::class.java)
        } else {
            IntentManager.startActivity(mCxt, LoginActivity::class.java)
        }
//        IntentManager.startActivity(mCxt, GifFunDebugActivity::class.java)
//        RecommendActivity.start(mCxt, UMeng.getChannel())
    }
}