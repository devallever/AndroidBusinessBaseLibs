package com.step.wincash.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.isVisible
import app.allever.android.lib.core.app.App
import com.step.wincash.base.BaseFragment
import com.step.wincash.databinding.StFragmentWebBinding
import com.step.wincash.event.ChangeShowPage
import com.step.wincash.init.Constance
import com.step.wincash.init.Constance.LINK_I
import com.step.wincash.ui.activity.STMainActivity
import com.step.wincash.utils.MusicUtil
import com.step.wincash.utils.WebJsOK
import com.step.wincash.utils.getHostSafe
import com.step.wincash.utils.openIntent
import com.step.wincash.utils.setOnSingleListener
import org.greenrobot.eventbus.EventBus


class WebFragment : BaseFragment<StFragmentWebBinding>() {
    private val TAG = this.javaClass.simpleName
    private var _url: String? = null
    private var clickUrl = ""
    private var _webView: WebView? = null

    private val ARG_URL = "arg_url"
    val GAID_TAG = "{gaid}"

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): StFragmentWebBinding {
        return StFragmentWebBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backImg.setOnSingleListener {
            if (_webView?.canGoBack() == true) {
                _webView?.goBack()
            } else {
                if (requireActivity() !is STMainActivity) {
                    requireActivity().finish()
                    return@setOnSingleListener
                }
                _webView?.onPause()
                EventBus.getDefault().post(ChangeShowPage())
            }
        }

        binding.ivHome.setOnSingleListener {
            if (requireActivity() !is STMainActivity) {
                requireActivity().finish()
                return@setOnSingleListener
            }
            _webView?.onPause()
            EventBus.getDefault().post(ChangeShowPage())
        }

        binding.ivReload.setOnSingleListener {
            binding.flLoading.isVisible = true
            _webView?.reload()
        }

        arguments?.let {
            _url = it.getString(ARG_URL)
        }
        fixStatusBar(binding.topBarFl)
        initWebView()

        loadUrl(_url)
    }


    fun loadUrl(url: String?) {
        if (url.isNullOrEmpty()) return
        if (_webView == null) return

        binding.flLoading.isVisible = true

        _url = url

        _webView?.let {
            it.clearHistory()
            it.removeAllViews()
            it.onResume()
            clickUrl = _url!!
            it.loadUrl(clickUrl)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        val webView = WebView(requireContext())
        binding.container.addView(
            webView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        _webView = webView
        _webView!!.settings.apply {
            javaScriptEnabled = true
            loadsImagesAutomatically = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            allowFileAccess = true
            databaseEnabled = true
            domStorageEnabled = true // 打开本地缓存提供JS调用,至关重要，开启DOM缓存，开启LocalStorage存储
            useWideViewPort = true
            setSupportZoom(false)
            setSupportMultipleWindows(false)
            setEnableSmoothTransition(true)
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            setGeolocationEnabled(true)
        }

        if (_url?.contains(LINK_I.getHostSafe()) == true) {
            // 添加命名空间
            val jsHelper = WebJsOK(requireContext())
            _webView!!.addJavascriptInterface(jsHelper, Constance.OKSPINE)
        }

        if (App.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        _webView?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, progress: Int) {
                super.onProgressChanged(view, progress)
                if (progress > 80) {
                    binding.flLoading.isVisible = false
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
            }

        }

        _webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.flLoading.isVisible = false
            }

            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean {
                if (!isAdded) {
                    return false
                }
                //处理okSpin的跳转 h5链接不跳
                val url = request.url.toString()
                clickUrl = url
                try {
                    if (openIntent(url, requireActivity())) {
                        return true
                    }
                } catch (e: Throwable) {
                    super.shouldOverrideUrlLoading(view, request)
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        _webView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        _webView?.onPause()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            _webView?.onPause()
        }else{
            _webView?.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            MusicUtil.play()
            _webView?.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    companion object {
        @JvmStatic
        fun newInstance(url: String? = null) = WebFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_URL, url)
            }
        }
    }
}