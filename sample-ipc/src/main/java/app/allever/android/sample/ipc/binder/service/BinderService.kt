package app.allever.android.sample.ipc.binder.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.ProcessHelper

class BinderService: Service() {

    companion object {
        // 定义交互的 code 码，相当于方法标识
        const val CODE_ADD = 1
        const val CODE_MULTIPLY = 2
    }

    private val mBinder = ServiceBinder()

    override fun onCreate() {
        super.onCreate()
        log("BinderService", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("BinderService", "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("BinderService", "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    class ServiceBinder: Binder() {
        fun add(a: Int, b: Int): Int {
            return a + b
        }

        fun multiply(a: Int, b: Int): Int {
            return a * b
        }

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            when (code) {
                CODE_ADD -> {
                    val a = data.readInt()
                    val b = data.readInt()
                    log("BinderService", "add: $a + $b = ${a + b} in process: ${ProcessHelper.getProcessName()}")
                    val result = add(a, b)
                    reply?.writeInt(result)
                    return true
                }
                CODE_MULTIPLY -> {
                    val a = data.readInt()
                    val b = data.readInt()
                    log("BinderService", "multiply: $a * $b = ${a * b} in process: ${ProcessHelper.getProcessName()}")
                    val result = multiply(a, b)
                    reply?.writeInt(result)
                    return true
                }
            }
            return super.onTransact(code, data, reply, flags)
        }
    }
}