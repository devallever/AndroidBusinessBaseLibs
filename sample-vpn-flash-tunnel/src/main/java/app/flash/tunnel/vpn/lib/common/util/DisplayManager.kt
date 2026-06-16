package app.flash.tunnel.vpn.lib.common.util

import android.util.DisplayMetrics
import app.flash.tunnel.vpn.lib.common.Common

object DisplayManager {
    fun dip2px(dip: Int): Int {
        val displayMetrics = Common.context.resources.displayMetrics
        val density = displayMetrics.density
        return (dip * density + 0.5f).toInt()
    }

    private fun getDisplayMetrics(): DisplayMetrics {
        val resource = Common.context.resources
        return resource.displayMetrics
    }

    fun getScreenWidth(): Int {
        return getDisplayMetrics().widthPixels
    }

}