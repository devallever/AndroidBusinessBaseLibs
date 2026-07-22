package app.allever.android.lib.router.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Route(
    val path: String,
    val name: String = "",
    val export: Boolean = true
)