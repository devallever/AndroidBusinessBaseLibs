package com.alsg.bakericon

import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object AppScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

    fun runOnUiThread(block: () -> Unit) {
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            block()
        } else {
            launch(Dispatchers.Main) {
                block()
            }
        }
    }
}