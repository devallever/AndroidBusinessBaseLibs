package com.allever.stealthcamera

import app.allever.android.lib.core.app.App

object MyApp{
    fun onCreate() {
        com.android.absbase.App.setContext(App.context)
    }
}