package app.flash.tunnel.vpn.page

import android.content.Context
import android.content.Intent
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.databinding.WebActivityBinding
import app.flash.tunnel.vpn.lib.common.util.StatusBarManager

class WebViewActivity : BaseActivity<WebActivityBinding>() {

    companion object {
        fun start(context: Context, url: String, title: String) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(Constants.EXTRA_WEB_URL, url)
            intent.putExtra(Constants.EXTRA_WEB_TITLE, title)
            context.startActivity(intent)
        }
    }

    override fun inflate() = WebActivityBinding.inflate(layoutInflater)

    override fun init() {
        fixStatusBar(mBinding.appBar)
        mBinding.ivBack.setOnClickListener { finish() }
        StatusBarManager.setLightStatusBar(this)
        mBinding.tvTitle.text = intent?.getStringExtra(Constants.EXTRA_WEB_TITLE) ?: ""
        mBinding.webView.loadUrl(intent?.getStringExtra(Constants.EXTRA_WEB_URL) ?: "")
    }
}