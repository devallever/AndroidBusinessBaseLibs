package app.allever.android.lib.network.core.util

import android.util.Log
import app.allever.android.lib.network.core.NetCore

object NetLogger {
    private val TAG = NetLogger::class.java.simpleName
    private val logEnabled by lazy {
        NetCore.config.logEnabled
    }
    fun log(msg: String) {
        if (logEnabled) {
            Log.d(TAG, msg)
        }
    }

    fun log(tag: String, msg: String) {
        if (logEnabled) {
            Log.d(tag, msg)
        }
    }

    fun logE(msg: String) {
        if (logEnabled) {
            Log.e(TAG, msg)
        }
    }

    fun logE(tag: String, msg: String) {
        if (logEnabled) {
            Log.e(tag, msg)
        }
    }
}