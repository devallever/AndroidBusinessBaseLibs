package app.flash.tunnel.vpn

import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.lib.common.util.DeviceManager

object Constants {
    const val PRIVACY_URL = "https://baidu.com"

    const val CONNECT_TIME_OUT = (1 + 6) * 1000L
    const val LOADING_DELAY_CONNECT = (5 + 1) * 1000L
    const val LOADING_DELAY_DEFAULT = (5 + 1) * 1000L
    const val SPLASH_DELAY = 1000L
    val CONNECT_TIME = if (App.DEBUG) 45 * 60 * 1000L else 30 * 60 * 1000L

    const val KEY_SMART_MODE = "ksm_fl"
    const val KEY_LOCAL_RESPONSE = "klr_fl"

    const val EXTRA_NEED_CHANGE_CONNECT_NODE = "needChangeConnectNode"
    const val EXTRA_SHOW_CONNECT_FAIL = "showConnectFail"
    const val EXTRA_WEB_URL = "url"
    const val EXTRA_WEB_TITLE = "title"
    const val EXTRA_REWARD_CANCEL = "EXTRA_REWARD_CANCEL_FT"
    const val EXTRA_SHOW_ADD_TIME_SUCCESS_DIALOG = "EXTRA_SHOW_ADD_TIME_SUCCESS_DIALOG_FT"
    const val EXTRA_SHOW_ADD_TIME_FAIL_DIALOG = "EXTRA_SHOW_ADD_TIME_FAIL_DIALOG_FT"


    const val ASSETS_ROOT: String = "file:///android_asset/"

    private val did = DeviceManager.getAndroidId(TunnelApp.context)
    private val mcc = DeviceManager.getMcc(TunnelApp.context)
    private val vCode = 1
    private val pkg = TunnelApp.context.packageName

    val API by lazy {
        "https://ft.flash-tunnel.com/tunnel/files?pgft=${pkg}&dift=${did}&mcft=${mcc}&vcft=${vCode}"
    }
}