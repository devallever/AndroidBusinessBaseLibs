package com.example.charge.ui.fragment

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
import com.example.charge.base.BaseFragment
import com.example.charge.databinding.FragmentWebBinding
import com.example.charge.init.Constance
import com.example.charge.ui.activity.MainActivity
import com.example.charge.utils.WebJsOK
import com.example.charge.utils.getHostSafe
import com.example.charge.utils.openIntent
import com.example.charge.utils.setOnSingleListener
import gjofg.frytfkrqy.hxrdk.gddrjgra.SdkManager
import org.greenrobot.eventbus.EventBus
import kotlin.apply
import kotlin.jvm.javaClass
import kotlin.let
import kotlin.text.contains
import kotlin.text.endsWith
import kotlin.text.isNullOrEmpty
import kotlin.text.replace


class WebFragment : BaseFragment<FragmentWebBinding>() {
    private val TAG = this.javaClass.simpleName
    private var _url: String? = null
    private var clickUrl = ""
    private var _webView: WebView? = null

    private val ARG_URL = "arg_url"
    val GAID_TAG = "{gaid}"

    override fun getBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWebBinding {
        return FragmentWebBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.backImg.setOnSingleListener {
            if (_webView?.canGoBack() == true) {
                _webView?.goBack()
            } else {
                if (requireActivity() !is MainActivity) {
                    requireActivity().finish()
                    return@setOnSingleListener
                }
                _webView?.onPause()
            }
        }

        binding.ivHome.setOnSingleListener {
            if (requireActivity() !is MainActivity) {
                requireActivity().finish()
                return@setOnSingleListener
            }
            _webView?.onPause()
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

        _url = if (url.endsWith(GAID_TAG)) {
            url.replace(GAID_TAG, SdkManager.getGoogleAndroidId())
        } else {
            url
        }

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

        if (_url?.contains(Constance.OKSPIN_URL.getHostSafe()) == true) {
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
//            MusicUtil.play()
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