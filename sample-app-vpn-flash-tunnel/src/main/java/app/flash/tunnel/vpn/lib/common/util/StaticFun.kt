package app.flash.tunnel.vpn.lib.common.util

import android.util.Log
import android.widget.Toast
import app.flash.tunnel.vpn.lib.common.Common

fun toast(msg: String) {
    Toast.makeText(Common.context, msg, Toast.LENGTH_SHORT).show()
}

fun log(msg: String) {
    if (!Common.DEBUG) {
        return
    }
    Log.d("CommonLog", msg)
}