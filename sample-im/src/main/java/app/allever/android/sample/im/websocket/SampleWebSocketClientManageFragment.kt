package app.allever.android.sample.im.websocket

import android.view.View
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.databinding.ImWebsocketClientManageFragmentBinding
import app.allever.android.sample.im.protocol.Message
import app.allever.android.sample.im.websocket.client.IMWebSocketClient

class SampleWebSocketClientManageFragment: BaseFragment<ImWebsocketClientManageFragmentBinding, BaseViewModel>() {


    private val clientListener = object : IMWebSocketClient.ClientListener {
        override fun onLog(log: String) {
            log(log)
        }

        override fun onOpen() {
            mBinding.tvStatus.text = "已连接"
            log("已连接")
        }

        override fun onMessage(message: String) {
            log("收到消息：$message")
        }

        override fun onMessage(message: Message) {
            log("收到消息[${message.type}][${message.contentType}]: ${message.fromUser} -> ${message.toUser}: ${message.content}")
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            mBinding.tvStatus.text = "已断开"
            log("已断开")
        }

        override fun onError(ex: Exception?) {
            mBinding.tvStatus.text = "连接失败"
            log("连接失败：${ex?.message}")
        }

    }

    override fun inflate() = ImWebsocketClientManageFragmentBinding.inflate(layoutInflater)

    override fun init() {
        IMWebSocketClient.registerClientListener(clientListener)
        mBinding.tvStatus.text = if (IMWebSocketClient.isConnected()) "已连接" else "未连接"
        val url = IMConfig.getWebsocketUrl()
        mBinding.etUrl.setText(url)

        mBinding.btnConnect.setOnClickListener {
            if (!IMConfig.isLogin()) {
                log("请先登录")
                return@setOnClickListener
            }
            val url = mBinding.etUrl.text.toString()
            if (url.isEmpty()) {
                log("请输入正确的URL")
                return@setOnClickListener
            }
            val connectUrl = "$url?username=${IMConfig.getLoginUser()}"
            IMWebSocketClient.connect(connectUrl)
            IMConfig.saveWebsocketUrl(url)
        }
        mBinding.btnDisconnect.setOnClickListener {
            IMWebSocketClient.disconnect()
        }
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
            IMWebSocketClient.sendMessage(message)
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