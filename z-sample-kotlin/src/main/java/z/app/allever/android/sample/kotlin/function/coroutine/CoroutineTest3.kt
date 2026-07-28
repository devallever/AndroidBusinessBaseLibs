package z.app.allever.android.sample.kotlin.function.coroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() {
    GlobalScope.launch {
        println("run in scope")
    }
}