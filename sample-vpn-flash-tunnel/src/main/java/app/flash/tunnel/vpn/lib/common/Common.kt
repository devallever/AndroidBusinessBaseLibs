package app.flash.tunnel.vpn.lib.common

import android.annotation.SuppressLint
import app.allever.android.lib.core.app.App

@SuppressLint("StaticFieldLeak")

object Common {
    val context by lazy {
        App.context
    }
    internal val DEBUG = App.DEBUG
}