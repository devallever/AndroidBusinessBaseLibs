package app.allever.android.lib.ad.core.strategy

import app.allever.android.lib.ad.core.type.AdType
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE

internal object AdLog {
    private val threadLocalBuilder = ThreadLocal.withInitial { StringBuilder() }

    fun logMessage(
        message: String,
        providerType: String = "",
        adType: AdType? = null,
        strategyName: String? = null,
        action: String? = null,
        isPreload: Boolean = false,
        success: Boolean? = null,
    ) {
        val builder = threadLocalBuilder.get()
        builder?: return
        builder.clear()
        
        if (providerType.isNotEmpty()) {
            builder.append("[$providerType] ")
        }
        if (adType != null) {
            builder.append("[${adType.name}] ")
        }
        if (strategyName != null) {
            builder.append("[$strategyName] ")
        }
        if (isPreload) {
            builder.append("[PRELOAD] ")
        }
        if (action != null) {
            builder.append("[$action] ")
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

        builder.append(realMessage)

        if (success == false) {
            logE(builder.toString())
        } else {
            log(builder.toString())
        }
    }

}
