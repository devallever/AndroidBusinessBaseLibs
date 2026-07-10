package app.allever.android.sample.im.websocket

import android.view.View
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.core.helper.TimeHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.im.databinding.ImHttpServerManageFragmentBinding
import app.allever.android.sample.im.http.LocalHttpServer

class SampleHttpServerManageFragment :
    BaseFragment<ImHttpServerManageFragmentBinding, BaseViewModel>() {

    private val serverListener = object : LocalHttpServer.HttpServerListener {

        override fun onLog(log: String) {
            log(log)
        }

        override fun onStarted(url: String) {
            mBinding.tvStatus.text = "已启动：$url"
        }

        override fun onStopped() {
            mBinding.tvStatus.text = "已停止"
        }

        override fun onError(msg: String) {
            log(msg)
        }
    }

    override fun inflate() = ImHttpServerManageFragmentBinding.inflate(layoutInflater)

    override fun init() {
        mBinding.tvStatus.text = if (LocalHttpServer.isRunning()) {
            "已启动：${LocalHttpServer.getServerUrl()}"
        } else {
            "已停止"
        }


        LocalHttpServer.registerListener(serverListener)

        mBinding.btnStart.setOnClickListener {
            LocalHttpServer.startServer(8080)
        }
        mBinding.btnStop.setOnClickListener {
            LocalHttpServer.stopServer()
        }
        mBinding.btnClear.setOnClickListener {
            mBinding.tvLog.text = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalHttpServer.unregisterListener()
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