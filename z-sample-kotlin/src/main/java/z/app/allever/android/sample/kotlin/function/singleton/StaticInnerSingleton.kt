package z.app.allever.android.sample.kotlin.function.singleton

class StaticInnerSingleton {

    fun method() {
        println("StaticInnerSingleton")
    }

    companion object {
        fun getIns(): StaticInnerSingleton  = Holder.INS
    }

    private object Holder {
        @JvmStatic
        val INS = StaticInnerSingleton()
    }
}