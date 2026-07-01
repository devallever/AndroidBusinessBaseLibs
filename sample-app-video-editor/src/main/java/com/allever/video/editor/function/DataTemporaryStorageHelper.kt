package com.allever.video.editor.function

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.android.absbase.utils.TimeUtils
import java.util.concurrent.ConcurrentHashMap

object DataTemporaryStorageHelper : Handler.Callback {
    private const val KEY_REMOVE = 1

    private val datas = ConcurrentHashMap<String, Any>()
    private val mHandler = Handler(Looper.getMainLooper(), this)

    override fun handleMessage(msg: Message): Boolean {
        when (msg?.what) {
            KEY_REMOVE -> {
                val key = msg.obj as? String
                if (key != null) {
                    datas.remove(key)
                }
            }
        }
        return true
    }

    private fun sendRemoveMessage(key: String, keepTime: Long) {
        val msg = Message.obtain()
        msg.what = KEY_REMOVE
        msg.obj = key
        mHandler.sendMessageDelayed(msg, keepTime)
    }

    fun put(key: String, data: Any, keepTime: Long = TimeUtils.TimeConstant.ONE_MIN) {
        datas[key] = data
        sendRemoveMessage(
            key,
            keepTime
        )
    }

    fun get(key: String): Any? {
        val data = datas.remove(key)
        return data
    }
}