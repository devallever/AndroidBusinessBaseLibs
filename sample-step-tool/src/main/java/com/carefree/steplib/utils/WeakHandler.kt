package com.carefree.steplib.utils

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

class WeakHandler(handler: IHandler,looper: Looper = Looper.getMainLooper()) : Handler(looper) {
    interface IHandler {
        fun handleMsg(msg: Message)
    }

    private val mRef: WeakReference<IHandler> = WeakReference(handler)

    override fun handleMessage(msg: Message) {
        mRef.get()?.handleMsg(msg)
    }
}