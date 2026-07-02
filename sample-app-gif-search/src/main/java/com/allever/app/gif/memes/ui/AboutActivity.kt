package com.allever.app.gif.memes.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.BaseActivity
import com.allever.app.gif.memes.ui.mvp.presenter.AboutPresenter
import com.allever.app.gif.memes.ui.mvp.view.AboutView
import app.allever.android.lib.core.app.App
import com.funny.gif.memes.util.SystemUtils

class AboutActivity : BaseActivity<AboutView, AboutPresenter>(), AboutView, View.OnClickListener {


    override fun getContentView(): Any = R.layout.gs_activity_about

    override fun initView() {
        findViewById<View>(R.id.about_privacy).setOnClickListener(this)
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.about)
        findViewById<TextView>(R.id.about_app_version).text = "v1.0"
    }

    override fun initData() {
    }

    override fun createPresenter(): AboutPresenter = AboutPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.about_privacy -> {
                val privacyUrl = "https://www.privacypolicies.com/live/47b211fa-e117-4830-804f-5889e952facb"
                SystemUtils.startWebView(App.context, privacyUrl)
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AboutActivity::class.java)
            context.startActivity(intent)
        }
    }
}