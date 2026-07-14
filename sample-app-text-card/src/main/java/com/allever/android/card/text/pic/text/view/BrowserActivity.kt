package com.allever.android.card.text.pic.text.view

import android.content.Context
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.allever.android.card.text.pic.text.util.StatusBarUtil
import com.allever.android.card.text.pic.text.base.AbsViewModel
import com.allever.android.card.text.pic.text.base.AppActivity
import com.allever.android.card.text.pic.text.databinding.TcActivityWebViewBinding

class BrowserActivity : AppActivity<TcActivityWebViewBinding, AbsViewModel>() {
    companion object {
        fun start(context: Context, title: String, url: String) {
            val intent = Intent(context, com.allever.android.card.text.pic.text.view.BrowserActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("url", url)
            context.startActivity(intent)
        }
    }

    override fun inflate() = TcActivityWebViewBinding.inflate(layoutInflater)

    override fun init() {

        StatusBarUtil.fixStatusBar(mBinding.topBar)
        mBinding.ivClose.setOnClickListener {
            finish()
        }

        val url = intent?.getStringExtra("url") ?: ""
        mBinding.tvTitle.text = intent?.getStringExtra("title") ?: ""
        mBinding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        mBinding.webView.loadUrl(url)

    }

    override fun viewModelClass() = AbsViewModel::class.java
}