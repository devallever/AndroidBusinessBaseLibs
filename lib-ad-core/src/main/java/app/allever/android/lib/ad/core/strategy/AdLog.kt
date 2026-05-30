package app.allever.android.lib.ad.core.strategy

object AdLog {

    const val PREFIX_LOAD = "[LOAD]"
    const val PREFIX_PRELOAD = "[PRELOAD]"
    const val PREFIX_CACHE = "[CACHE]"
    const val PREFIX_BIDDING = "[BIDDING]"
    const val PREFIX_WATERFALL = "[WATERFALL]"
    const val PREFIX_SINGLE = "[SINGLE]"

    fun format(
        tag: String,
        prefix: String,
        message: String,
        isPreload: Boolean = false
    ): String {
        val actualPrefix = if (isPreload) "$PREFIX_PRELOAD-$prefix" else prefix
        return "$tag: $actualPrefix $message"
    }

    fun formatError(
        tag: String,
        prefix: String,
        message: String,
        isPreload: Boolean = false
    ): String {
        return format(tag, prefix, "❌ ERROR: $message", isPreload)
    }

    fun formatSuccess(
        tag: String,
        prefix: String,
        message: String,
        isPreload: Boolean = false
    ): String {
        return format(tag, prefix, "✅ $message", isPreload)
    }

    fun formatTimeout(
        tag: String,
        prefix: String,
        timeoutMs: Long,
        isPreload: Boolean = false
    ): String {
        return formatError(tag, prefix, "⏰ TIMEOUT! (${timeoutMs}ms)", isPreload)
    }
}
