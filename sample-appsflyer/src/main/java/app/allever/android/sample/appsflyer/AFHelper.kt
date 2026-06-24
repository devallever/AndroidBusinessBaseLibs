package app.allever.android.sample.appsflyer

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener

/**
 *
 * https://dev.appsflyer.com/hc/docs/android-sdk
 */
object AFHelper {
    private var isInit = false
    private var isInitializing = false
    fun init(key: String) {
        if (isInit) {
            return
        }
        if (isInitializing) {
            return
        }
        isInitializing = true
        val listener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(p0: Map<String?, Any?>?) {

            }

            override fun onConversionDataFail(p0: String?) {
            }

            override fun onAppOpenAttribution(p0: Map<String?, String?>?) {
            }

            override fun onAttributionFailure(p0: String?) {
            }

        }
        AppsFlyerLib.getInstance().init(key, listener, App.context)
        AppsFlyerLib.getInstance().start(App.app, key, object : AppsFlyerRequestListener {
            override fun onSuccess() {
                log("AppsFlyer init success")
                isInit = true
                isInitializing = false
            }

            override fun onError(code: Int, message: String) {
                logE(" AppsFlyer start error $code -> $message")
                isInitializing = false
            }

        })
    }

    fun trackEvent(eventName: String, block: MutableMap<String, Any>.() -> Unit = {}) {
        val map = mutableMapOf<String, Any>()
        map.block()
        AppsFlyerLib.getInstance()
            .logEvent(App.context, eventName, map, object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    log("AppsFlyer logEvent success")
                }

                override fun onError(code: Int, message: String) {
                    logE(" AppsFlyer logEvent error $code -> $message")
                }

            })
    }
}