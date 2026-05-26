package app.allever.android.lib.ad.core

object AdCore {

    @Deprecated(
        message = "Use AdManager instead",
        replaceWith = ReplaceWith("AdManager", "app.allever.android.lib.ad.core.AdManager")
    )
    fun getVersion(): String = AdManager.VERSION
}
