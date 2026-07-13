package app.allever.android.sample.im.websocket

import android.view.View
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.IMGlobal
import app.allever.android.sample.im.databinding.ImHttpClientManageFragmentBinding
import app.allever.android.sample.im.http.API
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.http.response.StatusData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request

class SampleHttpClientManageFragment :
    BaseFragment<ImHttpClientManageFragmentBinding, BaseViewModel>() {

    override fun inflate() = ImHttpClientManageFragmentBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.etBaseUrl.setText(IMConfig.getHttpBaseUrl())

        mBinding.btnSetUrl.setOnClickListener {
            val url = mBinding.etBaseUrl.text.toString()
            if (url.isEmpty()) {
                log("请输入url")
                return@setOnClickListener
            }
            IMConfig.saveHttpBaseUrl(url)
            IMGlobal.initNetwork()
        }

        mBinding.btnTest.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val request = Request.Builder()
                    .url("${IMConfig.getHttpBaseUrl()}${API.USER_STATUS}")
                    .get()
                    .build()
                val call = IMGlobal.okHttpClient.newCall(request)
                val response = call.execute()
                val resultString = response.body?.string()
                lifecycleScope.launch(Dispatchers.Main) {
                    log("ok请求成功：${resultString}")
                }

                lifecycleScope.launch {
                    val result = NetCore.get<BaseResponse<StatusData>>(API.USER_STATUS)
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