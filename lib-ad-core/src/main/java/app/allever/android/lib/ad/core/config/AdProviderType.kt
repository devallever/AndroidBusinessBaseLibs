package app.allever.android.lib.ad.core.config

@Deprecated(
    message = "Use String instead of AdProviderType enum for better extensibility. " +
            "Each provider module should define its own PROVIDER_NAME constant.",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("String")
)
enum class AdProviderType {
    NONE,
    APPLOVIN,
    PANGLE,
    ADMOB,
    BIGO,
    GROMORE,
    CUSTOM
}
