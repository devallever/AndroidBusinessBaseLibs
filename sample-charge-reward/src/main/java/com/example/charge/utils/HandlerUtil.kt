package com.example.charge.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

object HandlerUtil {
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val workHandler by lazy {
        val handlerThread = HandlerThread("work_handler_thread")
        handlerThread.start()
        Handler(handlerThread.looper)
    }

    fun main() = mainHandler

    fun work() = workHandler

}