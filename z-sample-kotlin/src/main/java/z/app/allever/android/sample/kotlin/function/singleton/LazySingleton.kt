package z.app.allever.android.sample.kotlin.function.singleton

class LazySingleton {

    fun method() {
        println("LazySingleton")
    }

    companion object {
        val INS by lazy {
            LazySingleton()
        }
    }

}