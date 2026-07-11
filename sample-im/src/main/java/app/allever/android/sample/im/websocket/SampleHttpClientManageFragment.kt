package app.allever.android.sample.im.websocket

import android.view.View
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.core.store.StoreCore
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.databinding.ImHttpClientManageFragmentBinding
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.http.response.StatusData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class SampleHttpClientManageFragment :
    BaseFragment<ImHttpClientManageFragmentBinding, BaseViewModel>() {

    private val SP_KEY_HTTP_URL = "SP_KEY_HTTP_URL"

    private val okHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    override fun inflate() = ImHttpClientManageFragmentBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.etBaseUrl.setText(getBaseUrl())

        mBinding.btnSetUrl.setOnClickListener {
            val url = mBinding.etBaseUrl.text.toString()
            if (url.isEmpty()) {
                log("请输入url")
                return@setOnClickListener
            }
            saveBaseUrl(url)
            initNetwork()
        }

        initNetwork()

        mBinding.btnTest.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("${getBaseUrl()}/api/status")
                    .get()
                    .build()
                val call = okHttpClient.newCall(request)
                val response = call.execute()
                val resultString = response.body?.string()
                lifecycleScope.launch(Dispatchers.Main) {
                    log("ok请求成功：${resultString}")
                }

                lifecycleScope.launch {
                    val result = NetCore.get<BaseResponse<StatusData>>("/api/status")
                    if (result.isSuccess()) {
                        val data = "netCore请求成功 data：${result.data?.toJson()}"
                        log(data)
                        log("netCore请求成功 response：${result.toJson()}")
                    } else {
                        val result = "netCore请求失败：${result.msg}"
                        log(result)
                    }
                }
            }
            }

        mBinding.btnClear.setOnClickListener {
            mBinding.tvLog.text = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun log(message: String) {
        // 追加日志，并换行
        mBinding.tvLog.append("[${TimeHelper.formatTime(System.currentTimeMillis())}] $message\n")

        // 必须使用 post，等待 TextView 重新测量布局后，再执行滚动到底部
        mBinding.scrollView.post {
            mBinding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun getBaseUrl() = StoreCore.getString(SP_KEY_HTTP_URL, "http://192.168.0.1:8080")?:"http://192.168.0.1:8080"
    private fun saveBaseUrl(url: String) {
        StoreCore.putString(SP_KEY_HTTP_URL, url)
    }

    private fun initNetwork() {
        NetCore.init {
            // 使用公开测试 API
            baseUrl(getBaseUrl())
            // 设置统一业务响应类型
            responseClass(BaseResponse::class.java)

//            engine(OkHttpEngine.ENGINE_NAME) {
//                // OkHttp 专属配置
//                (this as? OkHttpConfig)?.apply {
////                    connectionPool(5, 5, java.util.concurrent.TimeUnit.MINUTES)
////                    retryOnConnectionFailure(true)
////                    addInterceptor("LoggingInterceptor")
////                    addNetworkInterceptor("LoggingInterceptor")
//                }
//            }
        }

        log("Network 已初始化，引擎: ${NetCore.currentEngine()} -> ${getBaseUrl()}")
    }

}