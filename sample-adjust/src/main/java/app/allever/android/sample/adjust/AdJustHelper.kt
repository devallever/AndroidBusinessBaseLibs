package app.allever.android.sample.adjust

import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.AdjustEvent
import com.adjust.sdk.AdjustPlayStoreSubscription
import com.adjust.sdk.LogLevel
import com.adjust.sdk.OnEventTrackingFailedListener
import com.adjust.sdk.OnEventTrackingSucceededListener
import com.adjust.sdk.OnSessionTrackingFailedListener
import com.adjust.sdk.OnSessionTrackingSucceededListener

/**
 * https://dev.adjust.com/zh/sdk/android
 */
object AdJustHelper {
    private var isInit = false
    fun init(appToken: String) {
        if (isInit) {
            return
        }
        val environment = if (App.DEBUG) {
            AdjustConfig.ENVIRONMENT_SANDBOX
        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }
        val config = AdjustConfig(App.context, appToken, environment)
        config.setLogLevel(LogLevel.VERBOSE)
        config.onSessionTrackingSucceededListener = OnSessionTrackingSucceededListener {
            log("Adjust session tracking succeeded")
        }
        config.onSessionTrackingFailedListener = OnSessionTrackingFailedListener {
            logE("Adjust session tracking failed: ${it.message}")
        }
        config.onEventTrackingSucceededListener = OnEventTrackingSucceededListener {
            log("Adjust event tracking succeeded")
        }
        config.onEventTrackingFailedListener = OnEventTrackingFailedListener {
            logE("Adjust event tracking failed: ${it.message}")
        }
        Adjust.initSdk(config)
        isInit = true
    }

    fun trackEvent(event: String) {
        val event = AdjustEvent(event)
        Adjust.trackEvent(event)
    }

    fun trackAdRevenue(source: String, revenue: Double, currency: String) {
        val adjustEvent = AdjustAdRevenue(source)
        adjustEvent.setRevenue(revenue, currency)// adjustEvent.setRevenue(0.1, "USD")
        Adjust.trackAdRevenue(adjustEvent)
        log("Adjust trackAdRevenue: $source")

//        val adjustEvent = AdjustEvent("abc123")
//        adjustEvent.setRevenue(0.01, "EUR")
//        Adjust.trackEvent(adjustEvent)
    }

    //trackPlayStoreSubscription
    fun trackPlayStoreSubscription() {
        val subscription = AdjustPlayStoreSubscription(
            0,//price 0.01
            "", //currency "EUR"
            "sku", //产品 ID
            "orderId",
            "signature",//购买数据的签名
            "purchaseToken" //交易唯一识别码。请参考Google 的文档，了解更多信息
        )
        subscription.purchaseTime = System.currentTimeMillis()

        Adjust.trackPlayStoreSubscription(subscription)
    }

}