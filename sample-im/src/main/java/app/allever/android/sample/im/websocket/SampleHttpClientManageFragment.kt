package app.allever.android.sample.im.websocket

import android.view.View
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.databinding.ImHttpClientManageFragmentBinding
import app.allever.android.sample.im.http.LocalHttpServer
import app.allever.android.sample.im.http.StatusData
import app.allever.android.sample.im.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SampleHttpClientManageFragment :
    BaseFragment<ImHttpClientManageFragmentBinding, BaseViewModel>() {

    private val okHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    override fun inflate() = ImHttpClientManageFragmentBinding.inflate(layoutInflater)

    override fun init() {

        mBinding.btnTest.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("http://10.20.224.246:8080/api/status")
                    .get()
                    .build()
                val call = okHttpClient.newCall(request)
                val response = call.execute()
                val resultString = response.body?.string()
                lifecycleScope.launch(Dispatchers.Main) {
                    log("ok请求成功：${resultString}")
                }

//                call.enqueue(object : okhttp3.Callback {
//                    override fun onFailure(call: Call, e: IOException) {
//                        lifecycleScope.launch(Dispatchers.Main) {
//                            log("请求失败：${e.message}")
//                        }
//                    }
//
//                    override fun onResponse(call: Call, response: Response) {
//                        val resultString = response.body?.string()
//                        lifecycleScope.launch(Dispatchers.Main) {
//                            log("请求成功：${resultString}")
//                        }
//                    }
//
//                })
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

}