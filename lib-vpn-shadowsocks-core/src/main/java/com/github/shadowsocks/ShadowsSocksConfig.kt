package com.github.shadowsocks

import com.github.shadowsocks.core.BuildConfig

object ShadowsSocksConfig {
    var connectTime = 45 * 60 * 1000L
    var appendTime = 60 * 60 * 1000L
    var autoStopMode = false
    var pkg = ""

    var appName = ""

    var notificationIcon = -1

    var tickerSuccess = ""

    var notificationMainClz: Class<*>? = null

    /**
     *   <string name="traffic">%1$s↑\t%2$s↓</string>
     *   <string name="speed">%s/s</string>
     */
    var notificationTraffic = -1
    var notificationSpeed = -1
}