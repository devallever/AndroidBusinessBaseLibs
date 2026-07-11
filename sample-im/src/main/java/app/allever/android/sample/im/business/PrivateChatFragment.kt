package app.allever.android.sample.im.business

import android.view.View
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.databinding.ImPrivateChatFragmentBinding
import app.allever.android.sample.im.databinding.ImWebsocketClientManageFragmentBinding
import app.allever.android.sample.im.websocket.client.IMWebSocketClient
import kotlinx.coroutines.launch

class PrivateChatFragment: BaseFragment<ImPrivateChatFragmentBinding, BaseViewModel>() {
    private var toUsername = ""
    private val clientListener = object : IMWebSocketClient.ClientListener {
        override fun onLog(log: String) {
            log(log)
        }

        override fun onOpen() {
            log("已连接")
        }

        override fun onMessage(message: String) {
            log("收到消息：$message")
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            log("已断开")
        }

        override fun onError(ex: Exception?) {
            log("连接失败：${ex?.message}")
        }

    }

    override fun inflate() = ImPrivateChatFragmentBinding.inflate(layoutInflater)

    override fun init() {
        toUsername = arguments?.getString("username") ?: ""
        IMWebSocketClient.registerClientListener(clientListener)
        mBinding.btnClear.setOnClickListener {
            mBinding.tvLog.text = ""
        }
        mBinding.btnSend.setOnClickListener {
            val message = mBinding.etMessage.text.toString()
            if (message.isEmpty()) {
                log("请输入要发送的消息")
                return@setOnClickListener
            }
            if (!IMWebSocketClient.isConnected()) {
                log("请先连接服务器")
                return@setOnClickListener
            }
            IMWebSocketClient.sendMessageToTarget(message, toUsername)
            mBinding.etMessage.setText("")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        IMWebSocketClient.unregisterClientListener()
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