package app.allever.android.lib.ad.core.strategy

import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE

object AdLog {
    private val logBuilder = StringBuilder()

    fun logMessage(
        message: String,
        providerType: String = "",
        adType: AdType? = null,
        strategyName: String? = null,
        action: String? = null,
        isPreload: Boolean = false,
        success: Boolean? = null,
    ) {
        //all !null
        logBuilder.clear()
        if (providerType.isNotEmpty()) {
            logBuilder.append("[$providerType] ")
        }
        if (adType != null) {
            logBuilder.append("[${adType.name}] ")
        }
        if (strategyName != null) {
            logBuilder.append("[$strategyName] ")
        }
        if (isPreload) {
            logBuilder.append("[PRELOAD] ")
        }
        if (action != null) {
            logBuilder.append("[$action] ")
        }
        val realMessage = if (success == null) {
            message
        } else {
            if (success) {
                "✅ $message"
            } else {
                "❌ ERROR: $message"
            }
        }

        logBuilder.append(realMessage)

        if (success == false) {
            logE(logBuilder.toString())
        } else {
            log(logBuilder.toString())
        }
    }

}
