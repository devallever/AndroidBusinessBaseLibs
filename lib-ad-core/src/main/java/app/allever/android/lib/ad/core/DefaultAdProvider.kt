package app.allever.android.lib.ad.core

import android.content.Context
import app.allever.android.lib.ad.core.base.BaseAdProvider
import app.allever.android.lib.ad.core.config.AdProviderConfig

class DefaultAdProvider : BaseAdProvider() {
    override fun onDestroy() {

    }

    companion object {
        const val PROVIDER_NAME = "DEFAULT"
    }

    override fun getProviderType() = PROVIDER_NAME

    override fun init(
        context: Context,
        config: AdProviderConfig,
        callback: (() -> Unit)?
    ) {

    }
}