package com.allever.app.gif.memes.ui

import android.os.Bundle
import com.allever.app.gif.memes.BuildConfig
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.ui.main.GifMainActivity
import com.allever.lib.common.app.BaseActivity
import com.allever.lib.common.util.ActivityCollector
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.util.SpUtils

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if (BuildConfig.DEBUG) {
            SpUtils.putString(Global.SP_OFFSET, "0")
            SpUtils.putString(Global.SP_SEARCH_OFFSET, "0")
        }

//        BillingHelper.connect()
        mHandler.postDelayed({
//            BillingHelper.getProductDetails(BillingConfig.PRODUCT_ID_LIST, finish = null)
            ActivityCollector.startActivity(this, GifMainActivity::class.java)
            finish()
        }, 2000)
    }

    override fun onBackPressed() {
    }
}