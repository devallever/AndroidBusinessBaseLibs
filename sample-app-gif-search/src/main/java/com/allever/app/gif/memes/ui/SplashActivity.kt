package com.allever.app.gif.memes.ui

import android.annotation.SuppressLint
import android.os.Bundle
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.helper.ActivityHelper
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.ui.main.GifMainActivity
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.util.SpUtils

class SplashActivity : AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gs_activity_splash)
        if (App.DEBUG) {
            SpUtils.putString(Global.SP_OFFSET, "0")
            SpUtils.putString(Global.SP_SEARCH_OFFSET, "0")
        }

        mHandler.postDelayed({
            ActivityHelper.startActivity<GifMainActivity>()
            finish()
        }, 2000)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }
}