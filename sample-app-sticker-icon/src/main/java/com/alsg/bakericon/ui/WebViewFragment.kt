package com.alsg.bakericon.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import app.allever.android.lib.mvvm.base.BaseMvvmFragment
import app.allever.android.lib.mvvm.base.BaseViewModel
import com.alsg.bakericon.base.AppFragmentActivity
import com.alsg.bakericon.databinding.SiFragmentWebViewBinding

/**
 *@Description
 *@author: zq
 *@date: 2023/10/16
 */
class WebViewFragment : BaseMvvmFragment<SiFragmentWebViewBinding, BaseViewModel>() {

    companion object {
        private const val URL = "URL"

        fun start(url: String, title: String) {
            AppFragmentActivity.start<WebViewFragment>(title) {
                it.putString(URL, url)
            }
        }
    }

    override fun inflate() = SiFragmentWebViewBinding.inflate(layoutInflater)

    override fun init() {
        val url = arguments?.getString(URL, "") ?: ""
        initWebView(mBinding.web)
        mBinding.web.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebView(webView: WebView) {
        webView.apply {
            setBackgroundColor(Color.TRANSPARENT)
            //解决在5.0以上cookie无法记住问题
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.setAcceptThirdPartyCookies(webView, true)
            //设置scrollBar隐藏
            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
            isScrollbarFadingEnabled = false

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                setNeedInitialFocus(false)
                loadWithOverviewMode = true
                displayZoomControls = false
                useWideViewPort = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                setSupportZoom(true)
                allowFileAccess = true
                builtInZoomControls = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            }

            webViewClient = WebViewClient()
        }

    }
}