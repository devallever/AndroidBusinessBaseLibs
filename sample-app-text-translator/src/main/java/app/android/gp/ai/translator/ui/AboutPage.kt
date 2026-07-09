package app.android.gp.ai.translator.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppMvpActivity
import app.android.gp.ai.translator.databinding.AAboutBinding
import app.android.gp.ai.translator.ui.mvp.presenter.AboutPresenter
import app.android.gp.ai.translator.ui.mvp.view.AboutView
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.SystemHelper

class AboutPage : AppMvpActivity<AboutView, AboutPresenter>(), AboutView {

    private val PRIVACY_URL = "https://www.privacypolicies.com/live/2e526aab-7ecd-495b-8ee4-8b7d00822321"

    private lateinit var mBinding: AAboutBinding



    override fun getContentView(): Any {
        mBinding = AAboutBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        addStatusBar(findViewById(R.id.rootLayout), findViewById(R.id.top_bar))
        mBinding.aboutPrivacy.setOnClickListener {
            SystemHelper.startWebView(App.context, PRIVACY_URL)
        }
        findViewById<View>(R.id.iv_left).setOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.tt_about)
        val last = ""
        mBinding.aboutAppVersion.text = "v1.0"
        mBinding.aboutRight.text =
            String.format(getString(R.string.tt_about_right), getString(R.string.tt_app_name))
    }

    override fun initData() {
    }

    override fun createPresenter(): AboutPresenter = AboutPresenter()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AboutPage::class.java)
            context.startActivity(intent)
        }
    }
}