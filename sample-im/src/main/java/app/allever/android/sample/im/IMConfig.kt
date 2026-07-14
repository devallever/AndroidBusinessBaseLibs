package app.allever.android.sample.im

import app.allever.android.lib.core.store.StoreCore

object IMConfig {
    private val KEY_LOGIN_USER = "KEY_LOGIN_USER"
    private val SP_KEY_WS_URL = "SP_KEY_WS_URL"

    private val SP_KEY_HTTP_URL = "SP_KEY_HTTP_URL"

    private val SP_KEY_SERVER_IP = "SP_KEY_SERVER_IP"
    fun isLogin() = getLoginUser().isNotEmpty()

    fun getLoginUser() = StoreCore.getString(KEY_LOGIN_USER, "")?:""

    fun saveUser(username: String) = StoreCore.putString(KEY_LOGIN_USER, username)

    fun saveServerIp(ip: String) {
        StoreCore.putString(SP_KEY_SERVER_IP, ip)
        saveHttpBaseUrl("http://$ip:8080")
        saveWebsocketUrl("ws://$ip:5400")
    }

    fun getServerIp() = StoreCore.getString(SP_KEY_SERVER_IP, "192.168.43.53")

    fun getWebsocketUrl() = StoreCore.getString(SP_KEY_WS_URL, "ws://192.168.43.53:5400")

    fun saveWebsocketUrl(url: String) = StoreCore.putString(SP_KEY_WS_URL, url)

    fun getConnectWebsocketUrl(username: String = getLoginUser()) = "${getWebsocketUrl()}?username=${username}"

    fun getHttpBaseUrl() = StoreCore.getString(SP_KEY_HTTP_URL, "http://192.168.0.1:8080")?:"http://192.168.0.1:8080"

    fun saveHttpBaseUrl(url: String) = StoreCore.putString(SP_KEY_HTTP_URL, url)
}