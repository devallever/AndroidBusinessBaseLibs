package app.allever.android.sample.ipc

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.view.Gravity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ProcessHelper
import app.allever.android.lib.core.util.TimeUtils
import app.allever.android.sample.ipc.messenger.MessengerService
import com.chad.library.adapter.base.BaseQuickAdapter

class IPCMessengerFragment :
    ListFragment<FragmentListBinding, ListViewModel, TextDetailClickItem>() {

    private val mClientHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MessengerService.MSG_FROM_SERVER -> {
                    val data = msg.data
                    val content = data.getString("content")
                    toast("收到服务端消息: $content")
                    log(
                        "MessengerClient",
                        "收到服务端消息: $content in process: ${ProcessHelper.getProcessName()}"
                    )
                }
            }
        }
    }

    private val mClientMessenger = Messenger(mClientHandler)

    private var mServiceMessenger: Messenger? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            log("MessengerClient", "onServiceConnected")
            mServiceMessenger = Messenger(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mServiceMessenger = null
        }
    }

    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> =
        TextDetailClickAdapter(Gravity.CENTER)

    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf(
        TextDetailClickItem("发送消息给服务端") {
            val msg = Message.obtain(null, MessengerService.MSG_FROM_CLIENT)
            val bundle = Bundle().apply {
                putString(
                    "content",
                    "${
                        TimeUtils.formatTime(
                            System.currentTimeMillis(),
                            "[HH:mm:ss]"
                        )
                    }我是客户端，请回复"
                )
            }
            msg.data = bundle
            //关键步骤，设置 replyTo，服务端收到消息后，会通过 replyTo 回复消息
            msg.replyTo = mClientMessenger
            mServiceMessenger?.send(msg)
        })

    override fun init() {
        super.init()
        val intent = Intent(requireActivity(), MessengerService::class.java)
        requireActivity().bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(mConnection)
    }
}