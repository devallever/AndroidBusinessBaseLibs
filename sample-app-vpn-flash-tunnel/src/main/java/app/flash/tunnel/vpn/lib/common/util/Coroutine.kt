package app.flash.tunnel.vpn.lib.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Coroutine {
    val IO by lazy {
        CoroutineScope(Dispatchers.IO)
    }
    val MAIN by lazy {
        CoroutineScope(Dispatchers.Main)
    }
    val DEFAULT by lazy {
        CoroutineScope(Dispatchers.Default)
    }
}

fun runInIoDispatcher(block: suspend CoroutineScope.() -> Unit) {
    Coroutine.IO.launch {
        block()
    }
}

fun runInMainDispatcher(block: suspend CoroutineScope.() -> Unit) {
    Coroutine.MAIN.launch {
        block()
    }
}