package app.flash.tunnel.vpn.helper

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.lib.common.util.DeviceManager
import app.flash.tunnel.vpn.lib.common.util.log
//import com.google.firebase.analytics.FirebaseAnalytics

@SuppressLint("StaticFieldLeak")
object PaidEventHelper {


//    private val mFirebaseAnalytics = FirebaseAnalytics.getInstance(TunnelApp.context)

    fun logAdPaid(type: Int, value: Long) {
        logEvent(Event.AD_PAID) {
            it.putInt(Params.type, type)
            it.putLong(Params.a_v, value)
        }
    }

    private fun logEvent(eventName: String, block: (bundle: Bundle) -> Unit = {}) {
        val bundle = Bundle()
        val did = DeviceManager.getAndroidId(TunnelApp.context)
        val mcc = DeviceManager.getMcc(TunnelApp.context)
        val versionName = "1.0"
        val ip = getConnectNodeIp()
        val androidVersion = Build.VERSION.SDK_INT
        //add public params
        bundle.putString(Params.did, did)
        bundle.putString(Params.mcc, mcc.toString())
        bundle.putString(Params.version_code, versionName)
        bundle.putString(Params.node_ip, ip)
        bundle.putInt(Params.android_version, androidVersion)
        bundle.putInt(Params.first_install_se, if (EventHelper.newUser) 1 else 0)
        bundle.putString(Params.fb_aid, ReferrerHelper.facebookReferrerDecryption?.adId?.toString()?:"")
        bundle.putString(Params.fb_cid, ReferrerHelper.facebookReferrerDecryption?.campaignId?.toString()?:"")
        bundle.putString(Params.fb_c_g_id, ReferrerHelper.facebookReferrerDecryption?.campaignGroupId?.toString()?:"")

        block(bundle)
        if (TunnelApp.DEBUG) {
            log("logEvent[$eventName]")
//            bundle.keySet().map {
//                val value = bundle.get(it)
//                log("PaidLogEvent[$eventName]: key: $it, value: $value")
//            }
        } else {
//            mFirebaseAnalytics.logEvent(eventName, bundle)
        }

    }

    private fun getConnectNodeIp(): String {
        if (!TunnelHelper.isServiceConnected()) {
            return "null"
        }

        return TunnelHelper.getSelectedNodeItem()?.entity?.host ?: "null"

    }

    //xor secret key = kftxorj
    object Event {
        const val AD_PAID = "CgIkGQYW"
    }

    object Params {
        //pub params
        const val did = "Dw8Q"
        const val mcc = "BgUX"
        const val version_code = "HTkXFwsX"
        const val node_ip = "BQkQHQYC"
        const val android_version = "CggrDgoA"
        const val first_install_se = "DTkdJxwX"
        const val fb_aid = "DQQrGQYW"
        const val fb_cid = "DQQrGwYW"
        const val fb_c_g_id = "CDkTJwYW"

        const val type = "type"

        //adPaid params
        const val a_v = "a_v"

    }

    object AdType {
        const val INTER = 1
        const val REWARD = 2
        const val NATIVE = 4
        const val BANNER = 5
    }
}