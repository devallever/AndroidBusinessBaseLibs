package com.allever.daymatter

import app.allever.android.lib.core.app.App
import org.litepal.LitePal

/**
 * Created by Allever on 18/5/21.
 */

object MyApp{
    private var isInit = false
    fun onCreate() {
        if (isInit) {
            return
        }
        LitePal.initialize(App.context)
        isInit = true
    }
}
