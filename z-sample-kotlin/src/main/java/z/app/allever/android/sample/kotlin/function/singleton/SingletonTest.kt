package z.app.allever.android.sample.kotlin.function.singleton

import z.app.allever.android.sample.kotlin.function.singleton.DCLSingleton

fun main() {
    HungrySingleton.method()
    LazySingleton.INS.method()
    DCLSingleton.getIns().method()
    StaticInnerSingleton.getIns().method()
}