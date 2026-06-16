package app.flash.tunnel.vpn.helper

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import app.allever.android.lib.core.app.App
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.lib.common.util.DeviceManager
import app.flash.tunnel.vpn.lib.common.util.StoreManager
import app.flash.tunnel.vpn.lib.common.util.log

@SuppressLint("StaticFieldLeak")
object EventHelper {

    var newUser = false
    private const val KEY_FIRST_INSTALL = "KEY_FIRST_INSTALL"
    var referrerSValue = ReferSValue.NONE

    var evsValue = EvsValue.HOME
    var ssTimeStart = 0L
    var iLTimeStart = 0L
    var configResultValue = ConfigResultValue.NONE_RESULT
    var logEnterMain = false
    var launchTimeStart = 0L

//    private val mFirebaseAnalytics = FirebaseAnalytics.getInstance(TunnelApp.context)

    fun init() {
        newUser = if (App.DEBUG) true else StoreManager.getBoolean(KEY_FIRST_INSTALL, true)
        StoreManager.putBoolean(KEY_FIRST_INSTALL, false)
        logLaunch()
    }

    fun logLaunch() {
        logEvent(Event.launch)
    }

    fun logClickConnect() {
        logEvent(Event.clickC) {
            /**
             *   1. evs事件来源，表明触发VPN来源。
             */
            it.putInt(Params.evs, evsValue)
        }
    }

    fun logConnectSuccess(usedTime: Long) {
        logEvent(Event.connectSs) {
            /**
             *   1. ssTime 从用户触发连接到连接节点成功所花费的时间。
             */
            it.putInt(Params.ssTime, usedTime.toInt())
        }
    }

    fun logTriggerLoadInterAd(adPosition: Int) {
        logEvent(Event.triggerIA) {
            /**
             *   1. adP 广告位置
             */
            it.putInt(Params.adP, adPosition)
        }
    }

    fun logLoadInterAd(adPosition: Int, usedTime: Long, resultValue: Int) {
        logEvent(Event.loadIA) {
            /**
             *   1. adP 广告位置
             *   2. iLTime 本次广告加载花费时间。
             *   3. result 广告加载结果代码。
             */
            it.putInt(Params.adP, adPosition)
            it.putInt(Params.iLTime, usedTime.toInt())
            it.putInt(Params.result, resultValue)
        }
    }

    fun logShowInterAd(adPosition: Int, usedTime: Long) {
        logEvent(Event.showIA) {
            /**
             *   1. adP 广告位置
             *   2. iSTime 从用户触发广告到广告展示所花费的时间，如果是缓存广告则值为0。
             */
            it.putInt(Params.adP, adPosition)
            it.putInt(Params.iSTime, usedTime.toInt())
        }
    }

    fun logAgreeVpnPermission(evsValue: Int) {
        logEvent(Event.agreeV) {
            /**
             *   1. evs事件来源，表明触发VPN来源。
             *   2. result 拉取节点是否成功，1代表成功，0代表还没有结果，-1代表已经拉取失败。
             */
            it.putInt(Params.evs, evsValue)
            it.putInt(Params.result, configResultValue)
        }
    }

    fun logFirstEnterHome(usedTime: Long) {
        logEvent(Event.vMainP) {
            /**
             *   1. evtime 从冷启动到完成展示主页的时间，单位毫秒。
             */
            it.putInt(Params.evtime, usedTime.toInt())
        }
    }

    fun logReturnApp() {
        logEvent(Event.reBack)
    }

    fun logFetchConfig(usedTime: Long, resultValue: Int, configTypeValue: Int) {
        logEvent(Event.cfgEnd) {
            /**
             *   1. evtime 从冷启动到拉取完成的时间，单位毫秒。
             *   2. result 拉取结果，1代表成功，-1代表拉取失败。
             *   3. cfgtyp 拉取配置的方式，0代表接口，1代表firebase。
             */
            it.putInt(Params.evtime, usedTime.toInt())
            it.putInt(Params.result, resultValue)
            it.putInt(Params.cfgtyp, configTypeValue)
        }
    }

    fun logConnectEnd() {
        logEvent(Event.cnnEnd)
    }

    fun logConnectingEnterBg(usedTime: Long) {
        logEvent(Event.cnnTBg) {
            /**
             *   1. evtime 从用户触发连接VPN到用户进入后台的时间，单位毫秒。
             */
            it.putInt(Params.evtime, usedTime.toInt())
        }
    }

    fun logRefIds(referrerString: String) {
        logEvent(Event.refIds) {
            /**
             *   1. referr facebook渠道该参数上传空字符串，非facebook渠道的refer字段十条抽一条上报剩下上报空字符串，
             *   注意，如果通过firebase方式上报需要根据firebase文档对字符串进行截断。
             */
            it.putString(Params.referr, referrerString)
        }
    }


    /**
     * common params
     *         const val did = "did"
     *         const val mcc = "mcc"
     *         const val nip = "nip"
     *         const val npt = "npt"
     *         const val appVer = "appVer"
     *         const val sysVer = "sysVer"
     *         const val referS = "referS"
     *         const val vState = "vState"
     */
    private fun logEvent(eventName: String, block: (bundle: Bundle) -> Unit = {}) {
        val bundle = Bundle()
        val did = DeviceManager.getAndroidId(TunnelApp.context)
        val mcc = DeviceManager.getMcc(TunnelApp.context)
        val nip = getConnectNodeIp()
        val npt = getConnectNodePort()
        val appVer = "1.0"
        val sysVer = Build.VERSION.SDK_INT
        val referS = referrerSValue
        val vState =
            if (TunnelHelper.isServiceConnected()) VStatusValue.CONNECTED else VStatusValue.DIS_CONNECTED
        //add public params
        bundle.putString(Params.did, did)
        bundle.putInt(Params.mcc, mcc)
        bundle.putString(Params.nip, nip)
        bundle.putInt(Params.npt, npt)
        bundle.putString(Params.appVer, appVer)
        bundle.putInt(Params.sysVer, sysVer)
        bundle.putInt(Params.referS, referS)
        bundle.putInt(Params.vState, vState)

        block(bundle)
        if (TunnelApp.DEBUG) {
            log("logEvent[$eventName]")
//            bundle.keySet().map {
//                val value = bundle.get(it)
//                log("logEvent[$eventName]: key: $it, value: $value")
//            }
        } else {
            if (newUser) {
//                mFirebaseAnalytics.logEvent(eventName, bundle)
            }
        }

    }

    private fun getConnectNodeIp(): String {
        if (!TunnelHelper.isServiceConnected()) {
            return ""
        }

        return TunnelHelper.getSelectedNodeItem()?.entity?.host ?: ""

    }

    private fun getConnectNodePort(): Int {
        if (!TunnelHelper.isServiceConnected()) {
            return 0
        }

        return TunnelHelper.getSelectedNodeItem()?.entity?.remotePort ?: 0

    }

    //xor secret key = kftxorj
    object Event {
        /**
         * launch
         * clickC
         * connectSs
         * triggerIA
         * loadIA
         * showIA
         * agreeV
         * vMainP
         * reBack
         * cfgEnd
         * cnnEnd
         * cnnTBg
         * refIds
         */
        const val launch = "BwcBFgwa"
        const val clickC = "CAodGwQx"
        const val connectSs = "CAkaFgoRHjgV"
        const val triggerIA = "HxQdHwgXGCIn"
        const val loadIA = "BwkVHCYz"
        const val showIA = "GA4bDyYz"
        const val agreeV = "CgEGHQok"
        const val vMainP = "HSsVEQEi"
        const val reBack = "GQM2GQwZ"
        const val cfgEnd = "CAATPQEW"
        const val cnnEnd = "CAgaPQEW"
        const val cnnTBg = "CAgaLC0V"
        const val refIds = "GQMSMQsB"
    }

    object Params {
        //pub params
        //2.0.0
        const val did = "Dw8Q"
        const val mcc = "BgUX"
        const val nip = "BQ8E"
        const val npt = "BRYA"
        //2.0.1
        const val appVer = "ChYELgoA"
        const val sysVer = "GB8HLgoA"
        const val referS = "GQMSHR0h"
        const val vState = "HTUAGRsX"

        //2.0.0
        const val ssTime = "GBUgEQIX"
        const val adP = "CgIk"
        const val iLTime = "AiogEQIX"
        const val result = "GQMHDQMG"
        const val iSTime = "AjUgEQIX"
        //2.0.1
        const val evs = "DhAH"
        const val evtime = "DhAAEQIX"
        const val cfgtyp = "CAATDBYC"
        const val referr = "GQMSHR0A"
    }

    object AdPositionValue {
        /**
         * 1代表连接成功插页，
         * 2代表返回应用插页，
         * 3代表断开连接插页，
         * 11代表首页横幅，
         * 21代表加时激励视频，
         * 31代表首页原生，
         * 32代表列表页原生，
         * 33代表加时弹窗原生，
         * 34代表连接成功页原生，
         * 35代表断开总结页原生
         */
        const val CONNECT_SUCCESS_INTER = 1
        const val RETURN_APP_INTER = 2
        const val DISCONNECT_INTER = 3
        const val HOME_BANNER = 11
        const val ADD_TIME_REWARD = 21
        const val HOME_NATIVE = 31
        const val LIST_NATIVE = 32
        const val ADD_TIME_NATIVE = 33
        const val CONNECT_SUCCESS_NATIVE = 34
        const val DISCONNECT_NATIVE = 35
    }


    /***
     * 1代表首页按钮，
     * 2代表列表页，失败后的重试弹窗对VPN触发来源没有影响。
     */
    object EvsValue {
        const val HOME = 1
        const val LIST = 2
    }

    /**
     * 用户来源渠道，1代表fb，2代表其他渠道。
     */
    object ReferSValue {
        const val NONE = 0
        const val FB = 1
        const val OTHER = 2
    }

    object VStatusValue {
        const val CONNECTED = 1
        const val DIS_CONNECTED = 0
    }

    /**
     * 拉取节点是否成功，
     * 1代表成功，
     * 0代表还没有结果，
     * -1代表已经拉取失败。
     */
    object ConfigResultValue {
        const val SUCCESS = ResultValue.SUCCESS
        const val NONE_RESULT = 0
        const val FAIL = ResultValue.FAIL
    }

    object ResultValue {
        const val SUCCESS = 1
        const val FAIL = -1
    }

    object AdResultValue {
        const val SUCCESS = -1
    }

    /***
     * 拉取配置的方式，
     * 0代表接口，
     * 1代表firebase。
     */
    object ConfigTypeValue {
        const val API = 0
        const val FIREBASE = 1
    }

    object ServerSource {
        const val NON_FETCH_NODE = 0
        const val API = 1
        const val FIREBASE = 2
        const val LOCAL = 3
    }
}