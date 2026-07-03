package org.xm.app.virtual.call.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.allever.app.virtual.call.BuildConfig
import com.allever.app.virtual.call.R
import com.allever.lib.common.app.App
import com.allever.lib.common.util.SystemUtils
import com.allever.lib.umeng.UMeng
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.include_top_bar.*
import org.xm.app.virtual.call.ad.AdContract
import org.xm.app.virtual.call.app.BaseActivity
import org.xm.app.virtual.call.ui.mvp.presenter.AboutPresenter
import org.xm.app.virtual.call.ui.mvp.view.AboutView

class AboutActivity : BaseActivity<AboutView, AboutPresenter>(),
    AboutView, View.OnClickListener {


    override fun getContentView(): Any = R.layout.activity_about

    override fun initView() {
        //判断是否有刘海屏幕
        checkNotch(Runnable {
            val statusBarViewId = addStatusBar(rootLayout)
            if (rootLayout is RelativeLayout) {
                val topBar = top_bar.layoutParams as? RelativeLayout.LayoutParams
                topBar?.addRule(RelativeLayout.BELOW, statusBarViewId.id)
            }
        })
        findViewById<View>(R.id.about_privacy).setOnClickListener(this)
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.about)
        val channel = UMeng.getChannel()
        val last = if (BuildConfig.DEBUG) {
            "(Debug)-$channel\n" +
                    "${App.context.packageName}\n" +
                    "AdMob-${AdContract.ADMOB_APP_ID}"
        } else {
            if (channel == "ad") {
                "(Release)-$channel\n" +
                        "${App.context.packageName}\n" +
                        "AdMob-${AdContract.ADMOB_APP_ID}"
            } else {
                ""
            }
        }
        findViewById<TextView>(R.id.about_app_version).text = "v${BuildConfig.VERSION_NAME}$last"
        findViewById<TextView>(R.id.about_right).text =
            String.format(getString(R.string.about_right), getString(R.string.app_name))
    }

    override fun initData() {
    }

    override fun createPresenter(): AboutPresenter =
        AboutPresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.about_privacy -> {
                val privacyUrl =
                    "https://www.hadsky.com/privacy_policy.html?chkcsrfval=2371ca600d787f8e4e68aaf691d0d56c&name=%E8%99%9A%E6%8B%9F%E6%9D%A5%E7%94%B5&alias=%E8%99%9A%E6%8B%9F%E6%9D%A5%E7%94%B5"
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