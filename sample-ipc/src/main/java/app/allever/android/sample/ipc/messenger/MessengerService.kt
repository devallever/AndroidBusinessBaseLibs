package app.allever.android.sample.ipc.messenger

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.ProcessHelper
import app.allever.android.lib.core.util.TimeUtils

class MessengerService : Service() {

    private val TAG = "MessengerService"

    companion object {
        const val MSG_FROM_CLIENT = 100
        const val MSG_FROM_SERVER = 101
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_FROM_CLIENT -> {
                    val data = msg.data
                    val clientMsg = data.getString("content")
                    log(
                        TAG,
                        "收到客户端消息: $clientMsg in process ${ProcessHelper.getProcessName()}"
                    )

                    // 回复客户端
                    val replyMessenger = msg.replyTo
                    if (replyMessenger != null) {
                        val replyMsg = Message.obtain(null, MSG_FROM_SERVER)
                        val bundle = Bundle().apply {
                            val msg = "${
                                TimeUtils.formatTime(
                                    System.currentTimeMillis(),
                                    "[HH:mm:ss]"
                                )
                            }服务端已收到，你好客户端！"
                            log(TAG, "服务端发送消息: $msg")
                            putString("content", msg)
                        }
                        replyMsg.data = bundle
                        try {
                            replyMessenger.send(replyMsg)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    // 将 Handler 封装为 Messenger
    private val mMessenger = Messenger(mHandler)

    override fun onBind(intent: Intent?): IBinder? {
        return mMessenger.binder
    }

    override fun onCreate() {
        super.onCreate()
        log(TAG, "onCreate in process: ${ProcessHelper.getProcessName()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        log(TAG, "onDestroy in process: ${ProcessHelper.getProcessName()}")
    }

    //bindS
    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        log(TAG, "bindService in process: ${ProcessHelper.getProcessName()}")
        return super.bindService(service, conn, flags)
    }
}