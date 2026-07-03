package org.xm.app.virtual.call.app


object MyApp {

    private var isInit = false
    fun onCreate() {
        if (isInit) {
            return
        }
        Global.initWallPagerData()
        isInit = true
    }
}