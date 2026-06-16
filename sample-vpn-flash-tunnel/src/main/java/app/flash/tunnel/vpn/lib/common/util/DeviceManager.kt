package app.flash.tunnel.vpn.lib.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import android.telephony.TelephonyManager

object DeviceManager {
    @SuppressLint("HardwareIds")
    fun getAndroidId(context: Context): String {
        val resolver = context.contentResolver
        return Secure.getString(resolver, Secure.ANDROID_ID)
    }

    fun getMcc(context: Context): Int {
        val operator =
            (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simOperator
        val mcc = if (operator.length > 3) {
            operator.substring(0, 3).toIntOrNull()?:0
        } else {
            0
        }
        return mcc
    }
}