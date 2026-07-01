package app.flash.tunnel.vpn.helper

class LogScene {
    companion object {
        private const val PRE = "FT-"
        const val DEFAULT = "${PRE}Default"
        const val RETRY = "${PRE}retry"
        const val VPN_STATE_CHANGE = "${PRE}stateChange"
        const val BTN_CONNECT = "${PRE}btnConnect"
        const val PING_FAIL = "${PRE}ping fail"
        const val SWITCH_NODE_BY_LIST = "${PRE}switchNode by List"
        const val FIRST_AGREE_VPN_PERMISSION = "${PRE}first agree vpn permission"
        const val FIREBASE_REMOTE_CONFIG = "${PRE}firebase remote config"
        const val VPN_STATE_STOP = "${PRE}serviceState state stop"
        const val CURRENT_NODE_LIVEDATA = "${PRE}currentNodeLiveData"
        const val AD_FINISH = "${PRE}ad finish"
        const val LOADING_PROGRESS_FINISH = "progress finish"
        const val TIME_OUT = "${PRE}timeout"
        const val NODE_LIST_LIVEDATA = "${PRE}node list livedata"
        const val API = "API"

    }
}