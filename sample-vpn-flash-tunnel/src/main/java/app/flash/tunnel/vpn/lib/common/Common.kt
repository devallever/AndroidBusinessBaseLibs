package app.flash.tunnel.vpn.lib.common

import android.annotation.SuppressLint
import android.content.Context
import app.allever.android.lib.core.app.App

@SuppressLint("StaticFieldLeak")

object Common {
    lateinit var context: Context
    internal val DEBUG = App.DEBUG
    fun init(context: Context) {
        Common.context = context
    }
}