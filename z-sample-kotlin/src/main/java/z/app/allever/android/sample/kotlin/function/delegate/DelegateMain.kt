package z.app.allever.android.sample.kotlin.function.delegate

fun main() {
    val p by Delegate()
    p?.name = "Allever"
    println("name = ${p?.name}")
}