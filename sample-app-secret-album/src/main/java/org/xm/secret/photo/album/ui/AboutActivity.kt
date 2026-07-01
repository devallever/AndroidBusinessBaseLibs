package org.xm.secret.photo.album.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.function.notchcompat.NotchCompat
import org.xm.secret.photo.album.R
import org.xm.secret.photo.album.app.BaseActivity
import org.xm.secret.photo.album.mvp.BasePresenter
import org.xm.secret.photo.album.util.SystemUtils

class AboutActivity: BaseActivity<Any, BasePresenter<Any>>(), View.OnClickListener {

    private val PRIVACY_URL = "https://baidu.com"

    override fun createPresenter(): BasePresenter<Any>? = null

    override fun initView() {
        NotchCompat.adaptNotchWithFullScreen(window)
        checkNotch(Runnable {
            addStatusBar(findViewById<ViewGroup>(R.id.rootLayout), findViewById<View>(R.id.top_bar))
        })

        findViewById<View>(R.id.about_privacy).setOnClickListener(this)
        findViewById<View>(R.id.iv_back).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.about)
        findViewById<TextView>(R.id.about_app_version).text = "v1.0"
        findViewById<TextView>(R.id.about_right).text =
            String.format(getString(R.string.about_right), getString(R.string.sa_app_name))

   }

    override fun initData() {
    }

    override fun getContentView(): Any = R.layout.sa_activity_about

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.iv_back -> {
                finish()
            }
            R.id.about_privacy -> {
                SystemUtils.startWebView(App.context, PRIVACY_URL)
            }
        }
    }

    companion object {
        fun start(context: Context?) {
            val intent = Intent(context, AboutActivity::class.java)
            context?.startActivity(intent)
        }
    }
}