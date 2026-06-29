package app.allever.android.sample.ipc.binder.client

import android.os.IBinder
import android.os.Parcel
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.helper.ProcessHelper
import app.allever.android.sample.ipc.binder.service.BinderService

/**
 * 代理类，封装 Binder 的 transact 调用
 */
class BinderProxy(private val binder: IBinder) {

    private val TAG = "BinderProxy"
    fun add(a: Int, b: Int): Int {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInt(a)
            data.writeInt(b)
            log(TAG, "add: $a + $b in process: ${ProcessHelper.getProcessName()}")
            binder.transact(BinderService.Companion.CODE_ADD, data, reply, 0)
            reply.readInt()
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        } finally {
            data.recycle()
            reply.recycle()
        }
    }
    fun multiply(a: Int, b: Int): Int {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInt(a)
            data.writeInt(b)
            log(TAG, "multiply: $a * $b in process: ${ProcessHelper.getProcessName()}")
            binder.transact(BinderService.Companion.CODE_MULTIPLY, data, reply, 0)
            reply.readInt()
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        } finally {
            data.recycle()
        }
    }
}