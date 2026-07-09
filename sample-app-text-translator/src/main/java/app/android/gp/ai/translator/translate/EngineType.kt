package app.android.gp.ai.translator.translate

class EngineType {
    companion object {
        val BAIDU = 1
        val GOOGLE = 2

        fun getEngineName(type: Int) = when (type) {
            BAIDU -> {
                "Baidu"
            }
            GOOGLE -> {
                "Google"
            }
            else -> {
                "Baidu"
            }
        }
    }
}