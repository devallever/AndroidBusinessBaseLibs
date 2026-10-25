package app.funnyappptech.android.fake.call.app

import app.allever.android.lib.core.app.App


class MyApp: App() {
    override fun init() {
        FakeCall.initWallPagerData()
    }
}