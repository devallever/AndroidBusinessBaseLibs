package app.allever.android.sample.im.websocket

import android.view.View
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.databinding.ImHttpClientManageFragmentBinding
import app.allever.android.sample.im.response.BaseResponse
import kotlinx.coroutines.launch

class SampleHttpClientManageFragment :
    BaseFragment<ImHttpClientManageFragmentBinding, BaseViewModel>() {

    override fun inflate() = ImHttpClientManageFragmentBinding.inflate(layoutInflater)

    override fun init() {

        mBinding.btnTest.setOnClickListener {
            lifecycleScope.launch {
                val result = NetCore.get<BaseResponse<String>>("/api/status")
                if (result.isSuccess()) {
                    val data = "请求成功：${result.data}"
                    log(data)
                } else {
                    val result = "请求失败：${result.msg}"
                    log(result)
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