package app.flash.tunnel.vpn.lib.common.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent

object ActivityManager {
    fun start(
        context: Context,
        clazz: Class<*>,
        requestCode: Int = 0,
        block: Intent.() -> Unit = {}
    ) {
        val intent = Intent(context, clazz).apply {
            val isApplication = context is Application
            if (isApplication) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            block()
        }

        if (context is Activity) {
            context.startActivityForResult(intent, requestCode)
        } else {
            context.startActivity(intent)
        }
    }
}