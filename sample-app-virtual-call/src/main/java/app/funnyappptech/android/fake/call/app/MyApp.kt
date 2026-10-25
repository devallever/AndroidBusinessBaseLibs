package app.funnyappptech.android.fake.call.app


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