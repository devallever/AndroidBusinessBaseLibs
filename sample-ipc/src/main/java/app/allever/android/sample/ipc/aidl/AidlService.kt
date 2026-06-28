package app.allever.android.sample.ipc.aidl

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.ProcessHelper
import app.allever.android.sample.ipc.ICalc

class AidlService: Service() {
    private val TAG = "AidlService"
    // 使用 object 表达式直接实现 AIDL 的 Stub
    private val mBinder = object : ICalc.Stub() {
        override fun basicTypes(
            anInt: Int,
            aLong: Long,
            aBoolean: Boolean,
            aFloat: Float,
            aDouble: Double,
            aString: String?
        ) {
            log( TAG, "basicTypes: $anInt, $aLong, $aBoolean, $aFloat, $aDouble, $aString in process: ${ProcessHelper.getProcessName()}")
        }

        override fun add(a: Int, b: Int): Int {
            val result = a + b
            log( TAG, "add: $result in process: ${ProcessHelper.getProcessName()}")
            return a + b
        }
        override fun multiply(a: Int, b: Int): Int {
            val result = a * b
            log( TAG, "multiply: $result in process: ${ProcessHelper.getProcessName()}")
            return result
        }
    }
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        log( TAG, "onCreate in process: ${ProcessHelper.getProcessName()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        log( TAG, "onDestroy in process: ${ProcessHelper.getProcessName()}")
    }

    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        log( TAG, "bindService in process: ${ProcessHelper.getProcessName()}")
        return super.bindService(service, conn, flags)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log( TAG, "onUnbind in process: ${ProcessHelper.getProcessName()}")
        return super.onUnbind(intent)
    }
}