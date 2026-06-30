package app.android.allever.gp.quick.project.util

import app.allever.android.lib.core.ext.log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.net.URL

object IPHelper {

    fun getInternalIp() : String{
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address.isSiteLocalAddress) {
                    val ip = address.hostAddress?:"192.168.0.1"
                    log("getInternalIp: $ip")
                    return ip
                }
            }
        }

        return "192.168.0.1"
    }

    fun getExternalIP(): String {
        try {
            // 使用公共服务获取外部IP，例如 ipify.org
            val whatismyip = URL("http://api.ipify.org")
            val yc = whatismyip.openConnection()
            val `in` = BufferedReader(InputStreamReader(yc.getInputStream()))
            val ip = `in`.readLine() // 获取IP地址
            `in`.close()
            log("getExternalIP: $ip")
            return ip
        } catch (e: Exception) {
            // Handle Exception
        }
        return "0.0.0.0"
    }
}